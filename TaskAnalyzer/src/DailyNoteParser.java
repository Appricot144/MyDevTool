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
    private static final Pattern TASK_PATTERN = Pattern.compile("^- \\[ \\] (.+)$");
    private static final Pattern TODO_SECTION_PATTERN = Pattern.compile("^## Todo$");
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
            line = line.trim();
            
            if (TODO_SECTION_PATTERN.matcher(line).find()) {
                inTodoSection = true;
                continue;
            }
            
            if (line.startsWith("## ") && !line.equals("## Todo")) {
                inTodoSection = false;
                continue;
            }
            
            if (!inTodoSection) {
                continue;
            }

            Matcher categoryMatcher = CATEGORY_PATTERN.matcher(line);
            if (categoryMatcher.find()) {
                currentCategory = categoryMatcher.group(1);
                continue;
            }

            Matcher taskMatcher = TASK_PATTERN.matcher(line);
            if (taskMatcher.find()) {
                String title = taskMatcher.group(1);
                tasks.add(new Task(title, false, noteDate, currentCategory));
            }
        }

        return tasks;
    }

    private LocalDate extractDateFromFileName(String fileName) {
        String dateStr = fileName.substring(0, 10);
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }
}