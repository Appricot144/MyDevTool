# GitLab Discussion → TODO.md 自動変換

GitLabのmerge requestについたdiscussionを自動でTODO.mdに追加するツールです。

## 📁 ファイル構成

```
your-project/
├── scripts/
│   ├── Get-GitLabDiscussions.ps1          # GitLab API取得スクリプト
│   └── GitLab-To-Todo.ps1                 # TODO変換メインスクリプト
├── .vscode/
│   ├── tasks.json                         # VS Codeタスク設定
│   └── keybindings.json                   # キーボードショートカット
├── config.json                            # 設定ファイル
├── update-todo.bat                        # 簡単実行バッチファイル
└── yyyy-mm-dd.md                         # 生成されるTODOファイル
```

## 🛠️ セットアップ手順

### 1. GitLab Personal Access Token作成

1. GitLab → Settings → Access Tokens
2. トークン名: `todo-automation`
3. スコープ: `api` にチェック
4. Create personal access token

### 2. 設定ファイル作成

`config.json` を作成して以下の内容を入力：

```json
{
    "gitlab": {
        "url": "https://your-gitlab.com",
        "access_token": "glpat-xxxxxxxxxxxxxxxxxxxx",
        "username": "your-username"
    },
    "todo": {
        "directory": ".",
        "default_recent_days": 7,
        "auto_add_unresolved_only": true
    },
    "priorities": {
        "urgent_keywords": ["urgent", "緊急", "至急", "asap", "重要"],
        "bug_keywords": ["fix", "修正", "バグ", "bug", "error", "エラー"],
        "review_keywords": ["review", "レビュー", "確認"],
        "emoji": {
            "urgent": "🔥",
            "bug": "🐛",
            "review": "👀",
            "code": "💻",
            "default": "📋"
        }
    }
}
```

### 3. スクリプトファイル配置

- `scripts/` フォルダを作成
- 2つのPowerShellスクリプトを配置

### 4. VS Code設定（オプション）

- `.vscode/tasks.json` を配置
- `.vscode/keybindings.json` を配置

## 🚀 使用方法

### 方法1: バッチファイル実行
```bash
# 簡単実行
update-todo.bat
```

### 方法2: VS Codeタスク
```
Ctrl+Shift+P → "Tasks: Run Task"
→ "GitLab → TODO更新 (全プロジェクト)" を選択
```

### 方法3: キーボードショートカット（VS Code）
```
Ctrl+Shift+G → Ctrl+Shift+T  # 全プロジェクト
Ctrl+Shift+G → Ctrl+Shift+U  # 未解決のみ
Ctrl+Shift+G → Ctrl+Shift+D  # テスト実行
```

### 方法4: PowerShell直接実行
```powershell
# 基本実行
.\scripts\GitLab-To-Todo.ps1 -ConfigFile config.json -AllProjects

# 特定プロジェクトのみ
.\scripts\GitLab-To-Todo.ps1 -ConfigFile config.json -ProjectId 123

# 未解決のみ
.\scripts\GitLab-To-Todo.ps1 -ConfigFile config.json -AllProjects -UnresolvedOnly

# テスト実行（実際には更新しない）
.\scripts\GitLab-To-Todo.ps1 -ConfigFile config.json -AllProjects -DryRun
```

## 📋 生成されるTODO例

```markdown
# TODO - 2024-06-24

## GitLab MR Discussion対応

🔥 **[web-app-frontend] 山田太郎からのコメント対応** (`Login.tsx:45`)
  - MR: [feat: ユーザー認証機能の実装](https://gitlab.com/project/-/merge_requests/15)
  - コメント: この部分でバリデーションエラーのハンドリングを追加した方が良いと思います。
  - [詳細](https://gitlab.com/project/-/merge_requests/15#note_789)
  - 日付: 06/24 14:15

👀 **[web-app-backend] 佐藤次郎からのコメント対応**
  - MR: [fix: SQLパフォーマンス改善](https://gitlab.com/project/-/merge_requests/8)
  - コメント: パフォーマンステストの結果を確認させてください
  - [詳細](https://gitlab.com/project/-/merge_requests/8#note_794)
  - 日付: 06/24 16:30
```

## 🎯 優先度について

コメント内容によって自動で優先度が設定されます：

- 🔥 **緊急**: urgent, 緊急, 至急, asap, 重要
- 🐛 **バグ**: fix, 修正, バグ, bug, error, エラー  
- 👀 **レビュー**: review, レビュー, 確認
- 💻 **コード**: コードレビューコメント（DiffNote）
- 📋 **その他**: 上記以外

## 🔧 トラブルシューティング

### エラー: スクリプトが実行できない
```powershell
# PowerShell実行ポリシーを変更
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### エラー: GitLab APIエラー
- Access Tokenが正しいか確認
- GitLab URLが正しいか確認
- ネットワーク接続を確認

### TODOが追加されない
- `-DryRun` フラグでテスト実行
- ログ出力を確認
- フィルタ条件（未解決のみ、期間指定）を確認

## 📝 カスタマイズ

### 優先度キーワードの変更
`config.json` の `priorities` セクションを編集

### 絵文字の変更
`config.json` の `emoji` セクションを編集

### デフォルト設定の変更
`config.json` の `todo` セクションを編集

## 🔒 セキュリティ

- `config.json` はGitにコミットしないでください
- `.gitignore` に `config.json` を追加することを推奨
- Access Tokenは定期的に再生成してください