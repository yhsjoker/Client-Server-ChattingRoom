package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class GUI extends JFrame {
    private final JTextField ipTextField = new JTextField("localhost", 6);
    private final JTextField portNumberTextField = new JTextField("9999", 6);
    private final JTextField nicknameTextField = new JTextField(randomName() , 10);
    private final JButton loginBtn = new JButton("进入聊天室");
    private final JButton logoutBtn = new JButton("退出聊天室");
    private final JTextArea contentArea = new JTextArea();
    private final JScrollPane contentScrollPane = new JScrollPane(contentArea);
    private final JTextArea msgArea = new JTextArea(5, 20);
    private final JScrollPane msgScrollPane = new JScrollPane(msgArea);
    private final JButton sendBtn = new JButton("发送");
    private boolean connected = false;

    public GUI() {
        setAppearance();
        setGUI();
        Client.setGUI(this);
        setButton();
        setComponentStatus();
    }
    public String randomName(){
        Random random = new Random();
        int randomNumber = 1000 + random.nextInt(9000);
        String suf = String.valueOf(randomNumber);
        return "临时用户" + suf;
    }
    public void setConnected(boolean status){
        connected = status;
        setComponentStatus();
    }
    public String getIP(){
        return ipTextField.getText();
    }

    public int getPortNumber(){
        return Integer.parseInt(portNumberTextField.getText());
    }

    public String getNickname(){
        return nicknameTextField.getText();
    }
    public String getTextMsg(){
        return msgArea.getText();
    }

    public void appendContent(String msg){
        SwingUtilities.invokeLater(() -> {
            if(msg != null) {
                contentArea.append(msg + "\n");
            }
        });
    }
    public void clearMsgArea(){
        msgArea.setText("");
    }

    private void setAppearance() {
        setSize(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(Config.WINDOW_TITLE);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    private void setGUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(getMenu(), BorderLayout.NORTH);
        mainPanel.add(contentScrollPane, BorderLayout.CENTER);
        mainPanel.add(getSendArea(), BorderLayout.SOUTH);

        this.add(mainPanel);
    }

    private JPanel getMenu() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 10));

        setTextFieldOnlyNumber(portNumberTextField);

        menuPanel.add(getContainer("IP: ", ipTextField));
        menuPanel.add(getContainer("端口: ", portNumberTextField));
        menuPanel.add(getContainer("昵称: ", nicknameTextField));
        menuPanel.add(loginBtn);
        menuPanel.add(logoutBtn);

        return menuPanel;
    }

    private Container getContainer(String labelName, JTextField textField) {
        JPanel container = new JPanel();
        container.add(new JLabel(labelName));
        container.add(textField);
        return container;
    }

    private JPanel getSendArea() {
        JPanel sendAreaPanel = new JPanel(new BorderLayout());
        sendAreaPanel.add(msgScrollPane, BorderLayout.CENTER);
        sendAreaPanel.add(sendBtn, BorderLayout.EAST);
        return sendAreaPanel;
    }

    private void setTextFieldOnlyNumber(JTextField textField){
        textField.addKeyListener(new KeyAdapter(){
            public void keyTyped(KeyEvent e) {
                int keyChar = e.getKeyChar();
                if(keyChar >= KeyEvent.VK_0 && keyChar <= KeyEvent.VK_9){
                }else{
                    e.consume();
                }
            }
        });
    }

    private void setButton(){
        loginBtn.addActionListener(e -> {
            String status = Client.connect();
            JOptionPane.showMessageDialog(this, status);
        });
        logoutBtn.addActionListener(e -> Client.disconnect());
        sendBtn.addActionListener(e -> Client.send());
    }

    private void setComponentStatus(){
        if(contentArea.isEnabled()){
            contentArea.setEnabled(false);
        }
        if(!msgArea.isEnabled()){
            msgArea.setEnabled(true);
        }
        if(connected){
            if(ipTextField.isEnabled()){
                ipTextField.setEnabled(false);
            }
            if(portNumberTextField.isEnabled()){
                portNumberTextField.setEnabled(false);
            }
            if(nicknameTextField.isEnabled()){
                nicknameTextField.setEnabled(false);
            }
            if(loginBtn.isEnabled()){
                loginBtn.setEnabled(false);
            }
            if(!logoutBtn.isEnabled()){
                logoutBtn.setEnabled(true);
            }
            if(!sendBtn.isEnabled()){
                sendBtn.setEnabled(true);
            }
        }
        else{
            if(!ipTextField.isEnabled()){
                ipTextField.setEnabled(true);
            }
            if(!portNumberTextField.isEnabled()){
                portNumberTextField.setEnabled(true);
            }
            if(!nicknameTextField.isEnabled()){
                nicknameTextField.setEnabled(true);
            }
            if(!loginBtn.isEnabled()){
                loginBtn.setEnabled(true);
            }
            if(logoutBtn.isEnabled()){
                logoutBtn.setEnabled(false);
            }
            if(sendBtn.isEnabled()){
                sendBtn.setEnabled(false);
            }
        }
    }
}

