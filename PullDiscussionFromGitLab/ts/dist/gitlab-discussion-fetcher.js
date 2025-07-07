"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.GitLabDiscussionFetcher = void 0;
const fs = __importStar(require("fs"));
const axios_1 = __importDefault(require("axios"));
const date_fns_1 = require("date-fns");
class GitLabDiscussionFetcher {
    constructor(configPath) {
        // config.json
        const configContent = fs.readFileSync(configPath, "utf-8");
        this.config = JSON.parse(configContent);
        this.client = axios_1.default.create({
            baseURL: `${this.config.gitlab.url}/api/v3`,
            headers: {
                "PRIVATE-TOKEN": this.config.gitlab.access_token,
            },
        });
    }
    // get merge request
    fetchUserMergeRequests() {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const response = yield this.client.get("/merge_requests", {
                    params: {
                        author_username: this.config.gitlab.username,
                        state: "all",
                        per_page: 100,
                    },
                });
                return response.data;
            }
            catch (error) {
                console.log("Error fetching merge requests:", error);
                return [];
            }
        });
    }
    // get merge request discussions
    fetchMergeRequestDiscussions(projectId, mrIid) {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const response = yield this.client.get(`/projects/${projectId}/merge_requests/${mrIid}/discussions`);
                return response.data;
            }
            catch (error) {
                console.log(`Error fetching discussions for MR ${mrIid}:`, error);
                return [];
            }
        });
    }
    filterDiscussionsByDate(discussions, targetDate) {
        const dayStart = (0, date_fns_1.startOfDay)(targetDate);
        const dayEnd = (0, date_fns_1.endOfDay)(targetDate);
        return discussions
            .map((discussion) => (Object.assign(Object.assign({}, discussion), { notes: discussion.notes.filter((note) => {
                const noteDate = (0, date_fns_1.parseISO)(note.created_at);
                return (0, date_fns_1.isWithinInterval)(noteDate, { start: dayStart, end: dayEnd });
            }) })))
            .filter((discussion) => discussion.notes.length > 0);
    }
    generateNoteUrl(mr, noteId) {
        return `${mr.web_url}#note_${noteId}`;
    }
    getFileLocation(note) {
        if (!note.position)
            return {};
        const filePath = note.position.new_path || note.position.old_path;
        const line = note.position.new_line || note.position.old_line;
        return { filePath, line };
    }
    // output MarkDown format
    formatOutput(mrDiscussions) {
        let output = "";
        for (const [mrKey, { mr, discussions }] of mrDiscussions) {
            if (discussions.length === 0)
                continue;
            output += `### MR: [${mr.title}](${mr.web_url})\n`;
            for (const discussion of discussions) {
                const checkbox = discussion.resolved ? "[x]" : "[ ]";
                const fileLocation = discussion.filePath && discussion.line
                    ? ` (\`${discussion.filePath}:${discussion.line}\`)`
                    : "";
                output += `- ${checkbox} [${discussion.projectName}] : ${discussion.author}${fileLocation}\n`;
                output += `  - comment: ${discussion.comment}\n`;
                output += `  - url: ${discussion.url}\n`;
                output += `  - date: ${discussion.date}\n`;
            }
            output += "\n";
        }
        return output;
    }
    // get MR discussions & output MarkDown format
    run(targetDate) {
        return __awaiter(this, void 0, void 0, function* () {
            console.log(`Fetching discussions for ${(0, date_fns_1.format)(targetDate, "yyyy-MM-dd")}...`);
            const mergeRequests = yield this.fetchUserMergeRequests();
            console.log(`Found ${mergeRequests.length} merge requests`);
            const mrDiscussions = new Map();
            for (const mr of mergeRequests) {
                const discussions = yield this.fetchMergeRequestDiscussions(mr.project_id, mr.iid);
                const filteredDiscussions = this.filterDiscussionsByDate(discussions, targetDate);
                if (filteredDiscussions.length === 0)
                    continue;
                const discussionOutputs = [];
                for (const discussion of filteredDiscussions) {
                    for (const note of discussion.notes) {
                        // システムノートは除外
                        if (note.system)
                            continue;
                        const { filePath, line } = this.getFileLocation(note);
                        discussionOutputs.push({
                            projectName: mr.project.name,
                            author: note.author.username,
                            comment: note.body.trim(),
                            url: this.generateNoteUrl(mr, note.id),
                            date: (0, date_fns_1.format)((0, date_fns_1.parseISO)(note.created_at), "MM/dd HH:mm"),
                            filePath,
                            line,
                            resolved: note.resolved || false,
                        });
                    }
                }
                if (discussionOutputs.length > 0) {
                    mrDiscussions.set(mr.web_url, { mr, discussions: discussionOutputs });
                }
            }
            const output = this.formatOutput(mrDiscussions);
            console.log(output);
        });
    }
}
exports.GitLabDiscussionFetcher = GitLabDiscussionFetcher;
