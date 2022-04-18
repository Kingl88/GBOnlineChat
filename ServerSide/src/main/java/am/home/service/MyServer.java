package am.home.service;

import am.home.handler.ClientHandler;
import am.home.service.db.DBConnection;
import am.home.service.interfaces.AuthenticationService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyServer {

    private static final Integer PORT = 8886;
    private final static Logger LOGGER = LogManager.getLogger(MyServer.class.getName());
    private AuthenticationService authenticationService;
    private List<ClientHandler> handlerList;
    private static Connection dbConnection;
    private static Statement statement;
    private static ExecutorService executorService = Executors.newCachedThreadPool();

    public MyServer() {
        LOGGER.atLevel(Level.ALL);
        LOGGER.info("Server started");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            dbConnection = DBConnection.getConnection();
            statement = dbConnection.createStatement();
            authenticationService = new AuthenticationServiceImpl(statement);
            authenticationService.start();
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
//                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
//                    "login TEXT," +
//                    "password TEXT," +
//                    "nickName TEXT" + ")");
//            statement.executeUpdate("INSERT INTO users (login, password, nickName)"
//                    + "VALUES ('A', 'A', 'A');");
//            statement.executeUpdate("INSERT INTO users (login, password, nickName)"
//                    + "VALUES ('B', 'B', 'B');");
//            statement.executeUpdate("INSERT INTO users (login, password, nickName)"
//                    + "VALUES ('C', 'C', 'C');");
//            statement.executeUpdate("INSERT INTO users (login, password, nickName)"
//                    + "VALUES ('D', 'D', 'D');");
            handlerList = new ArrayList<>();
            while (true) {
                LOGGER.info("Server wait connections ...");
                Socket socket = serverSocket.accept();
                LOGGER.info("Client connected");
                new ClientHandler(this, socket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
            authenticationService.stop();
        }
    }

    public synchronized boolean nickNameIsBusy(String nickName) {
        return handlerList.stream().anyMatch(clientHandler -> clientHandler.getNickName().equalsIgnoreCase(nickName));
    }

    public void sendMessageToClient(ClientHandler ch, String to, String message) {
        ClientHandler clientTo = handlerList.stream().filter(clientHandler -> clientHandler.getNickName().equals(to)).findAny().orElse(null);
        if (clientTo != null) {
            if (clientTo.equals(ch)) {
                LOGGER.info(message);
                JOptionPane.showMessageDialog(null, "You're trying to send a message to yourself");
            } else {
                clientTo.sendMessage("Message from " + ch.getNickName() + ": " + message);
                LOGGER.info(message);
                ch.sendMessage("Message to " + clientTo.getNickName() + ": " + message);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Nickname not found...");
        }
    }

    public synchronized void sendOnline(ClientHandler client) {
        LOGGER.info("Users online");
        client.sendMessage("Users online");
        for (ClientHandler ch : handlerList) {
            if (!ch.equals(client)) {
                LOGGER.info(ch.getNickName());
                client.sendMessage(ch.getNickName());
            }
        }
    }

    public void sendMessageToAllClient(String message) {
        File file = new File("serverLog.txt");
        file.deleteOnExit();
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, true))) {
            bos.write((message + "\n").getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info(message);
        handlerList.forEach(clientHandler -> clientHandler.sendMessage(message));
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        handlerList.add(clientHandler);
    }

    public synchronized void unSubscribe(ClientHandler clientHandler) {
        handlerList.remove(clientHandler);
    }

    public AuthenticationService getAuthenticationService() {
        return this.authenticationService;
    }

    public Statement getStatement() {
        return statement;
    }

    public ExecutorService getService() {
        return executorService;
    }
}
