import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Login extends JFrame {

    DataOutputStream dos;
    DataInputStream dis;
    private JTextField login;
    private JTextField password;

    public Login(DataOutputStream dos, DataInputStream dis) throws HeadlessException {
        this.dos = dos;
        this.dis = dis;
    }

    private void loginGUI() {
        // Параметры окна
        setBounds(600, 300, 240, 150);
        setTitle("LogIn");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        // Нижняя панель с полем для ввода сообщений и кнопкой отправки сообщений
        JPanel panel = new JPanel();
        Label textlogin = new Label("LogIn");
        Label pass = new Label("Password");
        panel.setLayout(null);
        textlogin.setBounds(40, 15, 80, 20);
        panel.add(textlogin);
        pass.setBounds(140, 15, 100, 20);
        panel.add(pass);
        login = new JTextField();
        login.setBounds(10, 40, 100, 20);
        panel.add(login);
        password = new JTextField();
        password.setBounds(120, 40, 100, 20);
        panel.add(password);
        JButton btnOk = new JButton("Войти");
        JButton btnCancel = new JButton("Отмена");
        btnOk.setBounds(15, 70, 90, 30);
        panel.add(btnOk);
        btnCancel.setBounds(125, 70, 90, 30);
        panel.add(btnCancel);
        btnOk.addActionListener(e -> sendMessageFromLoginForm());
        btnCancel.addActionListener(e -> dispose());
        getContentPane().add(panel);
        setVisible(true);
    }

    private void sendMessageFromLoginForm() {
        String msgLogin = login.getText();
        String msgPassword = password.getText();
        String msgToServer = "/start-".concat(msgLogin).concat("-").concat(msgPassword);
        System.out.println("Message to server: " + msgToServer);
        if (!msgToServer.trim().isEmpty()) {
            try {
                dos.writeUTF(msgToServer);
                login.setText("");
                password.setText("");
                login.grabFocus();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "You send incorrect message");
            }
        }
    }

    public boolean loginToChat() {
        loginGUI();
        AtomicBoolean flagTimer = new AtomicBoolean(false);
        Thread thread = new Thread(() -> {
            synchronized (flagTimer) {
                try {
                    flagTimer.wait(120000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            flagTimer.set(true);
        });
        thread.setDaemon(true);
        thread.start();
        while (true) {
            try {
                if (flagTimer.get()) {
                    dispose();
                    return false;
                }
                synchronized (flagTimer) {
                    if (dis.available() > 0) {
                        String message = dis.readUTF();
                        if (message.equals("Login completed")) {
                            flagTimer.notify();
                            dispose();
                            return true;
                        } else {
                            JOptionPane.showMessageDialog(null, "Wrong login or password!");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
