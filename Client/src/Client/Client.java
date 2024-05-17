package Client;

import javax.naming.TimeLimitExceededException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

public enum Client {
    CLIENT;
    public static final String _specialMark = "@XdY#";
    public static final String _endMark = _specialMark + "endChatting";
    public static final String _duplicationName = _specialMark + "duplicationName";
    public static final String _kickout = _specialMark + "kickout";

    private static GUI gui;
    private static Socket socket;

    public static void setGUI(GUI gui){
        Client.gui = gui;
    }

    public static String connect() {
        try {
            Client.socket = getSocket();
            Client.Sender.start();
            Client.Receiver.start();
            gui.setConnected(true);
            return "连接成功";
        }
        catch (IOException | TimeLimitExceededException ignored){
            System.out.println("连接失败");
            return "连接失败";
        }
        catch (Exception ignored){
            System.out.println("用户名重复");
            return "用户名重复";
        }
    }

    private static Socket getSocket() throws Exception{
        Socket loading = new Socket();
        loading.connect(new InetSocketAddress(gui.getIP(), gui.getPortNumber()), 1000);

        sendMsg(loading, gui.getNickname());
        String port = rcvMsg(loading);

        int portNumber = Integer.parseInt(port);

        if(portNumber == -1){
            throw new Exception();
        }

        return new Socket(gui.getIP(), portNumber);
    }

    private static void sendMsg(Socket socket, String msg) throws IOException {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeUTF(msg);
        dos.flush();
    }

    private static String rcvMsg(Socket socket) throws IOException, TimeLimitExceededException{
        DataInputStream dis = new DataInputStream(socket.getInputStream());

        long begin = new Date().getTime();
        String msg = "";
        while (true){
            msg = dis.readUTF();
            if(!msg.isEmpty()){
                return msg;
            }

            long now = new Date().getTime();
            if(now - begin > Config.CONNECTING_TIME_LIMIT){
                throw new TimeLimitExceededException();
            }
        }
    }
    private static String getCurrentTime(){
        LocalDateTime time = LocalDateTime.now();
        return String.format("%02d:%02d:%02d",time.getHour(),time.getMinute(),time.getSecond());
    }
    public static void send(){
        String msg = "[" + gui.getNickname() + ":" + getCurrentTime() + "] " + gui.getTextMsg();
        Sender.send(msg);
        gui.clearMsgArea();
    }

    public static void disconnect(){
        gui.setConnected(false);
        Sender.stop();
        Receiver.stop();
    }

    private enum Sender{
        SENDER;
        private static final LinkedBlockingQueue<String> sendingQueue = new LinkedBlockingQueue<String>();
        //修改为加锁队列
        public static void start() throws IOException {
            sendingQueue.clear();
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                while(true){
                    try {
                        String msg = sendingQueue.take();
                        dos.writeUTF(msg);
                        dos.flush();
                        if (msg.equals(Client._endMark)){
                            Client.disconnect();
                            break;
                        }
                    } catch (InterruptedException | IOException ignored) {
                    }
                }
            }).start();
        }
        public static void send(String msg){
            sendingQueue.add(msg);
            Receiver.localReceiveMsg(msg);
        }
        public static void stop(){
            sendingQueue.clear();
            sendingQueue.add(_endMark);
        }
    }

    private enum Receiver{
        RECEIVER;
        private static final LinkedBlockingQueue<String> receivingQueue = new LinkedBlockingQueue<>();
        public static void start() throws IOException {
            receivingQueue.clear();
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            new Thread(() -> {
                while(true){
                    try {
                        String msg = dis.readUTF();
                        if(msg.isEmpty()){
                            continue;
                        }
                        receivingQueue.add(msg);
                        if(msg.equals(_endMark)){
                            Client.disconnect();
                            break;
                        }
                    } catch (IOException ignored) {
                    }
                }
            }).start();

            new Thread(() -> {
                while (true){
                    try {
                        String msg = receive();
                        if(msg.equals(_endMark)){
                            Client.disconnect();
                            break;
                        }
                        gui.appendContent(msg);
                    } catch (InterruptedException ignored) {
                    }
                }
            }).start();
        }
        public static String receive() throws InterruptedException {
            return receivingQueue.take();
        }

        public static void localReceiveMsg(String msg){
            receivingQueue.add(msg);
        }

        public static void stop(){
            receivingQueue.add(_endMark);
        }
    }
}
