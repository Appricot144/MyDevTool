# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java-based task analyzer that parses markdown files containing daily notes with tasks and generates timeline reports. The tool analyzes task completion patterns across different categories over time.

## Build and Run Commands

```bash
# Compile the project
mvn compile

# Build JAR with dependencies
mvn package

# Run the application (requires directory path with .md files)
java -jar target/task-analyzer-1.0-SNAPSHOT-jar-with-dependencies.jar /path/to/markdown/files
```

## Architecture

The application follows a simple pipeline architecture:

1. **Main.java**: Entry point that processes command line arguments and coordinates the analysis
2. **DailyNoteParser.java**: Parses markdown files to extract tasks using regex patterns
   - Expects markdown files named in `yyyy-MM-dd.md` format
   - Recognizes tasks in format `- [x] task title` (completed) or `- [ ] task title` (incomplete)
   - Groups tasks under `## Category Name` headers
   - Supports up to 3 levels of task hierarchy using indentation
3. **Task.java**: Data model representing a single task with title, completion status, date, category, and indent level
   - Tasks are considered identical based on title, category, first appearance date, and indent level
   - Contains logic for duplicate removal using Stream.distinct() 
4. **TaskOrderedTreeManager.java**: Manages ordered tree structures for hierarchical task representation
   - **TaskOrderedTree**: Inner class representing tree nodes with task data and children
   - **buildOrderedTree()**: Constructs ordered trees from task lists while preserving file appearance order
   - **mergeOrderedTrees()**: Merges multiple trees by date, updating completion status and adding new nodes
   - **printOrderedTree()**: Outputs hierarchical task structure with visual tree formatting (●, ├─, └─)
5. **TaskTimelineAnalyzer.java**: Analyzes tasks to generate timeline reports
   - Generates task count changes by category over time
   - Produces two types of sprint summaries: traditional and hierarchical (ordered tree version)
   - Merges duplicate tasks and tracks first appearance dates

## Input Format

The parser expects markdown files with:

- Filename format: `yyyy-MM-dd.md`
- Task format: `- [x]` for completed, `- [ ]` for incomplete
- Category headers: `## Category Name`

## Output Format

The application generates two main types of reports:

### 1. Task Count Timeline Report
Shows task count changes by category over time:
```
カテゴリ: My Dev Tools
日付            タスク数        増減
2025-06-14      3
2025-06-15      8       +5
2025-06-18      8       0
```

### 2. Sprint Summary Reports
Two versions are generated:

**Traditional Sprint Summary**: Flat list of tasks grouped by category

**Hierarchical Sprint Summary (Ordered Tree)**: Tree structure preserving parent-child relationships:
```
[My Dev Tools] (8件)
  ●  2025-06-14 : [ ] 影響範囲検出器の実装
  └─ 2025-06-18 : [x] 機能整理
  ●  2025-06-14 : [ ] task analyzer を作る
  ├─ 2025-06-18 : [x] 最親ノードを小カテゴリにする
  ├─ 2025-06-15 : [ ] スプリット間に出たタスク全体の達成推移
  ├─ 2025-06-18 : [x] mdファイルの構成の変更
  ├─ 2025-06-18 : [x] 完了タスクがスプリットサマリにカウントされてない
  └─ 2025-06-18 : [x] スプリット間に出たタスクの推移
```

Legend:
- `●` : Root/parent task
- `├─` : Child task (not last)
- `└─` : Child task (last)
- `[x]` : Completed task
- `[ ]` : Incomplete task

## Dependencies

- Java 21
- Apache Commons IO 2.15.1
- Maven for build management
