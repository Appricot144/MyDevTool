import * as fs from "fs";
import axios, { AxiosInstance } from "axios";
import {
  format,
  parseISO,
  isWithinInterval,
  startOfDay,
  endOfDay,
} from "date-fns";

interface Config {
  gitlab: {
    url: string;
    access_token: string;
    username: string;
  };
}

interface MergeRequest {
  id: number;
  iid: number;
  title: string;
  project_id: number;
  web_url: string;
  project: {
    name: string;
    path_with_namespace: string;
  };
}

interface Discussion {
  id: string;
  notes: Note[];
}

interface Note {
  id: number;
  body: string;
  author: {
    username: string;
  };
  created_at: string;
  position?: {
    new_path?: string;
    old_path?: string;
    new_line?: number;
    old_line?: number;
  };
  noteable_id: number;
  noteable_type: string;
  resolved: boolean;
  resolvable: boolean;
  system: boolean;
}

interface DiscussionOutput {
  projectName: string;
  author: string;
  comment: string;
  url: string;
  date: string;
  filePath?: string;
  line?: number;
  resolved: boolean;
}

export class GitLabDiscussionFetcher {
  private client: AxiosInstance;
  private config: Config;

  constructor(configPath: string) {
    // config.json
    const configContent = fs.readFileSync(configPath, "utf-8");
    this.config = JSON.parse(configContent);

    this.client = axios.create({
      baseURL: `${this.config.gitlab.url}/api/v4`,
      headers: {
        "PRIVATE-TOKEN": this.config.gitlab.access_token,
      },
    });
  }

  // get merge request
  async fetchUserMergeRequests(): Promise<MergeRequest[]> {
    try {
      const response = await this.client.get("/merge_requests", {
        params: {
          author_username: this.config.gitlab.username,
          state: "all",
          per_page: 100,
        },
      });
      return response.data;
    } catch (error) {
      console.log("Error fetching merge requests:", error);
      return [];
    }
  }

  // get merge request discussions
  async fetchMergeRequestDiscussions(
    projectId: number,
    mrIid: number
  ): Promise<Discussion[]> {
    try {
      const response = await this.client.get(
        `/projects/${projectId}/merge_requests/${mrIid}/discussions`
      );
      return response.data;
    } catch (error) {
      console.log(`Error fetching discussions for MR ${mrIid}:`, error);
      return [];
    }
  }

  filterDiscussionsByDate(
    discussions: Discussion[],
    targetDate: Date
  ): Discussion[] {
    const dayStart = startOfDay(targetDate);
    const dayEnd = endOfDay(targetDate);

    return discussions
      .map((discussion) => ({
        ...discussion,
        notes: discussion.notes.filter((note) => {
          const noteDate = parseISO(note.created_at);
          return isWithinInterval(noteDate, { start: dayStart, end: dayEnd });
        }),
      }))
      .filter((discussion) => discussion.notes.length > 0);
  }

  generateNoteUrl(mr: MergeRequest, noteId: number): string {
    return `${mr.web_url}#note_${noteId}`;
  }

  getFileLocation(note: Note): { filePath?: string; line?: number } {
    if (!note.position) return {};

    const filePath = note.position.new_path || note.position.old_path;
    const line = note.position.new_line || note.position.old_line;

    return { filePath, line };
  }

  // output MarkDown format
  formatOutput(
    mrDiscussions: Map<
      string,
      { mr: MergeRequest; discussions: DiscussionOutput[] }
    >
  ): string {
    let output = "";

    for (const [mrKey, { mr, discussions }] of mrDiscussions) {
      if (discussions.length === 0) continue;

      output += `### MR: [${mr.title}](${mr.web_url})\n`;

      for (const discussion of discussions) {
        const checkbox = discussion.resolved ? "[x]" : "[ ]";
        const fileLocation =
          discussion.filePath && discussion.line
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
  async run(targetDate: Date): Promise<void> {
    console.log(
      `Fetching discussions for ${format(targetDate, "yyyy-MM-dd")}...`
    );

    const mergeRequests = await this.fetchUserMergeRequests();
    console.log(`Found ${mergeRequests.length} merge requests`);

    const mrDiscussions = new Map<
      string,
      { mr: MergeRequest; discussions: DiscussionOutput[] }
    >();

    for (const mr of mergeRequests) {
      const discussions = await this.fetchMergeRequestDiscussions(
        mr.project_id,
        mr.iid
      );
      const filteredDiscussions = this.filterDiscussionsByDate(
        discussions,
        targetDate
      );

      if (filteredDiscussions.length === 0) continue;

      const discussionOutputs: DiscussionOutput[] = [];

      for (const discussion of filteredDiscussions) {
        for (const note of discussion.notes) {
          // システムノートは除外
          if (note.system) continue;

          const { filePath, line } = this.getFileLocation(note);

          discussionOutputs.push({
            projectName: mr.project.name,
            author: note.author.username,
            comment: note.body.trim(),
            url: this.generateNoteUrl(mr, note.id),
            date: format(parseISO(note.created_at), "MM/dd HH:mm"),
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
  }
}
