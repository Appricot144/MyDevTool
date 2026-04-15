# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## プロジェクト概要

単体試験の証跡スクリーンショットを効率的に収集・管理するための Windows PowerShell GUI ツール（証跡キャプチャツール）。

- **動作環境**: Windows 10/11、PowerShell 5.1 以上
- **依存関係**: なし（Windows 標準の .NET Framework / Windows Forms を使用）
- **ビルドステップ**: 不要（スクリプト直接実行）

## 実行方法

```powershell
# 初回のみ（管理者 PowerShell で実行）
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned

# 起動：ファイルを右クリック → "PowerShellで実行"
# またはターミナルから
powershell -File "証跡キャプチャ.ps1"
```

## アーキテクチャ

単一ファイル構成（`証跡キャプチャ.ps1`）の PowerShell スクリプト。Windows Forms で GUI を構築し、Win32 API でクリップボードを監視する。

### 状態管理

| 変数 | 役割 |
|---|---|
| `$step` | 現在の手順番号（スピナーと連動） |
| `$subStep` | 手順内の連番（手順変更でリセット） |
| `$lastSeq` | クリップボードシーケンス番号（重複検知用） |
| `$thumbList` | 保存済み画像ファイルパスの配列 |
| `$thumbIndex` | サムネイルパネルで選択中のインデックス |

### データフロー

```
Alt+PrintScreen → クリップボード → タイマー（500ms 間隔）で監視
    ↓ シーケンス番号の変化を検知
Win32 API で画像取得 → {step}-{subStep}.png として保存
    ↓
サムネイルパネルへ追加 → UI 更新
```

### 主要コンポーネント（スクリプト内の構造）

- **設定セクション**: `$saveFolder` 変数（スクリプト冒頭で変更可能）
- **メインフォーム**: 520×420px、常時最前面表示
- **コントロールパネル**: 手順番号スピナー、監視ON/OFFボタン、ステータスラベル
- **サムネイルパネル**: FlowLayoutPanel、クリックで拡大表示
- **拡大表示ウィンドウ**: 900×660px、前後ナビゲーション付き
- **クリップボードモニター**: `GetClipboardSequenceNumber`（Win32 API）で変化を検知

### ファイル命名規則

```
{手順番号}-{連番}.png
例: 4-1.png, 4-2.png, 5-1.png
```

## 保存先の変更

スクリプト冒頭の1行のみ編集する：

```powershell
$saveFolder = "C:\作業フォルダ\Screenshots"
```

存在しないフォルダは起動時に自動作成される。

## テスト

自動テストフレームワークは存在しない。変更後は以下を手動確認する：

1. スクリプトが起動し、フォームが表示される
2. `Alt+PrintScreen` で画像がキャプチャされ、サムネイルに追加される
3. `Ctrl+↑` / `Ctrl+↓` で手順番号が変わり、連番が 1 にリセットされる
4. サムネイルをクリック / `Enter` で拡大表示が開く
5. 拡大表示で `←` `→` により前後の画像へ遷移できる
6. `⏸ 監視停止` で監視が止まり、`▶ 監視再開` で再開される
