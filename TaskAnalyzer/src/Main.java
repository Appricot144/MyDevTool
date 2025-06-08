import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar <jar> <DirectoryPath>");
            return;
        }

        String directoryPath = args[0];
        Path dir = Paths.get(directoryPath);

        try {
            List<Task> allTasks = new ArrayList<>();
            DailyNoteParser parser = new DailyNoteParser();

            // ディレクトリ内のすべての.mdファイルを処理
            Files.list(dir)
                .filter(path -> path.toString().endsWith(".md"))
                .forEach(path -> {
                    try {
                        allTasks.addAll(parser.parseNote(path));
                    } catch (IOException e) {
                        System.err.println("Error: Could't parse md file: " + path);
                        e.printStackTrace();
                    }
                });

            // 時系列分析を実行
            TaskTimelineAnalyzer analyzer = new TaskTimelineAnalyzer(allTasks);
            analyzer.printTimelineReport();

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 