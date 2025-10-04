// common/Protocol.java
package common;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Protocol {

    public static void sendFileName(OutputStream out, String fileName) throws IOException {
        DataOutputStream dataOut = new DataOutputStream(out);
        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        dataOut.writeInt(fileNameBytes.length);
        dataOut.write(fileNameBytes);
        dataOut.flush();
    }

    public static String readFileName(InputStream in) throws IOException {
        DataInputStream dataIn = new DataInputStream(in);
        int fileNameLength = dataIn.readInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        dataIn.readFully(fileNameBytes);
        return new String(fileNameBytes, StandardCharsets.UTF_8);
    }

    public static void sendFileSize(OutputStream out, long fileSize) throws IOException {
        DataOutputStream dataOut = new DataOutputStream(out);
        dataOut.writeLong(fileSize);
        dataOut.flush();
    }

    public static long readFileSize(InputStream in) throws IOException {
        DataInputStream dataIn = new DataInputStream(in);
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

        try (BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            byte[] buffer = new byte[8192];
            long totalBytesRead = 0;
            int bytesRead;

            while (totalBytesRead < expectedFileSize && (bytesRead = in.read(buffer)) != -1) {
                long bytesToWrite = Math.min(bytesRead, expectedFileSize - totalBytesRead);
                fileOut.write(buffer, 0, (int) bytesToWrite);
                totalBytesRead += bytesToWrite;
            }

            fileOut.flush();
        }
    }

    public static void sendResult(OutputStream out, boolean success) throws IOException {
        DataOutputStream dataOut = new DataOutputStream(out);
        dataOut.writeBoolean(success);
        dataOut.flush();
    }

    public static boolean readResult(InputStream in) throws IOException {
        DataInputStream dataIn = new DataInputStream(in);
        return dataIn.readBoolean();
    }
}