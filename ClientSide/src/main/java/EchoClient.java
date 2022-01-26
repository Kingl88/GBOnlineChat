import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EchoClient extends JFrame {
    private final int NUMBER_LINE_FOR_READ_FROM_FILE = 100;
    private final String SERVER_ADDRESS = "127.0.0.1";
    private final Integer SERVER_PORT = 8886;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Socket socket;
    private boolean isExit = false;
    private String loginForFileName;
    private JTextField msgInputField;
    private JTextArea chatArea;

    public EchoClient() throws IOException {
        connectionToServer();
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        Login login = new Login(dos, dis);
        if (login.loginToChat()) {
            prepareGUI();
            loginForFileName = login.getLogin();
            try (BufferedReader br = new BufferedReader(new FileReader("serverLog.txt"))) {
                List<String> allFile = new ArrayList<>();
                String line;
                while ((line = br.readLine()) != null) {
                    allFile.add(line);
                }
                if (allFile.size() < NUMBER_LINE_FOR_READ_FROM_FILE){
                    for (String s : allFile) {
                        chatArea.append(s + "\n");
                    }
                } else {
                    for (int i = allFile.size() - NUMBER_LINE_FOR_READ_FROM_FILE; i < allFile.size(); i++) {
                        chatArea.append(allFile.get(i) + "\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    String message;
                    if (dis.available() > 0) {
                        message = dis.readUTF();
                        if (message.startsWith("/start")) {
                            chatArea.append(message + "\n");
                            break;
                        }
                        if (isExit) {
                            closeConnection();
                            break;
                        }
                        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("client" + loginForFileName + ".txt", true))) {
                            bos.write((message + "\n").getBytes(StandardCharsets.UTF_8));
                            chatArea.append(message + "\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Wrong connection to server");
            }
        });
        thread.setDaemon(true);
        thread.start();
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
