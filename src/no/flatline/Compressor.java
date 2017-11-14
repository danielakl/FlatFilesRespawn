package no.flatline;

import java.io.File;

/**
 * @author Rolv-Arild Braaten
 * @since 0.0.1
 * @version 1.0.0
 */
public interface Compressor {

    /**
     * Compresses a file.
     *
     *  @param src the source file to compress.
     */
    void compress(File src);

    /**
     * Decompresses a file.
     *
     *  @param src the source file to decompress.
     */
    void decompress(File src);
}
