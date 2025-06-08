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
    private static final Pattern TASK_PATTERN = Pattern.compile("- \\[(x)?\\] (.+)");
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("^## (.+)$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<Task> parseNote(Path filePath) throws IOException {
        List<Task> tasks = new ArrayList<>();
        String content = Files.readString(filePath);
        String[] lines = content.split("\n");
        
        String currentCategory = "";
        LocalDate noteDate = extractDateFromFileName(filePath.getFileName().toString());

        for (String line : lines) {
            Matcher categoryMatcher = CATEGORY_PATTERN.matcher(line);
            if (categoryMatcher.find()) {
                currentCategory = categoryMatcher.group(1);
                continue;
            }

            Matcher taskMatcher = TASK_PATTERN.matcher(line);
            if (taskMatcher.find()) {
                boolean completed = taskMatcher.group(1) != null;
                String title = taskMatcher.group(2);
                tasks.add(new Task(title, completed, noteDate, currentCategory));
            }
        }

        return tasks;
    }

    private LocalDate extractDateFromFileName(String fileName) {
        String dateStr = fileName.substring(0, 10);
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }
}