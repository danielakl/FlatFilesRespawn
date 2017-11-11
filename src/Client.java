import no.flatline.Huffman;

import java.io.*;
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
    private static Mode MODE = Mode.COMPRESS;

    /**
     * Give the user a interactive CLI mode.
     */
    private static void cliMode() {
        System.out.println("Welcome to FlatFile's ultimate compression tool\u2122.\n" +
                           "Please select one of the options below:\n" +
                           "[1] - Compress files.\n" +
                           "[2] - Decompress files.\n" +
                           "[3] - Exit.\n" +
                           "Option: ");
        Scanner scanner = new Scanner(System.in);
        int option = scanner.nextInt();

    }

    /**
     * Parse commandline arguments.
     * @param args - The arguments from the CLI.
     */
    private static void parseArgs(String[] args) {
        for (String arg : args) {
            // Set mode for compression or decompression.
            if (arg.equals("decompress")) {
                MODE = Mode.DECOMPRESS;
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
            case DECOMPRESS: {
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
