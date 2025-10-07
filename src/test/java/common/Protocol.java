package common;

import utils.SpeedMonitor;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Protocol {
    private final int DEFAULT_BUFFER_SIZE = 8192;
    private long totalBytesRead = 0;


    public void sendFileName(OutputStream out, String fileName) throws IOException {
        DataOutputStream dataOut = new DataOutputStream(out);
        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        dataOut.writeInt(fileNameBytes.length);
        dataOut.write(fileNameBytes);
        dataOut.flush();
    }

    public String readFileName(InputStream in) throws IOException {
        DataInputStream dataIn = new DataInputStream(in);
        int fileNameLength = dataIn.readInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        dataIn.readFully(fileNameBytes);

        return new String(fileNameBytes, StandardCharsets.UTF_8);
    }

    public void sendFileSize(OutputStream out, long fileSize) throws IOException {
        DataOutputStream dataOut = new DataOutputStream(out);
        dataOut.writeLong(fileSize);
        dataOut.flush();
    }

    public long readFileSize(InputStream in) throws IOException {
        DataInputStream dataIn = new DataInputStream(in);
        return dataIn.readLong();
    }

    public void sendFile(OutputStream out, File file) throws IOException {
        try (BufferedInputStream fileIn = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fileIn.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }

    public void readFile(InputStream in, File outputFile, long expectedFileSize) throws IOException {
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + parentDir.getAbsolutePath());
            }
        }

        try (BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int bytesRead;

            while (totalBytesRead < expectedFileSize && (bytesRead = in.read(buffer)) != -1) {
                long bytesToWrite = Math.min(bytesRead, expectedFileSize - totalBytesRead);
                fileOut.write(buffer, 0, (int) bytesToWrite);
                totalBytesRead += bytesToWrite;
            }

            fileOut.flush();
        }
    }

    public void sendResult(OutputStream out, boolean success) throws IOException {
        DataOutputStream dataOut = new DataOutputStream(out);
        dataOut.writeBoolean(success);
        dataOut.flush();
    }

    public boolean readResult(InputStream in) throws IOException {
        DataInputStream dataIn = new DataInputStream(in);
        return dataIn.readBoolean();
    }

    public long getTotalBytesRead() {
        return totalBytesRead;
    }
}