package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EchoServer {
    private final int port;
    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    private EchoServer(int port) {
        this.port = port;
    }

    public static EchoServer bindToPort(int port) {
        return new EchoServer(port);
    }

    public void run() {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);
            while (!server.isClosed()) {
                Socket socket = server.accept();
                ClientHandler clientHandler = new ClientHandler(socket, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void broadcastMessage(String sender, String message, ClientHandler senderClient) {
        for (ClientHandler client : clients) {
            if (client != senderClient) {
                client.sendMessage(sender + ": " + message);
            }
        }
    }

    public void sendPrivateMessage(String recipientName, String sender, String message) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equalsIgnoreCase(recipientName)) {
                client.sendMessage("(Сообщение от " + sender + "): " + message);
                return;
            }
        }
    }

    public boolean isUsernameTaken(String newName) {
        return clients.stream().anyMatch(client -> client.getUsername().equalsIgnoreCase(newName));
    }

    public void notifyNameChange(String oldName, String newName) {
        for (ClientHandler client : clients) {
            if (!client.getUsername().equalsIgnoreCase(newName)) {
                client.sendMessage("Пользователь " + oldName + " теперь известен как " + newName);
            }
        }
    }
    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    public String getClientList() {
        StringBuilder sb = new StringBuilder("Подключенные пользователи: ");
        for (ClientHandler client : clients) {
            sb.append(client.getUsername()).append(", ");
        }
        return sb.toString().replaceAll(", $", "");
    }
}
