import java.util.*;

public class TaskOrderedTreeManager {
    
    public static class TaskOrderedTree {
        private Task task;
        private List<TaskOrderedTree> children;
        private int order;
        
        public TaskOrderedTree(Task task, int order) {
            this.task = task;
            this.children = new ArrayList<>();
            this.order = order;
        }
        
        public void addChild(TaskOrderedTree child) {
            children.add(child);
            // TODO chileren をorderでsort
        }
        
        public TaskOrderedTree findChild(Task task) {
            return children.stream()
                .filter(child -> child.task.equals(task))
                .findFirst()
                .orElse(null);
        }
        
        public void updateTaskCompleted(boolean completed) {
            this.task = new Task(
                this.task.getTitle(),
                completed,
                this.task.getDate(),
                this.task.getCategory(),
                this.task.getIndentLevel()
            );
            this.task.setFirstAppearanceDate(this.task.getFirstAppearanceDate());
        }
        
        // Getters
        public Task getTask() { return task; }
        public List<TaskOrderedTree> getChildren() { return children; }
        public int getOrder() { return order; }
    }
    
    public static TaskOrderedTree buildOrderedTree(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return null;
        }
        
        TaskOrderedTree root = new TaskOrderedTree(null, -1);
        Stack<TaskOrderedTree> parentStack = new Stack<>();
        parentStack.push(root);
        
        // FIXME order の値が階層毎に振られていない。(親1,2,3,4 子1,2,3 孫1,2)
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            TaskOrderedTree newNode = new TaskOrderedTree(task, i);
            
            // indent level 3 以上は indent level 3 として扱う
            while (parentStack.size() > task.getIndentLevel() + 2) {
                parentStack.pop();
            }
            
            if (parentStack.size() == task.getIndentLevel() + 1) {
                parentStack.peek().addChild(newNode);
                parentStack.push(newNode);
            } else if (parentStack.size() < task.getIndentLevel() + 2) {
                parentStack.peek().addChild(newNode);
                parentStack.push(newNode);
            } else {
                while (parentStack.size() > task.getIndentLevel() + 2) {
                    parentStack.pop();
                }
                parentStack.peek().addChild(newNode);
                parentStack.push(newNode);
            }
        }
        
        return root;
    }
    
    public static TaskOrderedTree mergeOrderedTrees(TaskOrderedTree baseTree, TaskOrderedTree newTree) {
        if (baseTree == null) {
            return newTree;
        }
        if (newTree == null) {
            return baseTree;
        }
        
        for (TaskOrderedTree newChild : newTree.getChildren()) {
            TaskOrderedTree existingChild = baseTree.findChild(newChild.getTask());
            
            if (existingChild != null) {
                existingChild.updateTaskCompleted(newChild.getTask().isCompleted());
                mergeOrderedTrees(existingChild, newChild);
            } else {
                baseTree.addChild(copyTree(newChild));
            }
        }
        
        return baseTree;
    }
    
    public static TaskOrderedTree copyTree(TaskOrderedTree source) {
        TaskOrderedTree copy = new TaskOrderedTree(source.getTask(), source.getOrder());
        for (TaskOrderedTree child : source.getChildren()) {
            copy.addChild(copyTree(child));
        }
        return copy;
    }
    
    public static void printOrderedTree(TaskOrderedTree tree, String prefix) {
        if (tree.getTask() == null) {
            for (TaskOrderedTree child : tree.getChildren()) {
                printOrderedTree(child, prefix);
            }
            return;
        }
        
        Task task = tree.getTask();
        System.out.println(prefix + "●  " + task.getPrintString() + " " + tree.getOrder()); // debug + " " + tree.getOrder()
        
        List<TaskOrderedTree> children = tree.getChildren();
        for (int i = 0; i < children.size(); i++) {
            TaskOrderedTree child = children.get(i);
            boolean isLast = (i == children.size() - 1);
            String childPrefix = isLast ? "└─ " : "├─ ";
            String nextPrefix = isLast ? "   " : "│  ";
            
            printOrderedTreeChild(child, prefix + childPrefix, prefix + nextPrefix);
        }
    }
    
    private static void printOrderedTreeChild(TaskOrderedTree tree, String currentPrefix, String nextPrefix) {
        Task task = tree.getTask();
        System.out.println(currentPrefix + task.getPrintString() + " " + tree.getOrder()); // debug + " " + tree.getOrder()
        
        List<TaskOrderedTree> children = tree.getChildren();
        for (int i = 0; i < children.size(); i++) {
            TaskOrderedTree child = children.get(i);
            boolean isLast = (i == children.size() - 1);
            String childPrefix = isLast ? "└─ " : "├─ ";
            String childNextPrefix = isLast ? "   " : "│  ";
            
            printOrderedTreeChild(child, nextPrefix + childPrefix, nextPrefix + childNextPrefix);
        }
    }
}