import java.time.LocalDate;

public class Task {
    private String title;
    private boolean completed;
    private LocalDate date;
    private String category;
    private int indentLevel;
    private LocalDate firstAppearanceDate;

    public Task(String title, boolean completed, LocalDate date, String category) {
        this.title = title;
        this.completed = completed;
        this.date = date;
        this.category = category;
        this.indentLevel = 0;
    }

    public Task(String title, boolean completed, LocalDate date, String category, int indentLevel) {
        this.title = title;
        this.completed = completed;
        this.date = date;
        this.category = category;
        this.indentLevel = indentLevel;
    }

    // Getters
    public String getTitle() { return title; }
    public boolean isCompleted() { return completed; }
    public LocalDate getDate() { return date; }
    public String getCategory() { return category; }
    public int getIndentLevel() { return indentLevel; }
    public LocalDate getFirstAppearanceDate() { return firstAppearanceDate; }
    
    // Setter
    public void setFirstAppearanceDate(LocalDate firstAppearanceDate) {
        this.firstAppearanceDate = firstAppearanceDate;
    }

    @Override
    public String toString() {
        return String.format("%s [%s] - %s", title, completed ? "完了" : "未完了", category);
    }
} 