// client/FileTransferClient.java
package client;

import common.Protocol;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class FileTransferClient {
    private final String filePath;
    private final String serverHost;
    private final int serverPort;

    public FileTransferClient(String filePath, String serverHost, int serverPort) {
        this.filePath = filePath;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public void sendFile() {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.err.println("Файл не существует: " + filePath);
            return;
        }

        try (Socket socket = new Socket(serverHost, serverPort);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {
            Protocol protocol = new Protocol();

            protocol.sendFileName(out, file.getName());
            System.out.println("Отправлено имя файла: " + file.getName());

            protocol.sendFileSize(out, file.length());
            System.out.println("Отправлен размер файла: " + file.length() + " байт");

            protocol.sendFile(out, file);
            System.out.println("Файл отправлен.");

            boolean success = protocol.readResult(in);

            if (success) {
                System.out.println("Передача файла успешна.");
            } else {
                System.err.println("Передача файла не удалась.");
            }

        } catch (UnknownHostException e) {
            System.err.println("Неизвестный хост: " + serverHost);
        } catch (IOException e) {
            System.err.println("Ошибка при передаче файла: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Использование: java client.FileTransferClient <путь_к_файлу> <хост_сервера> <порт_сервера>");
            System.exit(1);
        }

        String filePath = args[0];
        String serverHost = args[1];
        int serverPort = Integer.parseInt(args[2]);

        FileTransferClient client = new FileTransferClient(filePath, serverHost, serverPort);
        client.sendFile();
    }
}