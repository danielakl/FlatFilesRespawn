import no.flatline.Compress;
import no.flatline.Decompress;

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
        int blockSize = 32768;
        int mode = COMPRESS;

        if (args.length == 0) {
            System.out.println("No args");
        } else {
            System.out.println("Args");
            for (String arg : args) {
                // Set mode for compression or decompression.
                if (arg.equals("decompress")) {
                    mode = DECOMPRESS;
                // Create list of files to compress or decompress.
                } else if (arg.matches("^*.*$")) {
                    File file = new File(arg);
                    if (file.canRead()) {
                        files.add(file);
                    }
                // Define block size to compress with.
                } else if (arg.matches("^[0-9]+[kKmM]?[bB]$")) {
                    String blockSizeString = arg.toLowerCase();
                    blockSize = Integer.parseInt(blockSizeString.split("[0-9]+")[0]);
                    if (arg.contains("m")) {
                        blockSize <<= 20;
                    } else if (arg.contains("k")) {
                        blockSize <<= 10;
                    }
                }
            }
        }

        switch (mode) {
            case COMPRESS: {
                for (File file : files) {
                    Compress.compress(file, blockSize);
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
