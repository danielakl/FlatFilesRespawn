package no.flatline;

import java.io.File;

/**
 * @author Rolv-Arild Braaten
 * @version 0.0.1
 */
public interface Compressor {

    /**
     * Compresses a file into a different file.
     *  @param src the source file to compress.
     *
     */
    void compress(File src);

    /**
     * Decompresses a file into a different file.
     *  @param src the source file to decompress.
     *
     */
    void decompress(File src);
}
