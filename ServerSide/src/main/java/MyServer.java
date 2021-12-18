import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServer extends JFrame {

    private JTextField msgInputField;
    private JTextArea chatArea;

    private DataInputStream dis;
    private DataOutputStream dos;

    private final Integer SERVER_PORT = 8880;
    Socket socket = null;

    public MyServer() {
        System.out.println("Server Start");
        prepareGUI();
        startServer();
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server wait connect");
            socket = serverSocket.accept();
            System.out.println("Client Connected");
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            while (true) {
                String str = dis.readUTF();
                chatArea.append("Message from client: " + str + "\n");
                System.out.println("Message from client: " + str);
                if (str.equalsIgnoreCase("/finish")) {
                    dos.writeUTF(str);
                    closeConnection();
                    dispose(); //Destroy the JFrame object
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendMessageToClient() {
        String msg = msgInputField.getText();
        System.out.println("Message to client: " + msg);
        if (msg != null && !msg.trim().isEmpty()) {
            try {
                dos.writeUTF(msg);
                chatArea.append("Message to client: " + msg + "\n");
                msgInputField.setText("");
                msgInputField.grabFocus();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "You send incorrect message");
            }
        }
    }

    public void prepareGUI() {
        // Параметры окна
        setBounds(600, 300, 500, 500);
        setTitle("Сервер");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Текстовое поле для вывода сообщений
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Нижняя панель с полем для ввода сообщений и кнопкой отправки сообщений
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JButton btnSendMsg = new JButton("Отправить");
        bottomPanel.add(btnSendMsg, BorderLayout.EAST);
        msgInputField = new JTextField();
        add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.add(msgInputField, BorderLayout.CENTER);
        btnSendMsg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessageToClient();
            }
        });
        msgInputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessageToClient();
            }
        });

        // Настраиваем действие на закрытие окна
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                this.windowActivated(e);
                try {
                    dos.writeUTF("/finish");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        setVisible(true);
    }
}
