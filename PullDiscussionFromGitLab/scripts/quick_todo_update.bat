@echo off
chcp 65001 > nul
title GitLab → TODO.md 更新

echo.
echo ===============================================
echo   🚀 GitLab Discussion → TODO.md 更新
echo ===============================================
echo.

REM 設定値（ここを環境に合わせて変更）
set "GITLAB_URL=https://your-gitlab.com"
set "ACCESS_TOKEN=your-access-token-here"
set "TODO_DIR=%~dp0"
set "SCRIPT_PATH=%~dp0scripts\GitLab-To-Todo.ps1"

echo 📋 設定:
echo   GitLab URL: %GITLAB_URL%
echo   TODO Directory: %TODO_DIR%
echo   Script: %SCRIPT_PATH%
echo.

REM スクリプトの存在確認
if not exist "%SCRIPT_PATH%" (
    echo ❌ スクリプトが見つかりません: %SCRIPT_PATH%
    echo    scripts フォルダに GitLab-To-Todo.ps1 を配置してください
    pause
    exit /b 1
)

echo 🔄 実行中...
echo.

powershell.exe -ExecutionPolicy Bypass -File "%SCRIPT_PATH%" -GitLabUrl "%GITLAB_URL%" -AccessToken "%ACCESS_TOKEN%" -TodoDirectory "%TODO_DIR%" -AllProjects -RecentOnly -RecentDays 7

echo.
if %ERRORLEVEL% EQU 0 (
    echo ✅ 更新完了!
    echo 📂 TODO ファイル: %TODO_DIR%\%date:~0,4%-%date:~5,2%-%date:~8,2%.md
) else (
    echo ❌ エラーが発生しました
)

echo.
echo Press any key to close...
pause > nul