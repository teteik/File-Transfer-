package Test;

import common.Protocol;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TestServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Сервер запущен на порту 8080...");

        Socket clientSocket = serverSocket.accept();
        System.out.println("Клиент подключился.");

        try (InputStream in = clientSocket.getInputStream();
             OutputStream out = clientSocket.getOutputStream()) {

            String fileName = Protocol.readFileName(in);
            long fileSize = Protocol.readFileSize(in);

            System.out.println("Получаем файл: " + fileName + ", размер: " + fileSize + " байт");

            File outputFile = new File("received", "received_" + fileName);
            System.out.println("Создаем файл по пути: " + outputFile.getAbsolutePath());

            Protocol.receiveFile(in, outputFile, fileSize);

            if (outputFile.exists()) {
                System.out.println("Файл успешно записан. Размер: " + outputFile.length() + " байт");
            } else {
                System.err.println("Файл не был создан!");
            }

            System.out.println("Файл получен: " + outputFile.getAbsolutePath());
        }

        clientSocket.close();
        serverSocket.close();
    }
}