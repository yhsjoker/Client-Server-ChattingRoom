package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.LinkedBlockingQueue;

public class CLI {
    private static final LinkedBlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();
    private static final LinkedBlockingQueue<String> outputQueue = new LinkedBlockingQueue<>();

    public CLI() throws IOException {
        System.out.println("如果想结束聊天服务器请输入指令：end");

        new Thread(() -> {
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            while(true){
                try {
                    String cmd = console.readLine();
                    if(cmd == null || cmd.isEmpty()){
                        continue;
                    }
                    inputQueue.add(cmd);
                    if(inputQueue.take().equals("end")){
                        Server.stop();
                        Log.close();
                    }
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        new Thread(() -> {
            while (true){
                try {
                    String msg = outputQueue.take();
                    System.out.println(msg);
                    Log.write(msg);
                } catch (InterruptedException ignored) {
                } catch (IOException e) {
                    System.out.println("写入文件失败");
                }
            }
        }).start();
        Server.setCLI(this);
        Server.start();
    }

    public void outputMsg(String msg){
        outputQueue.add(msg);
    }
}
