// server/ClientHandler.java
package server;

import common.Protocol;
import common.SavePathValidator;
import utils.SpeedMonitor;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final SpeedMonitor speedMonitor;
    private final Protocol protocol;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.protocol = new Protocol();
        this.speedMonitor = new SpeedMonitor(socket.getInetAddress().toString(), protocol);
    }

    @Override
    public void run() {
        boolean success = false;
        try (InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {

            String fileName = protocol.readFileName(in);
            System.out.println("Получено имя файла: " + fileName);

            long fileSize = protocol.readFileSize(in);
            System.out.println("Ожидаемый размер файла: " + fileSize + " байт");

            File targetFile = SavePathValidator.validateAndResolve(fileName, "uploads");

            protocol.readFile(in, targetFile, fileSize);
            speedMonitor.shutdown();

            long actualSize = targetFile.length();
            success = actualSize == fileSize;

            if (success) {
                System.out.println("Файл '" + fileName + "' успешно сохранён.");
            } else {
                System.err.println("Ошибка: ожидаемый размер " + fileSize + ", получен " + actualSize);
                targetFile.delete();
            }

            protocol.sendResult(out, success);
        } catch (Exception e) {
            System.err.println("Ошибка при обработке клиента: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }
}