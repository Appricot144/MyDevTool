# ============================================================
# 証跡キャプチャツール
# ============================================================
# 【仕様】
#   - 保存先    : $saveFolder で指定（存在しない場合は自動作成）
#   - ファイル名 : {手順番号}-{連番}.png  例) 4-1.png, 4-2.png
#   - 自動保存  : PrintScreen を押すたびにクリップボードを監視して自動保存
#   - 重複検知  : Windowsのクリップボードシーケンス番号で管理
#               （押した回数 = 保存枚数）
#   - テキスト等のコピーは無視（画像のみ保存）
#
# 【ショートカット】
#   Ctrl+↑     : 手順番号 +1（連番リセット）
#   Ctrl+↓     : 手順番号 -1（連番リセット）
#   Enter       : 選択中サムネイルを拡大表示
#
# 【拡大表示ウィンドウ】
#   ←  / →    : 前後の画像へ移動
#   Esc         : 閉じる
# ============================================================

Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

# クリップボードシーケンス番号取得 API
Add-Type @"
    using System.Runtime.InteropServices;
    public class ClipboardHelper {
        [DllImport("user32.dll")]
        public static extern uint GetClipboardSequenceNumber();
    }
"@

# ============================================================
# 設定
# ============================================================
$saveFolder = "C:\作業フォルダ\Screenshots"
if (!(Test-Path $saveFolder)) { New-Item -ItemType Directory -Path $saveFolder | Out-Null }

# ============================================================
# 状態変数
# ============================================================
$script:step      = 1
$script:subStep   = 1
$script:testItem  = 1
$script:lastSeq   = [ClipboardHelper]::GetClipboardSequenceNumber()
$script:thumbList = @()
$script:thumbIndex = -1

# ============================================================
# メインフォーム
# ============================================================
$form = New-Object System.Windows.Forms.Form
$form.Text            = "証跡キャプチャ"
$form.Size            = New-Object System.Drawing.Size(520, 453)
$form.TopMost         = $true
$form.StartPosition   = "Manual"
$form.Location        = New-Object System.Drawing.Point(10, 10)
$form.FormBorderStyle = "Sizable"
$form.BackColor       = [System.Drawing.Color]::WhiteSmoke
$form.KeyPreview      = $true

# ============================================================
# 上部コントロールパネル
# ============================================================
$panel           = New-Object System.Windows.Forms.Panel
$panel.Size      = New-Object System.Drawing.Size(520, 95)
$panel.Location  = New-Object System.Drawing.Point(0, 0)
$panel.BackColor = [System.Drawing.Color]::White
$form.Controls.Add($panel)

# 試験項番ラベル
$lblTestItem          = New-Object System.Windows.Forms.Label
$lblTestItem.Text     = "試験項番:"
$lblTestItem.Location = New-Object System.Drawing.Point(10, 12)
$lblTestItem.Size     = New-Object System.Drawing.Size(65, 20)
$panel.Controls.Add($lblTestItem)

# 試験項番スピナー
$numTestItem          = New-Object System.Windows.Forms.NumericUpDown
$numTestItem.Location = New-Object System.Drawing.Point(78, 8)
$numTestItem.Size     = New-Object System.Drawing.Size(60, 25)
$numTestItem.Minimum  = 1
$numTestItem.Maximum  = 9999
$numTestItem.Value    = 1
$panel.Controls.Add($numTestItem)

$numTestItem.Add_ValueChanged({
    $script:testItem = [int]$numTestItem.Value
    $script:step    = 1
    $script:subStep = 1
    $numStep.Value  = 1
    # サムネイルパネルをクリア（画像リソースを解放）
    foreach ($ctrl in $thumbPanel.Controls) {
        foreach ($child in $ctrl.Controls) {
            if ($child -is [System.Windows.Forms.PictureBox] -and $child.Image -ne $null) {
                $child.Image.Dispose()
            }
        }
    }
    $thumbPanel.Controls.Clear()
    $script:thumbList  = @()
    $script:thumbIndex = -1
    # 新項番フォルダの既存画像を読み込む
    $testFolder = "$saveFolder\$($script:testItem)"
    if (Test-Path $testFolder) {
        Get-ChildItem "$testFolder\*.png" | Sort-Object Name | ForEach-Object {
            Add-Thumbnail $_.FullName ([System.IO.Path]::GetFileName($_.FullName))
        }
    }
    $lblStatus.Text = "待機中… 次: 手順 $($script:step)-$($script:subStep)"
})

# 手順番号ラベル
$lblStep          = New-Object System.Windows.Forms.Label
$lblStep.Text     = "手順番号:"
$lblStep.Location = New-Object System.Drawing.Point(10, 53)
$lblStep.Size     = New-Object System.Drawing.Size(65, 20)
$panel.Controls.Add($lblStep)

# 手順番号スピナー
$numStep          = New-Object System.Windows.Forms.NumericUpDown
$numStep.Location = New-Object System.Drawing.Point(78, 49)
$numStep.Size     = New-Object System.Drawing.Size(60, 25)
$numStep.Minimum  = 1
$numStep.Maximum  = 9999
$numStep.Value    = 1
$panel.Controls.Add($numStep)

$numStep.Add_ValueChanged({
    $script:step    = [int]$numStep.Value
    $script:subStep = 1
    $lblStatus.Text = "待機中… 次: 手順 $($script:step)-$($script:subStep)"
})

# 監視ON/OFFボタン
$btnToggle           = New-Object System.Windows.Forms.Button
$btnToggle.Text      = "⏸ 監視停止"
$btnToggle.Location  = New-Object System.Drawing.Point(155, 47)
$btnToggle.Size      = New-Object System.Drawing.Size(120, 30)
$btnToggle.BackColor = [System.Drawing.Color]::SteelBlue
$btnToggle.ForeColor = [System.Drawing.Color]::White
$btnToggle.FlatStyle = "Flat"
$panel.Controls.Add($btnToggle)

$btnToggle.Add_Click({
    if ($timer.Enabled) {
        $timer.Stop()
        $btnToggle.Text      = "▶ 監視再開"
        $btnToggle.BackColor = [System.Drawing.Color]::Gray
        $lblStatus.Text      = "⏸ 監視停止中"
    } else {
        $timer.Start()
        $btnToggle.Text      = "⏸ 監視停止"
        $btnToggle.BackColor = [System.Drawing.Color]::SteelBlue
        $lblStatus.Text      = "待機中… 次: 手順 $($script:step)-$($script:subStep)"
    }
})

# ステータスラベル
$lblStatus           = New-Object System.Windows.Forms.Label
$lblStatus.Text      = "待機中… 次: 手順 1-1"
$lblStatus.Location  = New-Object System.Drawing.Point(285, 53)
$lblStatus.Size      = New-Object System.Drawing.Size(220, 20)
$lblStatus.ForeColor = [System.Drawing.Color]::DarkBlue
$panel.Controls.Add($lblStatus)

# ============================================================
# ショートカットヒントバー
# ============================================================
$lblHint           = New-Object System.Windows.Forms.Label
$lblHint.Text      = "Ctrl+↑↓: 手順変更    サムネイルクリック / Enter: 拡大表示（←→で順送り）"
$lblHint.Location  = New-Object System.Drawing.Point(0, 97)
$lblHint.Size      = New-Object System.Drawing.Size(520, 18)
$lblHint.TextAlign = "MiddleCenter"
$lblHint.Font      = New-Object System.Drawing.Font("Arial", 7.5)
$lblHint.ForeColor = [System.Drawing.Color]::Gray
$lblHint.BackColor = [System.Drawing.Color]::White
$form.Controls.Add($lblHint)

# ============================================================
# サムネイルエリア
# ============================================================
$thumbPanel              = New-Object System.Windows.Forms.FlowLayoutPanel
$thumbPanel.Location     = New-Object System.Drawing.Point(0, 117)
$thumbPanel.Size         = New-Object System.Drawing.Size(504, 298)
$thumbPanel.Anchor       = "Top,Bottom,Left,Right"
$thumbPanel.AutoScroll   = $true
$thumbPanel.BackColor    = [System.Drawing.Color]::WhiteSmoke
$thumbPanel.Padding      = New-Object System.Windows.Forms.Padding(6)
$thumbPanel.WrapContents = $true
$form.Controls.Add($thumbPanel)

# ============================================================
# サムネイル選択ハイライト
# ============================================================
function Set-ThumbSelection($index) {
    for ($i = 0; $i -lt $thumbPanel.Controls.Count; $i++) {
        $c = $thumbPanel.Controls[$i]
        if ($i -eq $index) {
            $c.BackColor = [System.Drawing.Color]::SteelBlue
            $thumbPanel.ScrollControlIntoView($c)
        } else {
            $c.BackColor = [System.Drawing.Color]::White
        }
    }
    $script:thumbIndex = $index
}

# ============================================================
# 拡大表示ウィンドウ（←→で順送り対応）
# ============================================================
function Show-Viewer($startIndex) {
    $viewer               = New-Object System.Windows.Forms.Form
    $viewer.Size          = New-Object System.Drawing.Size(900, 660)
    $viewer.StartPosition = "CenterScreen"
    $viewer.BackColor     = [System.Drawing.Color]::Black
    $viewer.KeyPreview    = $true

    # ナビゲーションバー（先に追加してDock=Bottomで固定）
    $navPanel           = New-Object System.Windows.Forms.Panel
    $navPanel.Dock      = "Bottom"
    $navPanel.Height    = 40
    $navPanel.BackColor = [System.Drawing.Color]::FromArgb(40, 40, 40)
    $viewer.Controls.Add($navPanel)

    $btnPrev           = New-Object System.Windows.Forms.Button
    $btnPrev.Text      = "◀  前へ"
    $btnPrev.Size      = New-Object System.Drawing.Size(100, 30)
    $btnPrev.Location  = New-Object System.Drawing.Point(10, 5)
    $btnPrev.FlatStyle = "Flat"
    $btnPrev.ForeColor = [System.Drawing.Color]::White
    $btnPrev.BackColor = [System.Drawing.Color]::FromArgb(70, 70, 70)
    $navPanel.Controls.Add($btnPrev)

    $lblNavInfo           = New-Object System.Windows.Forms.Label
    $lblNavInfo.Size      = New-Object System.Drawing.Size(660, 30)
    $lblNavInfo.Location  = New-Object System.Drawing.Point(120, 5)
    $lblNavInfo.TextAlign = "MiddleCenter"
    $lblNavInfo.ForeColor = [System.Drawing.Color]::White
    $lblNavInfo.Font      = New-Object System.Drawing.Font("Arial", 9)
    $navPanel.Controls.Add($lblNavInfo)

    $btnNext           = New-Object System.Windows.Forms.Button
    $btnNext.Text      = "次へ  ▶"
    $btnNext.Size      = New-Object System.Drawing.Size(100, 30)
    $btnNext.Location  = New-Object System.Drawing.Point(788, 5)
    $btnNext.FlatStyle = "Flat"
    $btnNext.ForeColor = [System.Drawing.Color]::White
    $btnNext.BackColor = [System.Drawing.Color]::FromArgb(70, 70, 70)
    $navPanel.Controls.Add($btnNext)

    # 画像表示エリア（後に追加してDock=Fillで残りを埋める）
    $vpb           = New-Object System.Windows.Forms.PictureBox
    $vpb.Dock      = "Fill"
    $vpb.SizeMode  = "Zoom"
    $vpb.BackColor = [System.Drawing.Color]::Black
    $viewer.Controls.Add($vpb)

    # インデックスをハッシュテーブルで管理（クロージャ内で変更可能にする）
    $state = @{ index = $startIndex }

    $updateImage = {
        $total = $script:thumbList.Count
        if ($total -eq 0) { return }
        $path             = $script:thumbList[$state.index]
        $name             = [System.IO.Path]::GetFileName($path)
        $vpb.Image        = [System.Drawing.Image]::FromFile($path)
        $viewer.Text      = $name
        $lblNavInfo.Text  = "$name    ($($state.index + 1) / $total)"
        $btnPrev.Enabled  = ($state.index -gt 0)
        $btnNext.Enabled  = ($state.index -lt $total - 1)
        Set-ThumbSelection $state.index
    }
    & $updateImage

    $btnPrev.Add_Click({
        if ($state.index -gt 0) {
            $state.index--
            & $updateImage
        }
    })

    $btnNext.Add_Click({
        if ($state.index -lt $script:thumbList.Count - 1) {
            $state.index++
            & $updateImage
        }
    })

    $viewer.Add_KeyDown({
        param($s, $e)
        if ($e.KeyCode -eq "Left" -and $state.index -gt 0) {
            $state.index--
            & $updateImage
            $e.Handled = $true
        }
        if ($e.KeyCode -eq "Right" -and $state.index -lt $script:thumbList.Count - 1) {
            $state.index++
            & $updateImage
            $e.Handled = $true
        }
        if ($e.KeyCode -eq "Escape") {
            $viewer.Close()
        }
    })

    $viewer.ShowDialog()
}

# ============================================================
# サムネイル追加
# ============================================================
function Add-Thumbnail($imagePath, $label) {
    $idx = $thumbPanel.Controls.Count

    $container           = New-Object System.Windows.Forms.Panel
    $container.Size      = New-Object System.Drawing.Size(110, 105)
    $container.Margin    = New-Object System.Windows.Forms.Padding(4)
    $container.BackColor = [System.Drawing.Color]::White
    $container.Cursor    = "Hand"
    $container.Tag       = $idx

    $pb          = New-Object System.Windows.Forms.PictureBox
    $pb.Size     = New-Object System.Drawing.Size(100, 80)
    $pb.Location = New-Object System.Drawing.Point(5, 4)
    $pb.SizeMode = "Zoom"
    $pb.Image    = [System.Drawing.Image]::FromFile($imagePath)
    $pb.Cursor   = "Hand"
    $pb.Tag      = $idx
    $container.Controls.Add($pb)

    $lbl           = New-Object System.Windows.Forms.Label
    $lbl.Text      = $label
    $lbl.Location  = New-Object System.Drawing.Point(2, 86)
    $lbl.Size      = New-Object System.Drawing.Size(106, 16)
    $lbl.TextAlign = "MiddleCenter"
    $lbl.Font      = New-Object System.Drawing.Font("Arial", 7.5)
    $lbl.Tag       = $idx
    $container.Controls.Add($lbl)

    $clickAction = {
        $tag = $this.Tag
        if ($tag -eq $null) { $tag = $this.Parent.Tag }
        Show-Viewer ([int]$tag)
    }
    $pb.Add_Click($clickAction)
    $container.Add_Click($clickAction)

    $script:thumbList += $imagePath
    $thumbPanel.Controls.Add($container)
    Set-ThumbSelection $idx
}

# ============================================================
# メインウィンドウ キーボードショートカット
# ============================================================
$form.Add_KeyDown({
    param($s, $e)

    # Ctrl+↑ : 手順番号 +1
    if ($e.Control -and $e.KeyCode -eq "Up") {
        if ($numStep.Value -lt $numStep.Maximum) { $numStep.Value++ }
        $e.Handled = $true
        $e.SuppressKeyPress = $true
    }
    # Ctrl+↓ : 手順番号 -1
    if ($e.Control -and $e.KeyCode -eq "Down") {
        if ($numStep.Value -gt $numStep.Minimum) { $numStep.Value-- }
        $e.Handled = $true
        $e.SuppressKeyPress = $true
    }
    # Enter : 選択中サムネイルを拡大表示
    if ($e.KeyCode -eq "Return" -and $script:thumbIndex -ge 0) {
        Show-Viewer $script:thumbIndex
        $e.Handled = $true
        $e.SuppressKeyPress = $true
    }
})

# ============================================================
# クリップボード監視タイマー（500ms間隔）
# ============================================================
$timer          = New-Object System.Windows.Forms.Timer
$timer.Interval = 500

$timer.Add_Tick({
    # シーケンス番号が変わっていなければスキップ
    $seq = [ClipboardHelper]::GetClipboardSequenceNumber()
    if ($seq -eq $script:lastSeq) { return }
    $script:lastSeq = $seq

    # 画像以外のコピー（テキストなど）は無視
    $img = [System.Windows.Forms.Clipboard]::GetImage()
    if ($img -eq $null) { return }

    # 保存
    $script:step     = [int]$numStep.Value
    $script:testItem = [int]$numTestItem.Value
    $testFolder      = "$saveFolder\$($script:testItem)"
    if (!(Test-Path $testFolder)) { New-Item -ItemType Directory -Path $testFolder | Out-Null }
    $fileName        = "$($script:step)-$($script:subStep).png"
    $filePath        = "$testFolder\$fileName"
    $img.Save($filePath)
    Add-Thumbnail $filePath $fileName
    $script:subStep++
    $lblStatus.Text = "✅ $fileName 保存済  → 次: $($script:step)-$($script:subStep)"
})

$timer.Start()
$form.Add_FormClosed({ $timer.Stop() })
$form.ShowDialog()