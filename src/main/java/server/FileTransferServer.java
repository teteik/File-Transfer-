// server/FileTransferServer.java
package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileTransferServer {
    private final int port;
    private final ExecutorService executor;

    public FileTransferServer(int port) {
        this.port = port;
        this.executor = Executors.newCachedThreadPool();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port + ". Ожидание подключений...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Подключение от: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                executor.submit(handler);
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Использование: java server.FileTransferServer <порт>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        FileTransferServer server = new FileTransferServer(port);
        server.start();
    }
}