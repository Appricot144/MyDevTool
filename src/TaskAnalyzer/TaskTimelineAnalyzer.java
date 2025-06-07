package TaskAnalyzer;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TaskTimelineAnalyzer {
    private final List<Task> allTasks;

    public TaskTimelineAnalyzer(List<Task> tasks) {
        this.allTasks = tasks;
    }

    public Map<String, Map<LocalDate, Integer>> getCategoryTaskCountByDate() {
        Map<String, Map<LocalDate, Integer>> result = new TreeMap<>();
        
        // カテゴリごとにタスクをグループ化
        Map<String, List<Task>> tasksByCategory = allTasks.stream()
            .collect(Collectors.groupingBy(Task::getCategory));

        // 各カテゴリについて日付ごとのタスク数を計算
        for (Map.Entry<String, List<Task>> entry : tasksByCategory.entrySet()) {
            String category = entry.getKey();
            List<Task> tasks = entry.getValue();

            Map<LocalDate, Integer> dateCount = tasks.stream()
                .collect(Collectors.groupingBy(
                    Task::getDate,
                    TreeMap::new,
                    Collectors.collectingAndThen(Collectors.toList(), List::size)
                ));

            result.put(category, dateCount);
        }

        return result;
    }

    public void printTimelineReport() {
        Map<String, Map<LocalDate, Integer>> timeline = getCategoryTaskCountByDate();
        
        System.out.println("カテゴリ別タスク推移レポート");
        System.out.println("==========================");

        for (Map.Entry<String, Map<LocalDate, Integer>> categoryEntry : timeline.entrySet()) {
            String category = categoryEntry.getKey();
            Map<LocalDate, Integer> dateCount = categoryEntry.getValue();

            System.out.println("\nカテゴリ: " + category);
            System.out.println("日付\t\tタスク数\t増減");

            LocalDate prevDate = null;
            int prevCount = 0;

            for (Map.Entry<LocalDate, Integer> dateEntry : dateCount.entrySet()) {
                LocalDate date = dateEntry.getKey();
                int count = dateEntry.getValue();
                
                String change = "";
                if (prevDate != null) {
                    int diff = count - prevCount;
                    change = diff > 0 ? "+" + diff : String.valueOf(diff);
                }

                System.out.printf("%s\t%d\t%s%n", date, count, change);
                
                prevDate = date;
                prevCount = count;
            }
        }
    }
} 