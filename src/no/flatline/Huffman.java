package no.flatline;

import java.io.File;

/**
 * @author Rolv-Arild Braaten
 * @version 0.0.1
 */
public class Huffman implements Compressor {

    private static final int DEFAULT_BLOCK_SIZE = 256;
    private final int blockSize;

    /**
     * Default constructor.
     */
    public Huffman() {
        this(DEFAULT_BLOCK_SIZE);
    }

    /**
     * Creates a Huffman compressor with the specified block size.
     * To decompress correctly it is essential that a
     * Huffman compressor with the same block size is used.
     *
     * @param blockSize the block size to compress with.
     */
    public Huffman(int blockSize) {
        this.blockSize = blockSize;
    }

    @Override
    public void compress(File src) {

    }

    @Override
    public void decompress(File src) {

    }

    /**
     * @author Joakim Sæther
     * @since 0.0.1
     */
    private class Node implements Comparable<Node>{

        final char character;
        final int freq;
        final Node leftChild;
        final Node rightChild;

        Node(final char character, final int freq, final Node leftChild, final Node rightChild){
            this.character = character;
            this.freq = freq;
            this.leftChild = leftChild;
            this.rightChild = rightChild;
        }

        boolean isLeaf(){
            return this.leftChild == null && this.rightChild == null;
        }

        @Override
        public int compareTo(final Node o){
            final int freqCompare = Integer.compare(this.freq, o.freq);
            if(freqCompare != 0)return freqCompare;
            return Integer.compare(this.character, o.character);
        }
    }
}
