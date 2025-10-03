package Test;

import common.Protocol;

import java.io.*;
import java.net.Socket;

public class TestClient {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;
    private static final String FILE_PATH = "../resources/example.txt"; // или укажи нужный путь

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            System.out.println("Подключились к серверу по адресу: " + SERVER_HOST + ":" + SERVER_PORT);

            File fileToSend = new File(FILE_PATH);

            if (!fileToSend.exists() || !fileToSend.isFile()) {
                System.err.println("Файл не найден: " + fileToSend.getAbsolutePath());
                return;
            }

            try (OutputStream out = socket.getOutputStream();
                 InputStream in = socket.getInputStream()) {

                Protocol.sendFileName(out, fileToSend.getName());
                Protocol.sendFileSize(out, fileToSend.length());
                Protocol.sendFile(out, fileToSend);

                System.out.println("Файл успешно отправлен: " + fileToSend.getName());
            }

        } catch (IOException e) {
            System.err.println("Ошибка при работе с клиентом: " + e.getMessage());
            e.printStackTrace();
        }
    }
}