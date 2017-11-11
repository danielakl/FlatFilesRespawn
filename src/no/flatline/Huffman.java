package no.flatline;

import java.io.File;
import java.util.PriorityQueue;

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
     * Returns the root of the corresponding Huffman Tree to a string
     *
     * @param s the string to get the Huffman Tree for.
     * @return the root of the corresponding Huffman Tree to {@code s}
     */
    private Node getTree(String s) {
        char[] chars = s.toCharArray();
        int[] freq = new int[Character.MAX_VALUE]; // one entry for each possible character
        int diffCharCount = 0; // so priority queue doesn't need to expand later
        for (char c : chars) {
            if (freq[c] == 0) diffCharCount++;
            freq[c]++;
        }
        PriorityQueue<Node> nodes = new PriorityQueue<>(diffCharCount);
        for (char c = 0; c < freq.length; c++) {
            if (freq[c] != 0) {
                nodes.add(new Node(c, freq[c]));
            }
        }
        while (nodes.size() > 1) {
            Node left = nodes.poll();
            Node right = nodes.poll();
            int combinedFreq = left.freq;
            if (right != null) combinedFreq += right.freq;
            Node link = new Node(combinedFreq, left, right);
            nodes.add(link);
        }
        return nodes.poll();
    }

    /**
     * The Node class is used to create a Huffman Tree.
     * Each node stores a character and its frequency.
     *
     * @author Joakim Sæther
     * @author Rolv-Arild Braaten
     * @since 0.0.1
     */
    private class Node implements Comparable<Node> {

        private static final char LINK_CHAR = 0;

        final char character;
        final int freq;
        final Node leftChild;
        final Node rightChild;

        private Node(final char character, final int freq, final Node leftChild, final Node rightChild){
            this.character = character;
            this.freq = freq;
            this.leftChild = leftChild;
            this.rightChild = rightChild;
        }

        Node(final int character, final int freq) {
            this(LINK_CHAR, freq, null, null);
        }

        Node(final int freq, final Node leftChild, final Node rightChild) {
            this(LINK_CHAR, freq, leftChild, rightChild);
        }

        boolean isLeaf(){
            return this.leftChild == null && this.rightChild == null;
        }

        @Override
        public int compareTo(final Node o){
            final int freqCompare = Integer.compare(this.freq, o.freq);
            if (freqCompare != 0) return freqCompare;
            return Integer.compare(this.character, o.character);
        }
    }
}
