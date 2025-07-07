import * as path from "path";
import * as fs from "fs";
import { parseISO } from "date-fns";
import { GitLabDiscussionFetcher } from "./gitlab-discussion-fetcher";

async function main() {
  const args = process.argv.slice(2);

  if (args.length < 1) {
    console.log("Usage: node <main.js> <date>");
    console.log("Example: node main.js 2025-07-07");
    process.exit(1);
  }

  const dateStr = args[0];
  const targetDate = parseISO(dateStr);

  if (isNaN(targetDate.getTime())) {
    console.log("Invalid date format. Please use YYYY-MM-DD");
    process.exit(1);
  }

  const configPath = path.join(process.cwd(), "config.json");

  if (!fs.existsSync(configPath)) {
    console.log("config.json not found in current directory");
    process.exit(1);
  }

  const fetcher = new GitLabDiscussionFetcher(configPath);
  await fetcher.run(targetDate);
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
