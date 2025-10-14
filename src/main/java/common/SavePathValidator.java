package common;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SavePathValidator {
    public static File validateAndResolve(String fileName, String baseDirectory) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("The file name cannot be empty");
        }

        if (fileName.getBytes(StandardCharsets.UTF_8).length > 4096) {
            throw new IllegalArgumentException("Too long filename (more than 4096 bytes UTF-8)");
        }

        Path basePath = Paths.get(baseDirectory).normalize();
        Path targetPath = basePath.resolve(fileName).normalize();

        if (!targetPath.startsWith(basePath)) {
            throw new IllegalArgumentException("Invalid file name: " +
                    "attempt to move outside the uploads directory");
        }

        File targetFile = targetPath.toFile();
        targetFile = getUniqueFile(targetFile);

        return targetFile;
    }

    private static File getUniqueFile(File originalFile) {
        if (!originalFile.exists()) {
            return originalFile;
        }

        File parentDir = originalFile.getParentFile();
        String fileName = originalFile.getName();
        String nameWithoutExt = fileName;
        String extension = "";
        int lastDotIndex = fileName.lastIndexOf('.');

        if (lastDotIndex > 0) {
            nameWithoutExt = fileName.substring(0, lastDotIndex);
            extension = fileName.substring(lastDotIndex);
        }

        int counter = 1;
        String newName = nameWithoutExt + "(" + counter + ")" + extension;
        File newFile = new File(parentDir, newName);

        while (newFile.exists()) {
            counter++;
            newName = nameWithoutExt + "(" + counter + ")" + extension;
            newFile = new File(parentDir, newName);
        }

        return newFile;
    }
}