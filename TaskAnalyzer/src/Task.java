import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.function.Function;

/**
 * daily note の Taskを表すクラス</br>
 * 
 * ステートフルなクラス。</br>
 * first appearance は基本的にnullであり、全てのファイルの解析が完了した時にfirst appearance date がsetされることを期待している。</br>
 * 
 * 同一タスクとみなす条件は `title` `category` `first appearance date` `indent level` が同一であるタスク。</br>
 * タスクの完了状況、タスクが記録されているファイルの日付 にかかわらず、同一カテゴリ内の名前、最初に出現した日付、どのタスクの子であるかを判定して同一とみなす。</br>
 * `Stream.distinct()` による重複削除を期待している。</br>
 */
public class Task {
    private String title;
    private boolean completed;
    private LocalDate date;
    private String category;
    private int indentLevel;
    private LocalDate firstAppearanceDate;

    public Task(String title, boolean completed, LocalDate date, String category, int indentLevel) {
        this.title = title;
        this.completed = completed;
        this.date = date;
        this.category = category;
        this.indentLevel = indentLevel;
    }

    @Override
    public String toString() {
        String indentStr = "";
        for (int i = 0; i < indentLevel; i++) {
            indentStr = indentStr + "  ";
        }

        String firstAppearanceDateStr = firstAppearanceDate == null ? "null" : firstAppearanceDate.toString();
        return String.format("%s[%s-%s] %s [%s] - %s", indentStr, firstAppearanceDateStr, date.toString(), title, completed ? "x" : " ", category);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !((obj instanceof Task))) return false;

        
        Task task = (Task) obj;
        
        boolean isSamefirstAppearance = true;
        if(this.getFirstAppearanceDate() == null){
            isSamefirstAppearance = this.firstAppearanceDate == null;
        } else {
            isSamefirstAppearance = this.firstAppearanceDate.equals(task.getFirstAppearanceDate());
        }

        return this.title.equals(task.getTitle())
        && this.category.equals(task.getCategory())
        && this.indentLevel == task.getIndentLevel()
        && isSamefirstAppearance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, firstAppearanceDate, category, indentLevel);
    }

    // Getters
    public String getTitle() { return title; }
    public boolean isCompleted() { return completed; }
    public LocalDate getDate() { return date; }
    public String getCategory() { return category; }
    public int getIndentLevel() { return indentLevel; }
    public LocalDate getFirstAppearanceDate() { return firstAppearanceDate; }
    
    // Setter
    public void setFirstAppearanceDate(LocalDate firstDate) {
        this.firstAppearanceDate = firstDate;
    }

    public String getPrintString() {
        return String.format("%s : [%s] %s", firstAppearanceDate, (completed ? "x" : " "), title);
    }

    /**
     * 独自の条件で重複削除をする
     * @param <T>
     * @param keyExtractor
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
} 