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
3. **Task.java**: Data model representing a single task with title, completion status, date, and category
4. **TaskTimelineAnalyzer.java**: Analyzes tasks to generate timeline reports showing task count changes by category over time

## Input Format

The parser expects markdown files with:

- Filename format: `yyyy-MM-dd.md`
- Task format: `- [x]` for completed, `- [ ]` for incomplete
- Category headers: `## Category Name`

## Dependencies

- Java 21
- Apache Commons IO 2.15.1
- Maven for build management
