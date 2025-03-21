package RadomCsvGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class RandomCsvGenerator {
    
    public static void main(String[] args) {
        try {
            generateRandomCsv("random_data_5000.csv", 5000);
            System.out.println("CSVファイルが生成されました: random_data_5000.csv");
        } catch (IOException e) {
            System.err.println("エラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void generateRandomCsv(String filename, int numRows) throws IOException {
        FileWriter writer = new FileWriter(filename);
        Random random = new Random();
        
        // ヘッダー行の書き込み
        writer.write("番号,名前,部署,入社日,給与,評価,勤続年数,プロジェクト,スキル,備考\n");
        
        // データ行の生成
        for (int i = 0; i < numRows; i++) {
            int id = 2018001 + i; // 2018001から始まる連番
            
            StringBuilder line = new StringBuilder();
            line.append(id).append(",");
            line.append(randomString(10)).append(",");
            line.append(randomDepartment(random)).append(",");
            line.append(randomDate(random)).append(",");
            line.append(randomSalary(random)).append(",");
            line.append(randomRating(random)).append(",");
            line.append(random.nextInt(20)).append(",");
            line.append(randomProject(random)).append(",");
            line.append(randomSkills(random)).append(",");
            line.append(randomString(20));
            
            writer.write(line.toString() + "\n");
        }
        
        writer.close();
    }
    
    // ランダムな文字列を生成
    private static String randomString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        
        return sb.toString();
    }
    
    // ランダムな部署を取得
    private static String randomDepartment(Random random) {
        String[] departments = {"営業", "開発", "人事", "総務", "経理"};
        return departments[random.nextInt(departments.length)];
    }
    
    // ランダムな日付を生成
    private static String randomDate(Random random) {
        int year = 2018 + random.nextInt(6); // 2018-2023
        int month = 1 + random.nextInt(12);  // 1-12
        int day = 1 + random.nextInt(28);    // 1-28
        
        LocalDate date = LocalDate.of(year, month, day);
        return date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }
    
    // ランダムな給与を生成
    private static int randomSalary(Random random) {
        return 200000 + random.nextInt(600001); // 200000-800000
    }
    
    // ランダムな評価を取得
    private static String randomRating(Random random) {
        String[] ratings = {"A", "B", "C", "D", "S"};
        return ratings[random.nextInt(ratings.length)];
    }
    
    // ランダムなプロジェクトを取得
    private static String randomProject(Random random) {
        String[] projects = {"Alpha", "Beta", "Gamma", "Delta", "Epsilon"};
        return projects[random.nextInt(projects.length)];
    }
    
    // ランダムなスキルを生成
    private static String randomSkills(Random random) {
        String[] skills = {"Python", "Java", "SQL", "Excel", "C#", "JavaScript"};
        int numSkills = 1 + random.nextInt(3); // 1-3個のスキル
        
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < numSkills; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(skills[random.nextInt(skills.length)]);
        }
        
        return "\"" + sb.toString() + "\""; // カンマを含むため引用符で囲む
    }
}
