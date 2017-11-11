import no.flatline.Huffman;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author Joppe
 * @author Daniel Klock
 * @version 0.1.0
 */
public final class Client {
    private static final List<File> files = new ArrayList<>();
    private static Mode MODE = Mode.Compress;

    /**
     * Give the user a interactive CLI mode.
     */
    private static int cliMode() {
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        int EXIT = 0;

        System.out.println("Welcome to FlatFile's ultimate compression tool\u2122.");

        do {
            System.out.println("Please select one of the options below:\n" +
                    "[1] - Compress files.\n" +
                    "[2] - Decompress files.\n" +
                    "[3] - Exit.\n" +
                    "Option: ");
            Scanner scanner = new Scanner(System.in);
            int option = scanner.nextInt();
            switch (option) {
                case 1:
                    MODE = Mode.Compress;
                    break;
                case 2:
                    MODE = Mode.Decompress;
                    break;
                default:
                    return EXIT;
            }
            do {
                System.out.println("Add files to " + MODE.name().toLowerCase() + ".");
                if (files.size() != 0) {
                    System.out.println("Files added for " + (MODE.name().toLowerCase() + "ion") + ":");
                    for (File file : files) {
                        System.out.println("\t" + file.getPath());
                    }
                }
                System.out.println("\nCurrent directory: " + currentPath.toString());
                System.out.println("[0]\t..");
                File[] filesInCurrentDir = currentPath.toFile().listFiles();
                for (int i = 0; i < filesInCurrentDir.length; i++) {
                    System.out.println("[" + (i + 1) + "]\t" + filesInCurrentDir[i].getName());
                }
                System.out.println("[" + (filesInCurrentDir.length + 1) + "]\t" + MODE.name());
                scanner = new Scanner(System.in);
                String input = scanner.next();
                if (input.matches("^[0-9]+$")) {
                    int inputInt = Integer.parseInt(input);
                    if (inputInt == 0) {
                        // Change path to parent directory.
                    } else if (inputInt == (filesInCurrentDir.length + 1)) {
                        executeAction(MODE);
                    } else {
                        if (filesInCurrentDir[inputInt - 1].isFile()) {

                        } else if (filesInCurrentDir[inputInt - 1].isDirectory()) {
                            // Change currentPath
                        }
                    }
                }
                return 0;
            } while (true);
        } while (true);
    }

    /**
     * Parse commandline arguments.
     * @param args - The arguments from the CLI.
     */
    private static void parseArgs(String[] args) {
        for (String arg : args) {
            // Set mode for compression or decompression.
            if (arg.equals("decompress")) {
                MODE = Mode.Decompress;
                // Create list of files to compress or decompress.
            } else if (arg.matches("^.+\\..+$")) {
                File file = new File(arg);
                if (file.canRead()) {
                    files.add(file);
                }
            }
        }
        executeAction(MODE);
    }

    /**
     * Compress or Decompress all files currently within the files array.
     * @param action - Compress or Decompress files.
     */
    private static void executeAction(Mode action) {
        Huffman h = new Huffman();

        switch (action) {
            case Decompress: {
                for (File file : files) {
                    h.decompress(file);
                }
                break;
            }
            default: {
                for (File file : files) {
                    h.compress(file);
                }
                break;
            }
        }
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
