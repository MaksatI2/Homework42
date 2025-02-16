package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class EchoServer {
    private final int port;

    private EchoServer(int port) {
        this.port = port;
    }

    public static EchoServer bindToPort(int port) {
        return new EchoServer(port);
    }

    public void run() {
        try (ServerSocket server = new ServerSocket(port)) {
            while (!server.isClosed()) {
                Socket socket = server.accept();
                System.out.println("Клиент подключился.");
                new Thread(new ClientHandler(socket)).run();

            }

        } catch (IOException e) {
            System.out.printf("Ошибка: порт %d занят.%n", port);
            e.printStackTrace();
        }
    }

}
