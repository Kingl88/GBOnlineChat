package am.home.service;

import am.home.handler.ClientHandler;
import am.home.service.interfaces.AuthenticationService;

import javax.swing.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {

    private static final Integer PORT = 8886;

    private AuthenticationService authenticationService;
    private List<ClientHandler> handlerList;

    public MyServer() {
        System.out.println("Server started");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            authenticationService = new AuthenticationServiceImpl();
            authenticationService.start();
            handlerList = new ArrayList<>();
            while (true) {
                System.out.println("Server wait connections ...");
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");
                new ClientHandler(this, socket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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
                JOptionPane.showMessageDialog(null, "You're trying to send a message to yourself");
            } else {
                clientTo.sendMessage("Message from " + ch.getNickName() + ": " + message);
                ch.sendMessage("Message to " + clientTo.getNickName() + ": " + message);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Nickname not found...");
        }
    }

    public synchronized void sendOnline(ClientHandler client) {
        client.sendMessage("Users online");
        for (ClientHandler ch : handlerList) {
            if (!ch.equals(client)) {
                client.sendMessage(ch.getNickName());
            }
        }
    }

    public void sendMessageToAllClient(String message) {
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
}
