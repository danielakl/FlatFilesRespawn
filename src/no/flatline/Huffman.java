package no.flatline;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * @author Rolv-Arild Braaten
 * @author Daniel Klock
 * @author Joakim Sæther
 * @version 0.0.2
 */
public class Huffman implements Compressor {

    private static final int DEFAULT_BLOCK_SIZE = 256;
    private int blockSize;

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
        try {
            calcBlockSize(src);
            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(src)));
            Path compFilePath = src.toPath().getParent().resolve(src.getName() + ".cff");
            File compFile = Files.createFile(compFilePath).toFile();
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(compFile)));
            byte[] b = new byte[blockSize];
            int off = 0;
            int len = blockSize;
            dis.readFully(b, off, len);
            String s = new String(b);
            while(s != null) {
                Node root = getTree(new String(b));
                final StringBuilder sb = new StringBuilder();
                final Map<Character, String> table = buildTable(root);
                for(final char character : s.toCharArray()){
                    if(table.get(character) != null){
                        sb.append(table.get(character));
                    }
                }
                off += blockSize;
                dis.readFully(b, off, len);
                s = new String(b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void decompress(File src) {

    }

    /**
     * Creates a map where the characters can be mapped to their corresponding compressed bit string.
     * @param root is the root node of the Huffman tree.
     * @return a map with the characters used in the text mapped to their bit string.
     */
    private Map<Character, String> buildTable(final Node root){
        final Map<Character, String> table = new HashMap<>();
        buildTableImpl(root, "", table);
        return table;
    }

    /**
     * Maps the characters to their corresponding compressed bit string.
     * @param node is the root node of the Huffman tree.
     * @param s is the compressed string at the current location in the Huffman tree.
     * @param table is a table with all the used characters.
     */
    private void buildTableImpl(final Node node, final String s, final Map<Character, String> table){
        if(!node.isLeaf()){
            buildTableImpl(node.leftChild, s + '0', table);
            buildTableImpl(node.rightChild, s + '1', table);
        } else{
            table.put(node.character, s);
        }
    }

    /**
     * Uses an estimate of bytes available to be read from the file. This estimate
     * is used to calculate a fitting block size to use with the algorithm.
     * @param src - The file to calculate the block size from.
     * @throws IOException - Throws any IOExceptions.
     */
    private void calcBlockSize(File src) throws IOException {
        DataInputStream dis = new DataInputStream(new FileInputStream(src));
        int bytes = dis.available();
        blockSize = (int) Math.ceil(bytes / 10.0);
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
        for (char c : chars) {
            freq[c]++;
        }
        return getTree(freq);
    }

    /**
     * Returns the root of the corresponding Huffman Tree to a frequency array
     *
     * @param freq the frequency array to use on the string
     * @return the root of the corresponding Huffman Tree to {@code freq}
     */
    private Node getTree(int[] freq) {
        PriorityQueue<Node> nodes = new PriorityQueue<>();
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

        static final char LINK_CHAR = Character.MIN_VALUE;

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

        Node(final char character, final int freq) {
            this(character, freq, null, null);
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
