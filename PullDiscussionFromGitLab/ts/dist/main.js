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
Object.defineProperty(exports, "__esModule", { value: true });
const path = __importStar(require("path"));
const fs = __importStar(require("fs"));
const date_fns_1 = require("date-fns");
const gitlab_discussion_fetcher_1 = require("./gitlab-discussion-fetcher");
function main() {
    return __awaiter(this, void 0, void 0, function* () {
        const args = process.argv.slice(2);
        if (args.length < 1) {
            console.log("Usage: node <main.js> <date>");
            console.log("Example: node main.js 2025-07-07");
            process.exit(1);
        }
        const dateStr = args[0];
        const targetDate = (0, date_fns_1.parseISO)(dateStr);
        if (isNaN(targetDate.getTime())) {
            console.log("Invalid date format. Please use YYYY-MM-DD");
            process.exit(1);
        }
        const configPath = path.join(process.cwd(), "config.json");
        if (!fs.existsSync(configPath)) {
            console.log("config.json not found in current directory");
            process.exit(1);
        }
        const fetcher = new gitlab_discussion_fetcher_1.GitLabDiscussionFetcher(configPath);
        yield fetcher.run(targetDate);
    });
}
process.on("unhandledRejection", (error) => {
    console.log("Unhandled rejection:", error);
    process.exit(1);
});
if (require.main === module) {
    main().catch((error) => {
        console.log("Error:", error);
        process.exit(1);
    });
}
