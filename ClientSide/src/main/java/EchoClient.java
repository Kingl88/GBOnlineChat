import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class EchoClient extends JFrame {

    private final String SERVER_ADDRESS = "127.0.0.1";
    private final Integer SERVER_PORT = 8886;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Socket socket;
    private boolean isExit = false;

    private JTextField msgInputField;
    private JTextArea chatArea;

    public EchoClient() throws IOException {
        connectionToServer();
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        boolean isLogIn = new Login(dos, dis).loginToChat();

        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    if(!isLogIn){
                        JOptionPane.showMessageDialog(null, "Connection timeout exceeded");
                        return;
                    }
                    String message;
                    if (dis.available() > 0) {
                        message = dis.readUTF();
                        if (message.startsWith("/start")) {
                            chatArea.append(message + "\n");
                            break;
                        }
                        if (isExit) {
                            closeConnection();
                            return;
                        }
                        chatArea.append(message + "\n");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Wrong connection to server");
            }
        });
        thread.setDaemon(true);
        thread.start();
        if (isLogIn) {
            prepareGUI();
        } else{
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            closeConnection();
        }
    }

    private void sendMessageToServer() {
        String msg = msgInputField.getText();
        if (msg.equals("/finish")) {
            isExit = true;
        }
        System.out.println("Message to server: " + msg);
        if (!msg.trim().isEmpty()) {
            try {
                dos.writeUTF(msg);
                msgInputField.setText("");
                msgInputField.grabFocus();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "You send incorrect message");
            }
        }
    }

    private void connectionToServer() throws IOException {
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
    }

    private void closeConnection() {
        try {
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dispose();
    }

    private void prepareGUI() {
        // Параметры окна
        setBounds(600, 300, 500, 500);
        setTitle("Клиент");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Текстовое поле для вывода сообщений
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.BOLD, 16));
        chatArea.setLineWrap(true);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Нижняя панель с полем для ввода сообщений и кнопкой отправки сообщений
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JButton btnSendMsg = new JButton("Отправить");
        bottomPanel.add(btnSendMsg, BorderLayout.EAST);
        msgInputField = new JTextField();
        add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.add(msgInputField, BorderLayout.CENTER);
        btnSendMsg.addActionListener(e -> sendMessageToServer());
        msgInputField.addActionListener(e -> sendMessageToServer());

        // Настраиваем действие на закрытие окна
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    if (!socket.isClosed()) {
                        dos.writeUTF("/finish");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        setVisible(true);
    }

}
