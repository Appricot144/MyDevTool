import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DailyNoteParser {
    private static final Pattern TASK_PATTERN = Pattern.compile("^(\\s*)- \\[([x ])\\] (.+)$");
    private static final Pattern TODO_SECTION_PATTERN = Pattern.compile("^## todo$");
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("^### (.+)$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<Task> parseNote(Path filePath) throws IOException {
        List<Task> tasks = new ArrayList<>();
        String content = Files.readString(filePath);
        String[] lines = content.split("\n");
        
        String currentCategory = "";
        LocalDate noteDate = extractDateFromFileName(filePath.getFileName().toString());
        boolean inTodoSection = false;

        for (String line : lines) {
            String originalLine = line;
            line = line.trim().toLowerCase();
            
            if (TODO_SECTION_PATTERN.matcher(line).find()) {
                inTodoSection = true;
                continue;
            }
            
            if (line.startsWith("## ") && !line.equals("## todo")) {
                inTodoSection = false;
                continue;
            }
            
            if (!inTodoSection) {
                continue;
            }

            Matcher categoryMatcher = CATEGORY_PATTERN.matcher(originalLine);
            if (categoryMatcher.find()) {
                currentCategory = categoryMatcher.group(1);
                continue;
            }

            Matcher taskMatcher = TASK_PATTERN.matcher(originalLine);
            if (taskMatcher.find()) {
                String indentStr = taskMatcher.group(1);
                int indentLevel = calculateIndentLevel(indentStr);
                boolean isComplete = taskMatcher.group(2).equals("x");
                String title = taskMatcher.group(3);
                tasks.add(new Task(title, isComplete, noteDate, currentCategory, indentLevel));
            }
        }

        return tasks;
    }

    private LocalDate extractDateFromFileName(String fileName) {
        String dateStr = fileName.substring(0, 10);
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }
    
    private int calculateIndentLevel(String indentStr) {
        if (indentStr.isEmpty()) {
            return 0;
        }
        
        int level = 0;
        int i = 0;
        while (i < indentStr.length()) {
            if (indentStr.charAt(i) == '\t') {
                level++;
                i++;
            } else if (i + 1 < indentStr.length() && indentStr.charAt(i) == ' ' && indentStr.charAt(i + 1) == ' ') {
                level++;
                i += 2;
            } else {
                break;
            }
        }
        
        return Math.min(level, 3);
    }
}