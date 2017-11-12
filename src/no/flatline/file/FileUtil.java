package no.flatline.file;

import com.sun.istack.internal.Nullable;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Daniel Klock
 * @version 0.1.0
 */
public final class FileUtil {
    public static File createFile(String path, String filename) throws IOException {
        Path filePath = Paths.get(path, filename);
        String baseFilename = FilenameUtils.getBaseName(filename);
        String extension = FilenameUtils.getExtension(filename);
        for (int i = 1; filePath.toFile().exists(); i++) {
            filePath = Paths.get(path, baseFilename, " (" + i + ")." + extension);
        }
        return Files.createFile(filePath).toFile();
    }

    public static File createFile(String path, String filename, String extension) throws IOException {
        Path filePath = Paths.get(path, filename + "." + extension);
        for (int i = 1; filePath.toFile().exists(); i++) {
            filePath = Paths.get(path, filename, " (" + i + ")." + extension);
        }
        return Files.createFile(filePath).toFile();
    }

    public static String getBaseName(File file) {
        return FilenameUtils.getBaseName(file.getName());
    }

    public static String getExtension(File file) {
        return FilenameUtils.getExtension(file.getName());
    }
}
