package common;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Protocol {

    public static void sendFileName(OutputStream out, String fileName) throws IOException {
        BufferedOutputStream bufferedOut = new BufferedOutputStream(out);
        DataOutputStream dataOut = new DataOutputStream(bufferedOut);
        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        dataOut.writeInt(fileNameBytes.length);
        dataOut.write(fileNameBytes);
        dataOut.flush();
    }

    public static String readFileName(InputStream in) throws IOException {
        BufferedInputStream bufferedIn = new BufferedInputStream(in);
        DataInputStream dataIn = new DataInputStream(bufferedIn);
        int fileNameLength = dataIn.readInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        dataIn.readFully(fileNameBytes);
        return new String(fileNameBytes, StandardCharsets.UTF_8);
    }

    public static void sendFileSize(OutputStream out, long fileSize) throws IOException {
        BufferedOutputStream bufferedOut = new BufferedOutputStream(out);
        DataOutputStream dataOut = new DataOutputStream(bufferedOut);
        dataOut.writeLong(fileSize);
        dataOut.flush();
    }

    public static long readFileSize(InputStream in) throws IOException {
        BufferedInputStream bufferedIn = new BufferedInputStream(in);
        DataInputStream dataIn = new DataInputStream(bufferedIn);
        return dataIn.readLong();
    }

    public static void sendFile(OutputStream out, File file) throws IOException {
        try (BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = fileIn.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();

        }
    }

    public static void receiveFile(InputStream in, File outputFile, long expectedFileSize) throws IOException {
        File parentDir = outputFile.getParentFile();
        if (parentDir != null) {
            parentDir.mkdirs();
        }

        try (BufferedInputStream bufferedIn = new BufferedInputStream(in);
             BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(outputFile))) {

            byte[] buffer = new byte[8192];
            long totalBytesRead = 0;
            int bytesRead;

            while (totalBytesRead < expectedFileSize && (bytesRead = bufferedIn.read(buffer)) != -1) {
                long bytesToWrite = Math.min(bytesRead, expectedFileSize - totalBytesRead);
                fileOut.write(buffer, 0, (int) bytesToWrite);
                totalBytesRead += bytesToWrite;
            }

            fileOut.flush();
        }
    }
}