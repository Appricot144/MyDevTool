param(
    [Parameter(Mandatory=$true)]
    [string]$GitLabUrl,
    
    [Parameter(Mandatory=$true)]
    [string]$AccessToken,
    
    [Parameter(Mandatory=$false)]
    [string]$ProjectId,
    
    [Parameter(Mandatory=$false)]
    [datetime]$StartDate,
    
    [Parameter(Mandatory=$false)]
    [datetime]$EndDate,
    
    [Parameter(Mandatory=$false)]
    [switch]$AllProjects,
    
    [Parameter(Mandatory=$false)]
    [string]$OutputPath,
    
    [Parameter(Mandatory=$false)]
    [switch]$PassThru  # パイプライン出力用フラグ
)

# 設定値
$Username = "Aniline"
$Headers = @{
    "PRIVATE-TOKEN" = $AccessToken
    "Content-Type" = "application/json"
}

# GitLab APIのベースURL
$BaseUrl = "$GitLabUrl/api/v4"

# ログ出力関数（PassThruモードの時は標準エラーに出力）
function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] [$Level] $Message"
    
    if ($PassThru) {
        Write-Error $logMessage  # パイプライン時は標準エラーに出力
    } else {
        Write-Host $logMessage
    }
}

# GitLab APIコール関数
function Invoke-GitLabApi {
    param(
        [string]$Endpoint,
        [hashtable]$Params = @{}
    )
    
    $Uri = "$BaseUrl$Endpoint"
    if ($Params.Count -gt 0) {
        $QueryString = ($Params.GetEnumerator() | ForEach-Object { "$($_.Key)=$($_.Value)" }) -join "&"
        $Uri += "?$QueryString"
    }
    
    try {
        $Response = Invoke-RestMethod -Uri $Uri -Headers $Headers -Method Get
        return $Response
    }
    catch {
        Write-Log "API呼び出しエラー: $($_.Exception.Message)" "ERROR"
        throw
    }
}

# プロジェクト一覧取得
function Get-Projects {
    Write-Log "プロジェクト一覧を取得中..."
    
    if ($ProjectId) {
        # 特定のプロジェクトのみ
        try {
            $Project = Invoke-GitLabApi -Endpoint "/projects/$ProjectId"
            return @($Project)
        }
        catch {
            Write-Log "プロジェクトID $ProjectId が見つかりません" "ERROR"
            return @()
        }
    }
    else {
        # 全プロジェクト取得
        $AllProjectsList = @()
        $Page = 1
        $PerPage = 100
        
        do {
            $Params = @{
                "page" = $Page
                "per_page" = $PerPage
                "membership" = "true"  # 自分がメンバーのプロジェクトのみ
            }
            
            $Projects = Invoke-GitLabApi -Endpoint "/projects" -Params $Params
            $AllProjectsList += $Projects
            $Page++
        } while ($Projects.Count -eq $PerPage)
        
        return $AllProjectsList
    }
}

# Merge Request一覧取得
function Get-MergeRequests {
    param([object]$Project)
    
    Write-Log "プロジェクト '$($Project.name)' のMerge Requestを取得中..."
    
    $AllMRs = @()
    $Page = 1
    $PerPage = 100
    
    do {
        $Params = @{
            "page" = $Page
            "per_page" = $PerPage
            "author_username" = $Username
            "state" = "all"  # 全ての状態のMRを取得
        }
        
        # 日付フィルタ
        if ($StartDate) {
            $Params["created_after"] = $StartDate.ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
        }
        if ($EndDate) {
            $Params["created_before"] = $EndDate.ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
        }
        
        $MRs = Invoke-GitLabApi -Endpoint "/projects/$($Project.id)/merge_requests" -Params $Params
        $AllMRs += $MRs
        $Page++
    } while ($MRs.Count -eq $PerPage)
    
    return $AllMRs
}

# Discussion取得
function Get-Discussions {
    param(
        [object]$Project,
        [object]$MergeRequest
    )
    
    Write-Log "MR #$($MergeRequest.iid) のDiscussionを取得中..."
    
    try {
        $Discussions = Invoke-GitLabApi -Endpoint "/projects/$($Project.id)/merge_requests/$($MergeRequest.iid)/discussions"
        return $Discussions
    }
    catch {
        Write-Log "MR #$($MergeRequest.iid) のDiscussion取得に失敗: $($_.Exception.Message)" "WARN"
        return @()
    }
}

# メイン処理
function Main {
    Write-Log "GitLab Discussion取得スクリプトを開始"
    Write-Log "対象ユーザー: $Username"
    
    # パラメータ検証
    if (-not $AllProjects -and -not $ProjectId) {
        Write-Log "プロジェクトIDを指定するか、-AllProjectsフラグを使用してください" "ERROR"
        return
    }
    
    # 結果格納用配列
    $AllResults = @()
    
    try {
        # プロジェクト取得
        $Projects = Get-Projects
        Write-Log "対象プロジェクト数: $($Projects.Count)"
        
        foreach ($Project in $Projects) {
            Write-Log "プロジェクト処理中: $($Project.name) (ID: $($Project.id))"
            
            # Merge Request取得
            $MergeRequests = Get-MergeRequests -Project $Project
            Write-Log "見つかったMR数: $($MergeRequests.Count)"
            
            foreach ($MR in $MergeRequests) {
                # Discussion取得
                $Discussions = Get-Discussions -Project $Project -MergeRequest $MR
                
                if ($Discussions.Count -gt 0) {
                    $Result = @{
                        "project_id" = $Project.id
                        "project_name" = $Project.name
                        "project_url" = $Project.web_url
                        "merge_request_id" = $MR.id
                        "merge_request_iid" = $MR.iid
                        "merge_request_title" = $MR.title
                        "merge_request_url" = $MR.web_url
                        "merge_request_created_at" = $MR.created_at
                        "merge_request_state" = $MR.state
                        "discussions_count" = $Discussions.Count
                        "discussions" = $Discussions
                    }
                    
                    $AllResults += $Result
                    Write-Log "MR #$($MR.iid) に $($Discussions.Count) 件のDiscussionが見つかりました"
                }
            }
        }
        
        # 結果出力
        if ($AllResults.Count -gt 0) {
            $JsonOutput = $AllResults | ConvertTo-Json -Depth 10
            
            if ($PassThru) {
                # パイプライン出力（標準出力に出力）
                Write-Output $JsonOutput
            } elseif ($OutputPath) {
                # ファイル出力
                $JsonOutput | Out-File -FilePath $OutputPath -Encoding UTF8
                Write-Log "結果を $OutputPath に保存しました"
            } else {
                # デフォルトファイル出力
                $DefaultPath = "gitlab_discussions.json"
                $JsonOutput | Out-File -FilePath $DefaultPath -Encoding UTF8
                Write-Log "結果を $DefaultPath に保存しました"
            }
            
            Write-Log "合計 $($AllResults.Count) 件のMRに関するDiscussionを取得しました"
            
            # サマリー表示
            $TotalDiscussions = ($AllResults | ForEach-Object { $_.discussions_count } | Measure-Object -Sum).Sum
            Write-Log "合計Discussion数: $TotalDiscussions"
        }
        else {
            Write-Log "条件に一致するDiscussionは見つかりませんでした" "WARN"
        }
    }
    catch {
        Write-Log "処理中にエラーが発生しました: $($_.Exception.Message)" "ERROR"
        throw
    }
    
    Write-Log "処理完了"
}

# スクリプト実行
Main