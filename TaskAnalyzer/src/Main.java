import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar <jar> [-d] <DirectoryPath>");
            return;
        }

        boolean debugMode = false;
        boolean mode = false;
        String directoryPath;

        // コマンドライン引数の解析
        if (args.length >= 2 && Arrays.asList(args).contains("--experimental-mode")) {
            mode = true;
        }

        if (args.length >= 2 && args[0].equals("-d")) {
            debugMode = true;
            directoryPath = args[1];
        } else if (args.length >= 2 && args[1].equals("-d")) {
            debugMode = true;
            directoryPath = args[0];
        } else {
            directoryPath = args[0];
        }
        Path dir = Paths.get(directoryPath);

        try {
            List<Task> allTasks = new ArrayList<>();
            List<Memo> allMemos = new ArrayList<>();
            DailyNoteParser parser = new DailyNoteParser();

            // ディレクトリ内のすべての.mdファイルを処理
            Files.list(dir)
                    .filter(path -> path.toString().endsWith(".md"))
                    .forEach(path -> {
                        try {
                            DailyNoteParser.ParseResult result = parser.parseNote(path);
                            allTasks.addAll(result.getTasks());
                            allMemos.add(result.getMemo());
                        } catch (IOException e) {
                            System.err.println("Error: Could't parse md file: " + path);
                            e.printStackTrace();
                        }
                    });

            TaskTimelineAnalyzer analyzer = new TaskTimelineAnalyzer(allTasks, debugMode, mode);
            analyzer.printTimelineReport();
            // MemoPrinter printer = new MemoPrinter();
            // printer.printAllMemo(allMemos);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}