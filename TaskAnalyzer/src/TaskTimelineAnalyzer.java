import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TaskTimelineAnalyzer {
    private final List<Task> allTasks;
    private final boolean debugMode;
    private final boolean experimenalMode;

    public TaskTimelineAnalyzer(List<Task> tasks) {
        this.allTasks = tasks;
        this.debugMode = false;
        this.experimenalMode = false;
    }
    
    public TaskTimelineAnalyzer(List<Task> tasks, boolean debugMode, boolean experimenalMode) {
        this.allTasks = tasks;
        this.debugMode = debugMode;
        this.experimenalMode = experimenalMode;
    }

    /**
     * TODO allTasksをカテゴリ,日付毎のTask Listにして返す
     * @return -- result map
     */
    public Map<String, Map<LocalDate, List<Task>>> getTaskByCategoryAndDate() {
        return null;
    }

    /**
     * tasksを日付でMapする</br>
     * 日付でソート(昇順)して返す
     * @param tasks
     * @return -- result map
     */
    public Map<LocalDate, List<Task>> getTaskByDate(List<Task> tasks) {
        Map<LocalDate, List<Task>> result = tasks.stream()
            .sorted(Comparator.comparing(Task::getDate, Comparator.reverseOrder()))
            .collect(Collectors.groupingBy(Task::getDate, TreeMap::new, Collectors.toList()));
        
        return result;
    }

    public int getTotalUniqueTaskCount() {
        return allTasks.stream()
            .distinct()
            .collect(Collectors.toList())
            .size();
    }

    public void printTimelineReport() {
        System.out.println("\n\n");

        Map<String, List<Task>> tasksByCategory = allTasks.stream()
            .collect(Collectors.groupingBy(Task::getCategory));
        
        for (Map.Entry<String, List<Task>> entry : tasksByCategory.entrySet()) {
            String category = entry.getKey();
            List<Task> tasks = entry.getValue();
            TaskOrderedTreeManager.TaskOrderedTree categoryTree = null;

            // set first apparance date
            List<Task> uniqueTasks = uniqueTasks(tasks);
            updateFirstAppearanceDates(uniqueTasks);
            
            // build task summary trees
            Map<LocalDate, List<Task>> tasksByDate = tasks.stream()
                .collect(Collectors.groupingBy(Task::getDate, TreeMap::new, Collectors.toList()));
            for (Map.Entry<LocalDate, List<Task>> dateEntry : tasksByDate.entrySet()) {
                TaskOrderedTreeManager.TaskOrderedTree dailyTree = TaskOrderedTreeManager.buildOrderedTree(dateEntry.getValue());
                categoryTree = TaskOrderedTreeManager.mergeOrderedTrees(categoryTree, dailyTree);
            }
            
            // print summary
            printTaskNumWandering(category, tasks, "  ");
            printOrderedSprintSummary(category, uniqueTasks.size(), categoryTree, "  ");
        }
    }

    public void printTaskNumWandering(String category, List<Task> tasks, String prefix) {
        
        System.out.println("\n[Category: " + category + "]");
        System.out.println("==============================");
        System.out.println(prefix + "Date\t\tTasks\tDiff");
        
        List<Task> prevTasks = null;
        Map<LocalDate, List<Task>> tasksByDate = getTaskByDate(tasks);
        for (Map.Entry<LocalDate, List<Task>> entry : tasksByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<Task> dailyTasks = entry.getValue();

            int count = 0;
            int change = 0;
            
            // merge tasks & count
            if(prevTasks != null) {
                List<Task> mergedTasks = new ArrayList<Task>();
                mergedTasks.addAll(prevTasks);
                mergedTasks.addAll(dailyTasks);
                mergedTasks = uniqueTasks(mergedTasks);

                count = mergedTasks.size();
                change = count - prevTasks.size();
                prevTasks = mergedTasks;
            } else {
                prevTasks = dailyTasks;
                count = dailyTasks.size();
            }

            System.out.printf(prefix + "%s\t%d\t%s%n", date, count, (change <= 0 ? change : "+" + change) );
        }
    }

    public void printOrderedSprintSummary(String category, int taskSize, TaskOrderedTreeManager.TaskOrderedTree categoryTree, String prefix) {
        System.out.println(String.format("\n [%s](%s件)", category, taskSize));

        if (categoryTree != null) {
            TaskOrderedTreeManager.printOrderedTree(categoryTree, "  ", experimenalMode);
        }

        System.out.println();
    }
    
    /**
     * tasksの重複を除外して返す<br>
     * カテゴリ、タスク名、インデントが同一のtaskを同一条件としてもつ</br>
     * 
     * @param tasks -- target
     * @return -- unique tasks sorted by task date
     */
    private List<Task> uniqueTasks(List<Task> tasks) {
        List<Task> uniqueTasks = tasks.stream()
            .sorted((a,b) -> a.getDate().compareTo(b.getDate()))
            .filter(Task.distinctByKey(task -> task.getCategory() + ";" + task.getIndentLevel() + ";" + task.getTitle()))
            .toList();

        return uniqueTasks;
    }
    
    /**
     * uniquesのfirstAppearanceDateでtasksByDate内の対応するタスクのfirstAppearanceDateを更新する</br>
     * タスクの同一条件: title, indentLevel, categoryが同一
     */
    private void updateFirstAppearanceDates(List<Task> uniques) {
        for (Task task : allTasks) {
            LocalDate firstApparanceDate = uniques.stream()
                .filter(t -> task.equals(t))
                .findFirst()
                .map(t -> t.getDate())
                .orElse(null);
            
            if (firstApparanceDate != null) {
                task.setFirstAppearanceDate(firstApparanceDate);
            }
        }
    }
    
    
} 