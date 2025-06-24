param(
    [Parameter(Mandatory=$true)]
    [string]$GitLabUrl,
    
    [Parameter(Mandatory=$true)]
    [string]$AccessToken,
    
    [Parameter(Mandatory=$false)]
    [string]$TodoDirectory = ".",
    
    [Parameter(Mandatory=$false)]
    [string]$ProjectId,
    
    [Parameter(Mandatory=$false)]
    [datetime]$StartDate,
    
    [Parameter(Mandatory=$false)]
    [datetime]$EndDate,
    
    [Parameter(Mandatory=$false)]
    [switch]$AllProjects,
    
    [Parameter(Mandatory=$false)]
    [switch]$UnresolvedOnly,
    
    [Parameter(Mandatory=$false)]
    [switch]$RecentOnly,
    
    [Parameter(Mandatory=$false)]
    [int]$RecentDays = 7,
    
    [Parameter(Mandatory=$false)]
    [switch]$DryRun
)

# 設定
$Username = "Aniline"
$Today = Get-Date -Format "yyyy-MM-dd"
$TodayFile = Join-Path $TodoDirectory "$Today.md"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "🚀 GitLab Discussion → TODO.md 変換開始" -ForegroundColor Green
Write-Host "対象日: $Today" -ForegroundColor Cyan
Write-Host "出力先: $TodayFile" -ForegroundColor Cyan

if ($DryRun) {
    Write-Host "⚠️  DRY RUN モード - 実際には更新しません" -ForegroundColor Yellow
}

# 1. GitLab Discussionを取得
Write-Host "`n📡 GitLab Discussionを取得中..." -ForegroundColor Blue

$GetDiscussionScript = Join-Path $ScriptDir "Get-GitLabDiscussions.ps1"
if (-not (Test-Path $GetDiscussionScript)) {
    Write-Host "❌ GitLab取得スクリプトが見つかりません: $GetDiscussionScript" -ForegroundColor Red
    Write-Host "   Get-GitLabDiscussions.ps1 を同じフォルダに配置してください" -ForegroundColor Yellow
    exit 1
}

# パラメータ構築
$DiscussionParams = @{
    "GitLabUrl" = $GitLabUrl
    "AccessToken" = $AccessToken
}

if ($ProjectId) { $DiscussionParams["ProjectId"] = $ProjectId }
if ($StartDate) { $DiscussionParams["StartDate"] = $StartDate }
if ($EndDate) { $DiscussionParams["EndDate"] = $EndDate }
if ($AllProjects) { $DiscussionParams["AllProjects"] = $true }

try {
    # 一時ファイルに出力
    $TempJsonFile = Join-Path $env:TEMP "gitlab_discussions_temp.json"
    $DiscussionParams["OutputPath"] = $TempJsonFile
    
    # GitLab discussion取得スクリプトを実行
    & $GetDiscussionScript @DiscussionParams
    
    if (-not (Test-Path $TempJsonFile)) {
        Write-Host "❌ GitLab discussionデータの取得に失敗しました" -ForegroundColor Red
        exit 1
    }
    
    # JSONデータを読み込み
    $discussionData = Get-Content $TempJsonFile -Encoding UTF8 | ConvertFrom-Json
    Remove-Item $TempJsonFile -ErrorAction SilentlyContinue
    
} catch {
    Write-Host "❌ GitLab discussion取得エラー: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 取得完了: $($discussionData.Count) 件のMR" -ForegroundColor Green

# 2. TODOアイテムに変換
Write-Host "`n🔄 TODOアイテムに変換中..." -ForegroundColor Blue

$todoItems = @()

foreach ($mrData in $discussionData) {
    foreach ($discussion in $mrData.discussions) {
        foreach ($note in $discussion.notes) {
            # フィルタリング条件
            $shouldAdd = $true
            
            # 自分のコメントは除外
            if ($note.author.username -eq $Username) {
                $shouldAdd = $false
            }
            
            # システムメッセージは除外
            if ($note.system) {
                $shouldAdd = $false
            }
            
            # 未解決のみフィルタ
            if ($UnresolvedOnly -and $note.PSObject.Properties.Name -contains "resolved" -and $note.resolved) {
                $shouldAdd = $false
            }
            
            # 最近のコメントのみフィルタ
            if ($RecentOnly) {
                $noteDate = [DateTime]$note.created_at
                $cutoffDate = (Get-Date).AddDays(-$RecentDays)
                if ($noteDate -lt $cutoffDate) {
                    $shouldAdd = $false
                }
            }
            
            if ($shouldAdd) {
                # 優先度判定
                $priority = "📋"
                $comment = $note.body.ToLower()
                
                if ($comment -match "(urgent|緊急|至急|asap|重要)") {
                    $priority = "🔥"
                } elseif ($comment -match "(fix|修正|バグ|bug|error|エラー)") {
                    $priority = "🐛"
                } elseif ($comment -match "(review|レビュー|確認)") {
                    $priority = "👀"
                } elseif ($note.type -eq "DiffNote") {
                    $priority = "💻"
                }
                
                # ファイル情報
                $fileInfo = ""
                if ($note.position -and $note.position.new_path) {
                    $fileInfo = " (`$($note.position.new_path):$($note.position.new_line)`)"
                }
                
                # 日付フォーマット
                $noteDate = [DateTime]$note.created_at
                $dateStr = $noteDate.ToString("MM/dd HH:mm")
                
                # TODOアイテム作成
                $todoItem = [PSCustomObject]@{
                    "Priority" = $priority
                    "Title" = "**[$($mrData.project_name)] $($note.author.name)からのコメント対応**$fileInfo"
                    "Details" = @(
                        "  - MR: [$($mrData.merge_request_title)]($($mrData.merge_request_url))",
                        "  - コメント: $($note.body)",
                        "  - [詳細]($($mrData.merge_request_url)#note_$($note.id))",
                        "  - 日付: $dateStr"
                    )
                    "CreatedAt" = $noteDate
                    "NoteId" = $note.id
                    "IsResolved" = $note.PSObject.Properties.Name -contains "resolved" -and $note.resolved
                }
                
                $todoItems += $todoItem
            }
        }
    }
}

Write-Host "✅ 変換完了: $($todoItems.Count) 件のTODOアイテム" -ForegroundColor Green

if ($todoItems.Count -eq 0) {
    Write-Host "ℹ️  追加するTODOアイテムがありません" -ForegroundColor Yellow
    exit 0
}

# 3. TODO.mdファイルの更新
Write-Host "`n📝 TODO.mdファイルを更新中..." -ForegroundColor Blue

# 既存のTODOファイルを読み込み
$existingContent = @()
if (Test-Path $TodayFile) {
    $existingContent = Get-Content $TodayFile -Encoding UTF8
    Write-Host "既存のファイルを読み込み: $TodayFile" -ForegroundColor Gray
} else {
    # 新規ファイルの場合、ヘッダーを作成
    $existingContent = @(
        "# TODO - $Today",
        "",
        "## GitLab MR Discussion対応",
        ""
    )
    Write-Host "新規ファイルを作成: $TodayFile" -ForegroundColor Gray
}

# 既存のnote_idを抽出（重複チェック用）
$existingNoteIds = @()
foreach ($line in $existingContent) {
    if ($line -match "note_(\d+)") {
        $existingNoteIds += $Matches[1]
    }
}

# 新しいコンテンツを構築
$newContent = $existingContent
$addedCount = 0

# GitLab Discussion セクションを探す
$sectionIndex = -1
for ($i = 0; $i -lt $newContent.Count; $i++) {
    if ($newContent[$i] -match "## GitLab MR Discussion対応") {
        $sectionIndex = $i
        break
    }
}

# セクションが見つからない場合は末尾に追加
if ($sectionIndex -eq -1) {
    $newContent += @("", "## GitLab MR Discussion対応", "")
    $sectionIndex = $newContent.Count - 1
}

# 優先度と日付でソート
$sortedTodos = $todoItems | Sort-Object @{Expression={
    switch ($_.Priority) {
        "🔥" { 1 }
        "🐛" { 2 }
        "👀" { 3 }
        "💻" { 4 }
        default { 5 }
    }
}}, @{Expression={$_.CreatedAt}; Descending=$true}

# TODOアイテムを挿入
$insertIndex = $sectionIndex + 1
foreach ($todo in $sortedTodos) {
    # 重複チェック
    if ($existingNoteIds -contains $todo.NoteId) {
        Write-Host "スキップ（重複）: note_$($todo.NoteId)" -ForegroundColor Gray
        continue
    }
    
    # 解決済みマーク
    $resolvedMark = if ($todo.IsResolved) { " ✅" } else { "" }
    
    # TODOアイテムを挿入
    $todoLines = @(
        "",
        "$($todo.Priority) $($todo.Title)$resolvedMark"
    ) + $todo.Details + @("")
    
    $newContent = $newContent[0..$insertIndex] + $todoLines + $newContent[($insertIndex + 1)..($newContent.Count - 1)]
    $insertIndex += $todoLines.Count
    $addedCount++
    
    Write-Host "追加: $($todo.Priority) $($todo.Title)" -ForegroundColor Green
}

# 4. ファイルに書き込み
if (-not $DryRun) {
    $newContent | Out-File -FilePath $TodayFile -Encoding UTF8
    Write-Host "`n✅ TODO.mdを更新しました!" -ForegroundColor Green
    Write-Host "📄 ファイル: $TodayFile" -ForegroundColor Cyan
    Write-Host "➕ 追加されたアイテム: $addedCount 件" -ForegroundColor Cyan
} else {
    Write-Host "`n🔍 [DRY RUN] 追加予定のアイテム: $addedCount 件" -ForegroundColor Yellow
    Write-Host "📄 対象ファイル: $TodayFile" -ForegroundColor Yellow
}

# 5. サマリー表示
Write-Host "`n📊 処理サマリー" -ForegroundColor Magenta
Write-Host "=" * 40 -ForegroundColor Magenta
Write-Host "取得したMR数: $($discussionData.Count)"
Write-Host "生成されたTODO数: $($todoItems.Count)"
Write-Host "追加されたTODO数: $addedCount"

if ($todoItems.Count -gt 0) {
    Write-Host "`n優先度別:"
    $priorityGroups = $todoItems | Group-Object Priority
    foreach ($group in $priorityGroups) {
        Write-Host "  $($group.Name): $($group.Count) 件"
    }
    
    Write-Host "`nプロジェクト別:"
    $projectCounts = @{}
    foreach ($mrData in $discussionData) {
        $projectCounts[$mrData.project_name] = ($projectCounts[$mrData.project_name] ?? 0) + $mrData.discussions_count
    }
    foreach ($project in $projectCounts.Keys) {
        Write-Host "  $project: $($projectCounts[$project]) 件"
    }
}

Write-Host "`n🎉 処理完了!" -ForegroundColor Green