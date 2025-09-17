import { getFetcher } from "./fetcher";
import type { ResDiscussion, ResMr } from "./response";

async function main() {
  let private_token = "";
  let server_url = "";
  let period: string = "2w";
  const username = "r_habata";

  // コマンドライン引数の処理
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

  // get my MR list
  let list_mr_url = `${server_url}/api/v4/merge_requests?author_username=${username}`;
  const mrList = await getFetcher<ResMr[]>(list_mr_url);

  // get my MR discussions
  const discussions = mrList.map(async (mr) => {
    const discussion_url = `${server_url}/api/v4/projects/${mr.project_id}/merge_requests/${mr.iid}/discussions`;
    return await getFetcher<ResDiscussion[]>(discussion_url);
  });
  const outputItems = discussions.map((discussion) => {
    // TODO ここでPromise型になる理由は？
    return Array.from(discussion.notes).filter(
      (note) => note.auther.username !== username
    );
  });

  // format output

  // output
  /**
   * 出力形式:
   * ### Rv: project_name
   * - [ ] (source.ts:85) [discuttion-url]
   *  - コメント
   */
}
