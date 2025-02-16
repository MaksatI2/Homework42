package server;

import java.io.*;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.printf("Подключен клиент: %s%n", socket);

        try (socket;
             Scanner reader = getReader(socket);
             PrintWriter writer = getWriter(socket)) {
            sendResponse("Привет " + socket, writer);

            while (true) {
                String message = reader.nextLine();
                if (isEmptyMsg(message) || isQuitMsg(message)) {
                    break;
                }
                sendResponse(message.toUpperCase(), writer);
            }
        } catch (NoSuchElementException ex) {
            System.out.println("Клиент закрыл соединение!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.printf("Клиент отключен: %s%n", socket);
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

    private static void sendResponse(String response, Writer writer) throws IOException {
        writer.write(response + System.lineSeparator());
        writer.flush();
    }
}
