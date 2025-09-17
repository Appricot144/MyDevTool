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

        /**
         * children の末尾に child を追加する
         * 
         * @param child
         */
        public void addChild(TaskOrderedTree child) {
            children.add(child);
        }

        /**
         * children から task と equals true となる child を返す
         * 
         * @param task
         * @return -- found a child
         */
        public TaskOrderedTree findChild(Task task) {
            return children.stream()
                    .filter(child -> child.task.equals(task))
                    .findFirst()
                    .orElse(null);
        }

        /**
         * childrenの中でpointと一致する要素の前にinsertsを挿入</br>
         * children: [... , inserts..., point, ...]
         *
         * @param inserts -- nodes to inserts
         * @param point   -- insert point
         * @return
         */
        public void insertSomeChild(List<TaskOrderedTree> inserts, TaskOrderedTree point) {
            int insertIndex = -1;
            for (int i = 0; i < this.children.size(); i++) {
                if (this.children.get(i).getTask().equals(point.getTask())) {
                    insertIndex = i;
                    break;
                }
            }

            this.children.addAll(insertIndex, inserts);
        }

        // Getters
        public Task getTask() {
            return task;
        }

        public List<TaskOrderedTree> getChildren() {
            return children;
        }

        public int getOrder() {
            return order;
        }

        // Setters
        public void setTask(Task task) {
            this.task = task;
        }
    }

    public static TaskOrderedTree buildOrderedTree(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return null;
        }

        TaskOrderedTree root = new TaskOrderedTree(null, -1);
        Stack<TaskOrderedTree> parentStack = new Stack<>();
        parentStack.push(root);

        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            TaskOrderedTree newNode = new TaskOrderedTree(task, i);

            // indent level 3 以上は indent level 3 として扱う
            while (parentStack.size() > task.getIndentLevel() + 2) {
                parentStack.pop();
            }

            if (parentStack.size() < task.getIndentLevel() + 1) {
                parentStack.peek().addChild(newNode);
                parentStack.push(newNode);
            } else {
                while (parentStack.size() > task.getIndentLevel() + 1) {
                    parentStack.pop();
                }
                parentStack.peek().addChild(newNode);
                parentStack.push(newNode);
            }
        }

        return root;
    }

    /**
     * baseTree に newTree を merge</br>
     * Task.equals を条件に同一とみなす</br>
     * merge 処理は再帰的に実行される
     * 
     * @param baseTree
     * @param newTree
     * @return -- merged tree
     */
    public static TaskOrderedTree mergeOrderedTrees(TaskOrderedTree baseTree, TaskOrderedTree newTree) {
        if (baseTree == null) {
            return newTree;
        }
        if (newTree == null) {
            return baseTree;
        }

        List<TaskOrderedTree> holds = new ArrayList<>();

        for (TaskOrderedTree newChild : newTree.getChildren()) {
            TaskOrderedTree existingChild = baseTree.findChild(newChild.getTask());

            if (existingChild != null) {
                existingChild.setTask(newChild.getTask());
                // holdsをnewChildの前に挿入
                baseTree.insertSomeChild(holds, newChild);
                holds.clear();
                mergeOrderedTrees(existingChild, newChild);
            } else {
                holds.add(newChild);
            }
        }

        // 一致するnodeがなければholdsを後ろに挿入
        baseTree.getChildren().addAll(holds);

        return baseTree;
    }

    /**
     * TaskOrderedTree をコピーして返す
     * 
     * @param source
     * @return -- copied tree
     */
    public static TaskOrderedTree copyTree(TaskOrderedTree source) {
        TaskOrderedTree copy = new TaskOrderedTree(source.getTask(), source.getOrder());
        for (TaskOrderedTree child : source.getChildren()) {
            copy.addChild(copyTree(child));
        }
        return copy;
    }

    /**
     * TaskOrderedTreeの出力
     * 
     * @param tree
     * @param prefix
     */
    public static void printOrderedTree(TaskOrderedTree tree, String prefix, boolean experimenalMode) {
        List<TaskOrderedTree> children = tree.getChildren();

        if (tree.getTask() == null) {
            System.out.println(prefix + "●");
        }

        for (int i = 0; i < children.size(); i++) {
            TaskOrderedTree child = children.get(i);
            boolean isLast = (i == children.size() - 1);
            String childPrefix = isLast ? "└─" : "├─";
            String nextPrefix = isLast ? "   " : "│  ";

            if (experimenalMode) {
                // System.out.println(prefix + "│ " +
                // "---------------------------------------------------");
                System.out.println(prefix + "│ ");
            }
            printOrderedTreeRecursive(child, prefix + childPrefix, prefix + nextPrefix);
        }
    }

    private static void printOrderedTreeRecursive(TaskOrderedTree tree, String currentPrefix, String nextPrefix) {
        Task task = tree.getTask();

        List<TaskOrderedTree> children = tree.getChildren();
        if (children.isEmpty()) {
            System.out.println(task.getColor() + currentPrefix + task.getPrintString() + Task.END);
        } else {
            System.out.println(task.getColor() + currentPrefix + task.getPrintString() + Task.END);
            for (int i = 0; i < children.size(); i++) {
                TaskOrderedTree child = children.get(i);
                boolean isLast = (i == children.size() - 1);
                String childPrefix = isLast ? "└─" : "├─";
                String childNextPrefix = isLast ? "   " : "│  ";

                printOrderedTreeRecursive(child, nextPrefix + childPrefix, nextPrefix + childNextPrefix);
            }
        }
    }
}