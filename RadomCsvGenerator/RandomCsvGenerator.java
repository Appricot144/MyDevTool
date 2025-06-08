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
    	
    	// 連番の初期値
    	int id = 2018001;
    	
        FileWriter writer = new FileWriter(filename);
        Random random = new Random();
        
        // ヘッダー行の書き込み
        writer.write("番号,名前,部署,入社日,給与,評価,勤続年数,プロジェクト,スキル,備考\n");
        
        // データ行の生成
        for (int i = 0; i < numRows; i++) {
            id += i;
            
            StringBuilder line = new StringBuilder();
            line.append(id).append(",");
            line.append(randomName(5, " ")).append(",");
            line.append("").append(",");
            line.append(randomDate(30)).append(",");
            line.append("").append(",");
            line.append("").append(",");
            line.append("").append(",");
            line.append("").append(",");
            line.append("").append(",");
            line.append("");
            
            writer.write(line.toString() + "\n");
        }
        
        writer.close();
    }
    
    /**
     * 英字のランダムな名前を作ります
     * 
     * @param length : 姓名の長さ
     * @param separator : 氏名間の文字
     * @return
     */
    private static String randomName(int length, String separator) {
    	String firstName = randomString(length);
    	String familyName = randomString(length);
    	return firstName + separator + familyName;
    }
    
    /**
     * ランダムな文字列を生成
     * 
     * @param length
     * @return
     */
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
    
    /**
     *  ランダムな日付を生成
     *  
     * @param interval : 現在からさかのぼる期間（年）
     * @return
     */
    private static String randomDate(int interval) {
    	Random random = new Random();
    	
    	int nowYear = LocalDate.now().getYear();
        int year = nowYear - random.nextInt(interval);
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28);
        
        LocalDate date = LocalDate.of(year, month, day);
        return date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }
    
    /**
     * TODO 指定した期間の中でランダムな日付を返す
     * 
     * @param random
     * @param startDate
     * @param endDate
     * @return
     */
    private static String randomDate(String startDate, String endDate) {
    	Random random = new Random();
    	return "";
    }
    
    /**
     * 配列からランダムな値を取得
     * 
     * @param random
     * @return
     */
    private static String choiceWord(String[] words) {
    	Random random = new Random();
        return words[random.nextInt(words.length)];
    }
    
    /**
     * 配列からランダムな値を指定した数だけ取得
     * i.g. "A,C,B"
     * 
     * @param random
     * @param words
     * @param num 
     * @return
     */
    private static String choiceSomeWords(String[] words, int num) {
    	Random random = new Random();
        int numWords = 1 + random.nextInt(num);
        
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < numWords; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(words[random.nextInt(words.length)]);
        }
        
        return "\"" + sb.toString() + "\"";
    }
}
