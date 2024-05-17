package Server;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public enum Log {
    LOG;
    private static FileWriter fileWriter;
    public static boolean start(String filepath) throws IOException {
        if(Files.exists(Paths.get(filepath))){
            fileWriter = new FileWriter(filepath, true);
            System.out.println("日志文件流创建成功，可以接收系统运行日志了，全民大聊天正式开始！");
            return true;
        }
        else{
            System.out.println("您给出的日志文件不存在，请查看");
            return false;
        }
    }
    public static void close() throws IOException {
        if (fileWriter != null) {
            fileWriter.close();
        }
    }
    public static void write(String msg) throws IOException {
        if (fileWriter != null) {
            fileWriter.write(msg + "\n"); // 添加换行符以便日志阅读
            fileWriter.flush(); // 确保即时写入文件
        }
    }
}
