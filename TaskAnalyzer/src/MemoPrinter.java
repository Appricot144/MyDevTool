import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

import java.util.stream.Collectors;

public class MemoPrinter {

    public void printAllMemo(List<Memo> allMemos) {
        // 日ごとのメモに分割
        Map<LocalDate, List<String>> dailyMemo = allMemos.stream()
                .sorted(Comparator.comparing(Memo::getDate))
                .collect(Collectors.toMap(Memo::getDate, Memo::getLines));

        List<Memo> uniqueMemos = allMemos.stream().distinct().toList();

        // 出力
        System.out.println("Sprint Memos...");
        for (List<String> dailyLines : uniqueMemos.stream().map(m -> m.getLines()).toList()) {
            dailyLines.stream().forEach(line -> {
                System.out.println(line);
            });
            System.out.println("---");
        }
    }
}
