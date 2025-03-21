package PropertiesFileUpdator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * .propertiesファイルの特定の変数を変換するプログラム
 * コマンドライン引数として、編集対象のファイルパス、編集する変数、変換先の値を受け取ります
 */
public class PropertiesFileUpdator {

    public static void main(String[] args) {
        // コマンドライン引数のチェック
        if (args.length != 3) {
            System.out.println("使用方法: java PropertiesFileUpdater <ファイルパス> <変数名> <新しい値>");
            System.out.println("例: java PropertiesFileUpdater config.properties app.url http://newserver.com");
            System.exit(1);
        }

        String filePath = args[0];
        String propertyKey = args[1];
        String newValue = args[2];

        try {
            updateProperty(filePath, propertyKey, newValue);
            System.out.println("変数の更新が完了しました。");
            System.out.println("ファイル: " + filePath);
            System.out.println("変数: " + propertyKey);
            System.out.println("新しい値: " + newValue);
        } catch (IOException e) {
            System.err.println("エラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * .propertiesファイルの特定の変数の値を更新します
     * 
     * @param filePath    編集対象のpropertiesファイルのパス
     * @param propertyKey 編集する変数名
     * @param newValue    変換先の値
     * @throws IOException ファイル操作中にエラーが発生した場合
     */
    public static void updateProperty(String filePath, String propertyKey, String newValue) throws IOException {
        File propertiesFile = new File(filePath);
        
        // ファイルの存在確認
        if (!propertiesFile.exists()) {
            throw new FileNotFoundException("指定されたファイルが見つかりません: " + filePath);
        }
        
        // プロパティファイルの読み込み
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(propertiesFile)) {
            properties.load(fis);
        }
        
        // 指定されたキーの存在確認
        if (!properties.containsKey(propertyKey)) {
            throw new IllegalArgumentException("指定された変数が見つかりません: " + propertyKey);
        }
        
        // 現在の値を取得
        String oldValue = properties.getProperty(propertyKey);
        
        // 値を更新
        properties.setProperty(propertyKey, newValue);
        
        // 変更をファイルに保存
        try (FileOutputStream fos = new FileOutputStream(propertiesFile)) {
            // コメントには更新時間を記載
            String comment = "Updated property " + propertyKey + " from [" + oldValue + "] to [" + newValue + "]";
            properties.store(fos, comment);
        }
    }
}