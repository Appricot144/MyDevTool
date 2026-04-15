import { getFetcher } from "./fetcher";
import type { ResDiscussion, ResMr } from "./response";

/**
 * 出力形式:
 * ### Rv: project_name
 * - [ ] (source.ts:85) [discuttion-url]
 *  - コメント
 */

async function main() {
  let private_token = "";
  let server_url = "";
  let period: string = "2w";
  const username = "r_habata";

  // command line args
  const args = process.argv.slice(2);
  for (const [index, arg] of args.entries()) {
    switch (index) {
      case 0:
        server_url = arg;
        break;
      case 1:
        private_token = arg;
        break;
      case 2:
        period = arg;
        break;
    }
  }

  // get user MR list
  let list_mr_url = `${server_url}/api/v4/merge_requests?author_username=${username}`;
  const mrList = await getFetcher<ResMr[]>(list_mr_url);

  for (const mr of mrList) {
    // get MR discussions
    const discussion_url = `${server_url}/api/v4/projects/${mr.project_id}/merge_requests/${mr.iid}/discussions`;
    const items = (await getFetcher<ResDiscussion[]>(discussion_url))
      .map((discussion) => discussion.notes)
      .flat()
      .filter((note) => note.author.username !== username);

    // output discussions
    console.log(`### Rv: ${mr.title}`);
    items.forEach((item) => {
      const source = item.position.new_path;
      const line = item.position.new_line;

      console.log(
        `- [ ] (${source}:L${line}) [${mr.web_url}/#note_${item.id}]`
      );
      console.log(` - ${item.body}`);
    });
    console.log("\n");
  }
}

main();
