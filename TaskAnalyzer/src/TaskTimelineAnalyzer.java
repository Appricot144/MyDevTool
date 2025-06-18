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
            .map(task -> task.getTitle().toLowerCase())
            .collect(Collectors.toSet())
            .size();
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
        
        printSprintSummary();
    }

    public void printSprintSummary() {
        System.out.println("\n\nスプリントサマリ");
        System.out.println("================");
        
        int totalUniqueTasks = getTotalUniqueTaskCount();
        System.out.println("総タスク数: " + totalUniqueTasks);
        
        System.out.println("\nカテゴリ別タスク一覧:");
        Map<String, List<Task>> tasksByCategory = allTasks.stream()
            .collect(Collectors.groupingBy(Task::getCategory));
        
        for (Map.Entry<String, List<Task>> entry : tasksByCategory.entrySet()) {
            String category = entry.getKey();
            List<Task> tasks = entry.getValue();
            
            // 日付順にソート
            tasks.sort((a, b) -> a.getDate().compareTo(b.getDate()));
            
            // 前日の差分を計算
            List<Task> dailyDifferences = calculateDailyDifferences(tasks);
            
            System.out.println("\n[" + category + "] (" + dailyDifferences.size() + "件)");
            
            printHierarchicalTasks(dailyDifferences);
        }
    }
    
    private List<Task> calculateDailyDifferences(List<Task> tasks) {
        List<Task> result = new ArrayList<>();
        Map<LocalDate, Set<String>> tasksByDate = new TreeMap<>();
        
        // 日付ごとにタスクタイトルをグループ化
        for (Task task : tasks) {
            tasksByDate.computeIfAbsent(task.getDate(), k -> new TreeSet<>())
                .add(task.getTitle().toLowerCase());
        }
        
        Set<String> previousDayTasks = new TreeSet<>();
        
        for (Map.Entry<LocalDate, Set<String>> entry : tasksByDate.entrySet()) {
            LocalDate date = entry.getKey();
            Set<String> currentDayTasks = entry.getValue();
            
            // 前日にはなかった新しいタスクのみを追加
            for (String taskTitle : currentDayTasks) {
                if (!previousDayTasks.contains(taskTitle)) {
                    // スプリント全体で最も完了状態のタスクを探す
                    Task bestTask = tasks.stream()
                        .filter(t -> t.getTitle().toLowerCase().equals(taskTitle))
                        .max((a, b) -> {
                            // 完了タスクを優先し、同じ完了状態なら最新の日付を優先
                            if (a.isCompleted() != b.isCompleted()) {
                                return a.isCompleted() ? 1 : -1;
                            }
                            return a.getDate().compareTo(b.getDate());
                        })
                        .orElse(null);
                    
                    if (bestTask != null) {
                        // 新しいタスクオブジェクトを作成（初回出現日付を保持）
                        Task firstAppearance = tasks.stream()
                            .filter(t -> t.getTitle().toLowerCase().equals(taskTitle))
                            .min((a, b) -> a.getDate().compareTo(b.getDate()))
                            .orElse(bestTask);
                        
                        Task taskWithCompletionStatus = new Task(
                            bestTask.getTitle(), 
                            bestTask.isCompleted(), 
                            firstAppearance.getDate(), 
                            bestTask.getCategory(),
                            firstAppearance.getIndentLevel()
                        );
                        result.add(taskWithCompletionStatus);
                    }
                }
            }
            
            previousDayTasks = new TreeSet<>(currentDayTasks);
        }
        
        return result;
    }
    
    private void printHierarchicalTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return;
        }
        
        // デバッグ用: インデントレベルを確認
        if (debugMode) {
            System.out.println("  Debug: Task indent levels:");
            for (Task task : tasks) {
                System.out.println("    " + task.getTitle() + " -> Level: " + task.getIndentLevel());
            }
        }
        
        // インデントレベル0のタスクでグループ化
        List<TaskHierarchy> hierarchies = buildHierarchies(tasks);
        
        for (TaskHierarchy hierarchy : hierarchies) {
            printTaskHierarchy(hierarchy, "");
        }
    }
    
    private List<TaskHierarchy> buildHierarchies(List<Task> tasks) {
        List<TaskHierarchy> hierarchies = new ArrayList<>();
        TaskHierarchy currentHierarchy = null;
        
        for (Task task : tasks) {
            if (task.getIndentLevel() == 0) {
                currentHierarchy = new TaskHierarchy(task);
                hierarchies.add(currentHierarchy);
            } else if (currentHierarchy != null) {
                currentHierarchy.addChild(task);
            }
        }
        
        return hierarchies;
    }
    
    private void printTaskHierarchy(TaskHierarchy hierarchy, String prefix) {
        Task rootTask = hierarchy.getRoot();
        System.out.println("  " + prefix + "●  " + rootTask.getDate() + " : [" + 
            (rootTask.isCompleted() ? "x" : " ") + "] " + rootTask.getTitle());
        
        List<Task> children = hierarchy.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Task child = children.get(i);
            boolean isLast = (i == children.size() - 1);
            String childPrefix = isLast ? "└─ " : "├─ ";
            String nextPrefix = isLast ? "   " : "│  ";
            
            System.out.println("  " + prefix + childPrefix + child.getDate() + " : [" + 
                (child.isCompleted() ? "x" : " ") + "] " + child.getTitle());
            
            // 3階層目の子タスクがある場合
            printGrandChildren(children, child, prefix + nextPrefix);
        }
    }
    
    private void printGrandChildren(List<Task> allChildren, Task parent, String prefix) {
        List<Task> grandChildren = new ArrayList<>();
        int parentIndex = allChildren.indexOf(parent);
        
        for (int i = parentIndex + 1; i < allChildren.size(); i++) {
            Task task = allChildren.get(i);
            if (task.getIndentLevel() > parent.getIndentLevel()) {
                grandChildren.add(task);
            } else {
                break;
            }
        }
        
        for (int i = 0; i < grandChildren.size(); i++) {
            Task grandChild = grandChildren.get(i);
            boolean isLast = (i == grandChildren.size() - 1);
            String childPrefix = isLast ? "└─ " : "├─ ";
            
            System.out.println("  " + prefix + childPrefix + grandChild.getDate() + " : [" + 
                (grandChild.isCompleted() ? "x" : " ") + "] " + grandChild.getTitle());
        }
    }
    
    private static class TaskHierarchy {
        private Task root;
        private List<Task> children;
        
        public TaskHierarchy(Task root) {
            this.root = root;
            this.children = new ArrayList<>();
        }
        
        public void addChild(Task child) {
            children.add(child);
        }
        
        public Task getRoot() {
            return root;
        }
        
        public List<Task> getChildren() {
            return children;
        }
    }
} 