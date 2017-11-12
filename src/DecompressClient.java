import no.flatline.Huffman;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author Joppe
 * @author Daniel Klock
 * @author Roy Åne Sylthe
 * @version 0.1.1
 */
public final class DecompressClient {
    private static final String printfExp = "%1$-5s %2$s %n";

    private static final List<File> files = new ArrayList<>();

    /**
     * Give the user a interactive CLI mode.
     */
    private static void cliMode() {
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        System.out.println("Welcome to FlatFile's ultimate decompression tool\u2122.");
        do {
            System.out.println("Add files to decompress.");
            if (files.size() != 0) {
                System.out.println("Files added for decompression:");
                for (File file : files) {
                    System.out.println("\t" + file.getPath());
                }
            }
            System.out.println("\nCurrent directory: " + currentPath.toString());
            System.out.printf(printfExp,"[0]","..");
            File[] filesInCurrentDir = currentPath.toFile().listFiles();
            for (int i = 0; i < filesInCurrentDir.length; i++) {
                System.out.printf(printfExp,"[" + (i + 1) + "]", filesInCurrentDir[i].getName());
            }
            System.out.printf(printfExp,"[" + (filesInCurrentDir.length + 1) + "]","decompress");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.next();
            if (input.matches("^[0-9]+$")) {
                int inputInt = Integer.parseInt(input);
                if (inputInt == 0) {
                    currentPath = (currentPath.getParent() != null) ? currentPath.getParent() : currentPath;
                } else if (inputInt == (filesInCurrentDir.length + 1)) {
                    System.out.println("Decompressing...");
                    double time = decompress() / 1000.0;
                    System.out.println("Time spent decompressing: " + time + " seconds.");
                    files.clear();
                    break;
                } else {
                    File selectedFile = filesInCurrentDir[inputInt - 1];
                    if (selectedFile.isFile()) {
                        files.add(selectedFile);
                    } else if (selectedFile.isDirectory()) {
                        currentPath = currentPath.resolve(selectedFile.toPath());
                    }
                }
            }
        } while (true);
    }

    /**
     * Parse commandline arguments.
     * @param args - The arguments from the CLI.
     */
    private static void parseArgs(String[] args) {
        for (String arg : args) {
            // Create list of files to decompress.
            if (arg.matches("^.+\\..+$")) {
                File file = new File(arg);
                if (file.canRead()) {
                    files.add(file);
                }
            }
        }
        decompress();
    }

    /**
     * Decompress all files currently within the files array.
     */
    private static long decompress() {
        Huffman h = new Huffman();

        long start = System.currentTimeMillis();
        for (File file : files) {
            h.decompress(file);
        }
        return System.currentTimeMillis() - start;
    }

    /**
     * Give the user an interactive CLI mode, or execute all commands given
     * through commandline arguments.
     * @param args - Arguments given through the commandline.
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            cliMode();
        } else {
            parseArgs(args);
        }
    }
}
