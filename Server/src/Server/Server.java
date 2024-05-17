package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public enum Server {
    SERVER;
    public static final String _specialMark = "@XdY#";
    public static final String _endMark = _specialMark + "endChatting";
    public static final String _duplicationName = _specialMark + "duplicationName";
    public static final String _kickout = _specialMark + "kickout";


    private static CLI cli;
    private static final List<Channel> channels = Collections.synchronizedList(new LinkedList<>());

    public static void setCLI(CLI cli) {
        Server.cli = cli;
    }

    public static void start() throws IOException {
        ServerSocket server = new ServerSocket(Config.PORT_NUMBER);
        while (true) {
            Socket client = server.accept();
            connect(client);
        }
    }

    private static void connect(Socket client) {
        new Thread(new Runnable() {
            private String getNickname(Socket socket) throws IOException {
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                return dis.readUTF();
            }

            private void sendPort(Socket socket, int port) throws IOException {
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF(String.valueOf(port));
                dos.flush();
            }

            private boolean portInUse(int port) {
                try {
                    Socket socket = new Socket("localhost", port);
                    socket.close();
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }

            private int getFreePort() {
                for (int port = Config.START_PORT_NUMBER; port <= Config.END_PORT_NUMBER; port++) {
                    if (!portInUse(port)) {
                        return port;
                    }
                }
                return -1;
            }

            private boolean accountExists(String nickname) {
                synchronized (channels) {
                    for (Channel c : channels) {
                        if (c.nickname.equals(nickname)) {
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public void run() {
                try {
                    String nickname = getNickname(client);
                    if (!accountExists(nickname)) {
                        int port = getFreePort();
                        if (port == -1){
                            sendPort(client, -1);
                            return;
                        }
                        ServerSocket server = new ServerSocket(port);
                        sendPort(client, port);
                        Socket client = server.accept();
                        Server.addClient(new Channel(nickname, client, cli));
//                        System.out.println(nickname + " 连接成功, 端口号为：" + client.getLocalPort());

                        String welcomeMsg = getConnectMsg(nickname);
                        programMsg("", welcomeMsg);
                        cli.outputMsg(welcomeMsg);
                    }
                    else{
                        sendPort(client, -1);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private static String getConnectMsg(String nickname){
        return "[" + getCurrentTime() + "]" + " 欢迎 " + nickname + " 进入聊天室";
    }

    static String getCurrentTime(){
        LocalDateTime time = LocalDateTime.now();
        return String.format("%02d:%02d:%02d",time.getHour(),time.getMinute(),time.getSecond());
    }

    private static void addClient(Channel client) {
        channels.add(client);
    }

    public static void programMsg(String user, String msg){
        synchronized (channels){
            for(Channel c : channels){
                if(c.nickname.equals(user)){
                    continue;
                }
                c.sender.send(msg);
            }
        }
    }

    public static void remove(String user){
        synchronized (channels){
            channels.removeIf(c -> c.nickname.equals(user));
        }
    }

    public static void stop(){
        synchronized (channels){
            for(Channel c : channels){
                c.sender.send(Server._endMark);
            }
        }
        channels.clear();
        System.exit(0);
    }
}

class Channel{
    String nickname;
    Socket socket;
    CLI cli;
    Sender sender;
    Receiver receiver;
    boolean running;
    public Channel(String nickname, Socket socket, CLI cli) throws IOException {
        this.nickname = nickname;
        this.socket = socket;
        this.cli = cli;
        sender = new Sender();
        receiver = new Receiver();
        running = true;
    }

    public void disconnect(){
        if(!running) return;
        running = false;
        sender.stop();
        receiver.stop();
        Server.remove(nickname);

        String disconnectMsg = getDisconnectMsg(nickname);
        Server.programMsg("", disconnectMsg);
        cli.outputMsg(disconnectMsg);
    }

    private String getDisconnectMsg(String nickname){
        return nickname + " 退出了聊天室";
    }

    public class Sender{
        private final LinkedBlockingQueue<String> sendingQueue = new LinkedBlockingQueue<String>();
        public Sender() throws IOException {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                while(true){
                    try {
                        String msg = sendingQueue.take();
                        dos.writeUTF(msg);
                        dos.flush();
                        if (msg.equals(Server._endMark)){
                            disconnect();
                            break;
                        }
                    } catch (InterruptedException | IOException ignored) {
                    }
                }
            }).start();
        }
        public void send(String msg){
            sendingQueue.add(msg);
        }
        public void stop(){
            sendingQueue.clear();
            sendingQueue.add(Server._endMark);
        }
    }

    public class Receiver{
        private final LinkedBlockingQueue<String> receivingQueue = new LinkedBlockingQueue<>();
        public Receiver() throws IOException {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            new Thread(() -> {
                while(true){
                    try {
                        String msg = dis.readUTF();
                        if(msg.isEmpty()){
                            continue;
                        }
                        receivingQueue.add(msg);
                        if(msg.equals(Server._endMark)){
                            disconnect();
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
                        if(msg.equals(Server._endMark)){
                            disconnect();
                            break;
                        }
                        Server.programMsg(nickname, msg);
                        cli.outputMsg(msg);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
        public String receive() throws InterruptedException {
            return receivingQueue.take();
        }
        public void stop(){
            receivingQueue.add(Server._endMark);
        }
    }

}