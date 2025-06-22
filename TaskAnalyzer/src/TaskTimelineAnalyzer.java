import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TaskTimelineAnalyzer {
    private final List<Task> allTasks;
    private final boolean debugMode;

    public TaskTimelineAnalyzer(List<Task> tasks) {
        this.allTasks = tasks;
        this.debugMode = false;
    }
    
    public TaskTimelineAnalyzer(List<Task> tasks, boolean debugMode) {
        this.allTasks = tasks;
        this.debugMode = debugMode;
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

    public Map<String, Set<String>> getUniqueTasksByCategory() {
        Map<String, Set<String>> result = new TreeMap<>();
        
        Map<String, List<Task>> tasksByCategory = allTasks.stream()
            .collect(Collectors.groupingBy(Task::getCategory));

        for (Map.Entry<String, List<Task>> entry : tasksByCategory.entrySet()) {
            String category = entry.getKey();
            List<Task> tasks = entry.getValue();
            
            Set<String> uniqueTasks = tasks.stream()
                .map(task -> task.getTitle().toLowerCase())
                .collect(Collectors.toSet());
            
            result.put(category, uniqueTasks);
        }
        
        return result;
    }

    public int getTotalUniqueTaskCount() {
        return allTasks.stream()
            .distinct()
            .collect(Collectors.toList())
            .size();
    }

    public void printTimelineReport() {
        printTaskNumWandering();
        printOrderedSprintSummary();
    }

    public void printTaskNumWandering() {
        Map<String, Map<LocalDate, Integer>> timeline = getCategoryTaskCountByDate();
        
        System.out.println("カテゴリ別タスク数推移レポート");
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
    
    public void printOrderedSprintSummary() {
        System.out.println("\n\nスプリントサマリ");
        System.out.println("================");
        
        Map<String, List<Task>> tasksByCategory = allTasks.stream()
            .collect(Collectors.groupingBy(Task::getCategory));
        
        for (Map.Entry<String, List<Task>> entry : tasksByCategory.entrySet()) {
            String category = entry.getKey();
            List<Task> tasks = entry.getValue();
            
            List<Task> mergedTasks = mergeTasks(tasks);
            
            Map<LocalDate, List<Task>> tasksByDate = tasks.stream()
                .collect(Collectors.groupingBy(Task::getDate, TreeMap::new, Collectors.toList()));
            
            TaskOrderedTreeManager.TaskOrderedTree categoryTree = null;
            
            for (Map.Entry<LocalDate, List<Task>> dateEntry : tasksByDate.entrySet()) {
                List<Task> dailyTasks = dateEntry.getValue();
                
                TaskOrderedTreeManager.TaskOrderedTree dailyTree = TaskOrderedTreeManager.buildOrderedTree(dailyTasks);
                if (debugMode) {
                    System.out.println("\nDebug: Date:" + dateEntry.getKey());
                    TaskOrderedTreeManager.printOrderedTree(dailyTree, " ");
                }
                categoryTree = TaskOrderedTreeManager.mergeOrderedTrees(categoryTree, dailyTree);
            }
            
            System.out.println("\n[" + category + "] (" + mergedTasks.size() + "件)");
            
            if (categoryTree != null) {
                TaskOrderedTreeManager.printOrderedTree(categoryTree, "  ");
            }
        }
    }
    
    
    /**
     * スプリント内で発生したタスクを重複を除外し、カテゴリ内で発生したタスクの総体を返す
     * 副作用:解析結果に first appearance date を付ける
     * 
     * TODO ファイル内での位置を保持する
     */
    private List<Task> mergeTasks(List<Task> tasks) {
        List<Task> result = new ArrayList<>();
        List<Task> uniqueTasks = tasks.stream()
            .filter(Task.distinctByKey(task -> 
                task.getCategory() + ";" + task.getIndentLevel() + ";" + task.getTitle()
                ))
            .toList();

        for (Task aTask : uniqueTasks) {
            List<Task> sameTasks = tasks.stream()
                .filter(task -> task.getTitle().equals(aTask.getTitle()) && task.getIndentLevel() == aTask.getIndentLevel())
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .toList();

            Task firstAppearance = sameTasks.getFirst();
            Task finalStateTask = sameTasks.getLast();
            finalStateTask.setFirstAppearanceDate(firstAppearance.getDate());
            
            result.add(finalStateTask);
        }

        // DEBUG
        if (debugMode) {
            System.out.println("Debug: Task merge:");
            result.stream().forEach(t -> {
                System.out.println(t.toString());
            });
        }
        
        return result;
    }
    
    
} 