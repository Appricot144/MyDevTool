package TaskAnalyzer;

import java.time.LocalDate;

public class Task {
    private String title;
    private boolean completed;
    private LocalDate date;
    private String category;

    public Task(String title, boolean completed, LocalDate date, String category) {
        this.title = title;
        this.completed = completed;
        this.date = date;
        this.category = category;
    }

    // Getters
    public String getTitle() { return title; }
    public boolean isCompleted() { return completed; }
    public LocalDate getDate() { return date; }
    public String getCategory() { return category; }

    @Override
    public String toString() {
        return String.format("%s [%s] - %s", title, completed ? "完了" : "未完了", category);
    }
} 