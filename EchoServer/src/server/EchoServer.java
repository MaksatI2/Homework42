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
                System.out.println("Клиент подключился.");
                ClientHandler clientHandler = new ClientHandler(socket, this);
                clients.add(clientHandler);
                new Thread(clientHandler).run();
            }
        } catch (IOException e) {
            System.out.printf("Ошибка: порт %d занят.%n", port);
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

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        System.out.println("Клиент отключен: " + clientHandler.getUsername());
    }
}
