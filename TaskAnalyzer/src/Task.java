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
 * first appearance は基本的にnullであり、全てのファイルの解析が完了した時にfirst appearance date
 * がsetされることを期待している。</br>
 * 
 * 同一タスクとみなす条件は `title` `category` `first appearance date` `indent level`
 * が同一であるタスク。</br>
 * タスクの完了状況、タスクが記録されているファイルの日付
 * にかかわらず、同一カテゴリ内の名前、最初に出現した日付、どのタスクの子であるかを判定して同一とみなす。</br>
 * `Stream.distinct()` による重複削除を期待している。</br>
 */
public class Task {
    private String title;
    private boolean completed;
    private LocalDate date;
    private String category;
    private int indentLevel;

    // state full section !
    // after inserted member s
    private LocalDate firstAppearanceDate;
    private String colorStr = "";

    // color end
    public static final String END = "\u001b[00m";

    public Task(String title, boolean completed, LocalDate date, String category, int indentLevel) {
        this.title = title.trim();
        this.completed = completed;
        this.date = date;
        this.category = category.trim();
        this.indentLevel = indentLevel;
    }

    @Override
    public String toString() {
        String indentStr = "";
        for (int i = 0; i < indentLevel; i++) {
            indentStr = indentStr + "  ";
        }

        String firstAppearanceDateStr = firstAppearanceDate == null ? "null" : firstAppearanceDate.toString();
        return String.format("%s[%s-%s] %s [%s] - %s", indentStr, firstAppearanceDateStr, date.toString(), title,
                completed ? "x" : " ", category);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || !((obj instanceof Task)))
            return false;

        Task task = (Task) obj;

        boolean isSamefirstAppearance = true;
        if (this.getFirstAppearanceDate() == null) {
            isSamefirstAppearance = this.firstAppearanceDate == null;
        } else {
            isSamefirstAppearance = this.firstAppearanceDate.equals(task.getFirstAppearanceDate());
        }

        boolean isSameTitle = this.title.replaceAll("(^~~|~~$)", "")
                .equals(task.getTitle().replaceAll("(^~~|~~$)", ""));

        return isSameTitle
                && this.category.equals(task.getCategory())
                && this.indentLevel == task.getIndentLevel()
                && isSamefirstAppearance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, firstAppearanceDate, category, indentLevel);
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public int getIndentLevel() {
        return indentLevel;
    }

    public LocalDate getFirstAppearanceDate() {
        return firstAppearanceDate;
    }

    public String getColor() {
        return colorStr;
    }

    // Setter
    public void setFirstAppearanceDate(LocalDate firstDate) {
        this.firstAppearanceDate = firstDate;
    }

    public void setColor(TaskBgColor color) {
        this.colorStr = color.getColor();
    }

    public String getPrintString() {
        return String.format("[%s] : [%s] %s", firstAppearanceDate, (completed ? "x" : " "), title);
    }

    /**
     * 独自の条件で重複削除をする
     * 
     * @param <T>
     * @param keyExtractor
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public enum TaskBgColor {
        ANSI_RED("\u001b[41m"),
        ANSI_GREEN("\u001b[42m"),
        ANSI_YELLOW("\u001b[43m");

        private final String colorCode;

        private TaskBgColor(String colorCode) {
            this.colorCode = colorCode;
        }

        public String getColor() {
            return colorCode;
        }
    }
}