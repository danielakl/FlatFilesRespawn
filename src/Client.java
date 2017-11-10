import no.flatfiles.Compress;
import no.flatfiles.Decompress;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joppe
 * @author Daniel Klock
 * @version 0.1.0
 */
public class Client {
    private static final int COMPRESS = 0;
    private static final int DECOMPRESS = 1;

    public static void main(String[] args) {
        List<File> files = new ArrayList<>();
        int mode = COMPRESS;

        if (args.length == 0) {
            System.out.println("No args");
        } else {
            for (String arg : args) {
                // Set mode for compression or decompression.
                if (arg.equals("decompress")) {
                    mode = DECOMPRESS;
                // Create list of files to compress or decompress.
                } else if (arg.matches("^.+\\..+$")) {
                    File file = new File(arg);
                    if (file.canRead()) {
                        files.add(file);
                    }
                }
            }
        }

        switch (mode) {
            case COMPRESS: {
                for (File file : files) {
                    Compress.compress(file);
                }
                break;
            }
            case DECOMPRESS: {
                for (File file : files) {
                    Decompress.decompress(file);
                }
                break;
            }
        }
    }
}
