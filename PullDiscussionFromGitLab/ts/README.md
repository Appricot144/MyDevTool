# GitLab Discussion Fetcher

GitLab APIを使用して、指定した日付のマージリクエストに付けられたディスカッションを取得し、マークダウン形式で出力するTypeScriptプログラムです。

## セットアップ

1. 依存関係をインストールします：
```bash
npm install
```

2. `config.json`を作成し、GitLabの情報を設定します：
```json
{
  "gitlab": {
    "url": "https://your-gitlab.com",
    "access_token": "your-token",
    "username": "Aniline"
  }
}
```

### GitLab Access Tokenの取得方法

1. GitLabにログインします
2. ユーザー設定 → Access Tokens に移動します
3. 新しいトークンを作成し、以下のスコープを選択します：
   - `api`
   - `read_api`
   - `read_user`

## 使用方法

指定した日付のディスカッションを取得します：

```bash
# TypeScriptで直接実行
npx ts-node gitlab-discussions.ts 2024-06-24

# または npm script を使用
npm start 2024-06-24
```

## 出力形式

標準出力にマークダウン形式で出力されます：

```markdown
### MR: [マージリクエストのタイトル](https://gitlab.com/project/frontend/-/merge_requests/15)
- [ ] [プロジェクト名] : コメント送信者 (`Login.tsx:45`)
  - comment: この部分でバリデーションエラーのハンドリングを追加した方が良いと思います。
  - url: https://gitlab.com/project/frontend/-/merge_requests/15#note_789
  - date: 06/24 14:15
```

- チェックボックスは、ディスカッションが解決済みの場合は `[x]` になります
- ファイル名と行番号は、コードに関するコメントの場合のみ表示されます
- システムメッセージ（自動生成されたコメント）は除外されます

## ビルド

TypeScriptをJavaScriptにコンパイルする場合：

```bash
npm run build
```

コンパイル後のファイルは `dist` ディレクトリに出力されます。

## トラブルシューティング

### API制限エラー
GitLab APIには rate limit があります。大量のマージリクエストがある場合は、適宜待機時間を設けるか、処理を分割してください。

### 権限エラー
Access Tokenに必要なスコープが設定されているか確認してください。プライベートプロジェクトのディスカッションを取得する場合は、そのプロジェクトへのアクセス権限が必要です。

### 日付フォーマットエラー
日付は `YYYY-MM-DD` 形式で指定してください（例：2024-06-24）。