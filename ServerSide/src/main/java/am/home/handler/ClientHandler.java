package am.home.handler;

import am.home.service.MyServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientHandler {

    private MyServer myServer;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Statement statement;
    private Date date;

    private String nickName;

    public ClientHandler(MyServer myServer, Socket socket, Statement statement) {
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.statement = statement;
            this.date = new Date();
            Thread thread = new Thread(() -> {
                try {
                    authentication();
                    receiveMessage();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            });
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void authentication() throws Exception {
        while (true) {
            String message;
            if (dis.available() > 0) {
                message = dis.readUTF();
                if (message.startsWith("/start")) {
                    String[] arr = message.split("-", 3);
                    if (arr.length != 3) {
                        throw new IllegalAccessException();
                    }
                    final String nick = myServer.getAuthenticationService().getNickNameByLoginAndPassword(arr[1].trim(), arr[2].trim());
                    if (nick != null) {
                        if (!myServer.nickNameIsBusy(nick)) {
                            sendMessage("Login completed");
                            this.nickName = nick;
                            myServer.sendMessageToAllClient(nickName + " connected to chat");
                            myServer.subscribe(this);
                            return;
                        } else {
                            sendMessage("You nick now busy");
                        }
                    } else {
                        sendMessage("Wrong login or password");
                    }
                }
            }
        }
    }

    public void sendMessage(String message) {
        try {
            dos.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveMessage() throws IOException {
        while (true) {
            String message = dis.readUTF();
            if (message.startsWith("/online")) {
                myServer.sendOnline(this);
            } else if (message.startsWith("/w")) {
                myServer.sendMessageToClient(this, message.split("-")[1].trim(), message.split("-")[2].trim());
            } else if (message.startsWith("/finish")) {
                myServer.sendMessageToAllClient(nickName + " exit from chat");
                closeConnection();
                return;
            } else if (message.startsWith("/rn")) {
                try {
                    statement.executeUpdate("UPDATE users SET nickName = '" + message.split("-")[1].trim() + "' WHERE nickName = '" + nickName + "';");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                myServer.sendMessageToAllClient(nickName + " changed his nickname to " + message.split("-")[1].trim());
                nickName=message.split("-")[1].trim();
            } else {
                myServer.sendMessageToAllClient(date() + nickName + ": " + message);
            }
        }
    }

    private String date(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("[HH:mm]");
        return simpleDateFormat.format(date);
    }

    public String getNickName() {
        return nickName;
    }

    private void closeConnection() {
        myServer.unSubscribe(this);
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
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
