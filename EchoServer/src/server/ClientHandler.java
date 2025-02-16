package server;

import java.io.*;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final EchoServer server;
    private String username;
    private PrintWriter writer;

    public ClientHandler(Socket socket, EchoServer server) {
        this.socket = socket;
        this.server = server;
        this.username = generateRandomUsername();
    }

    @Override
    public void run() {
        try (socket;
             Scanner reader = getReader(socket)) {

            writer = getWriter(socket);
            sendMessage("Добро пожаловать, " + username + "!");

            while (true) {
                String message = reader.nextLine();
                if (isEmptyMsg(message) || isQuitMsg(message)) {
                    break;
                }
                handleMessage(message);
            }
        } catch (NoSuchElementException ex) {
            System.out.println("Клиент отключился: " + username);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.removeClient(this);
        }
    }

    private void handleMessage(String message) {
        if (message.startsWith("/name ")) {
            changeUsername(message.substring(6).trim());
        } else if (message.equals("/list")) {
            sendMessage(server.getClientList());
        } else if (message.startsWith("/whisper ")) {
            String[] parts = message.split(" ", 3);
            if (parts.length < 3) {
                sendMessage("Ошибка: Используйте /whisper имя сообщение");
                return;
            }
            server.sendPrivateMessage(parts[1], username, parts[2]);
        } else {
            server.broadcastMessage(username, message, this);
        }
    }

    private void changeUsername(String newName) {
        if (newName.contains(" ") || server.isUsernameTaken(newName)) {
            sendMessage("Ошибка: Имя недоступно.");
        } else {
            String oldName = this.username;
            this.username = newName;
            sendMessage("Вы теперь известны как " + newName);
            server.notifyNameChange(oldName, newName);
        }
    }

    private static PrintWriter getWriter(Socket socket) throws IOException {
        return new PrintWriter(socket.getOutputStream(), true);
    }

    private static Scanner getReader(Socket socket) throws IOException {
        return new Scanner(new InputStreamReader(socket.getInputStream(), "UTF-8"));
    }

    private static boolean isQuitMsg(String message) {
        return "bye".equalsIgnoreCase(message);
    }

    private static boolean isEmptyMsg(String message) {
        return message == null || message.isBlank();
    }

    private static String generateRandomUsername() {
        String[] names =
                {"User1", "User2", "User3", "User4", "User5", "User6", "User7"};
        return names[new Random().nextInt(names.length)];
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

    public String getUsername() {
        return username;
    }
}
