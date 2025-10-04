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

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.speedMonitor = new SpeedMonitor(socket.getInetAddress().toString());
    }

    @Override
    public void run() {
        boolean success = false;
        try (InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {

            // Читаем имя файла
            String fileName = Protocol.readFileName(in);
            System.out.println("Получено имя файла: " + fileName);

            // Читаем размер файла
            long fileSize = Protocol.readFileSize(in);
            System.out.println("Ожидаемый размер файла: " + fileSize + " байт");

            // Валидируем и получаем целевой файл
            File targetFile = SavePathValidator.validateAndResolve(fileName, "uploads");

            // Получаем файл
            Protocol.receiveFile(in, targetFile, fileSize);

            // Проверяем размер
            long actualSize = targetFile.length();
            success = actualSize == fileSize;

            if (success) {
                System.out.println("Файл '" + fileName + "' успешно сохранён.");
            } else {
                System.err.println("Ошибка: ожидаемый размер " + fileSize + ", получен " + actualSize);
                targetFile.delete(); // удаляем повреждённый файл
            }

            // Отправляем результат клиенту
            Protocol.sendResult(out, success);

            // Выводим финальную скорость, если клиент был активен менее 3 секунд
            speedMonitor.printSpeed();

        } catch (Exception e) {
            System.err.println("Ошибка при обработке клиента: " + e.getMessage());
        }
    }
}