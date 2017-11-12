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
            int[] freq = new int[Character.MAX_VALUE+1];
            byte[] b = new byte[blockSize];
            int len = blockSize;
            dis.readFully(b, 0, len);
            dos.writeInt(blockSize);
            dos.write(b);
            String s = new String(b);
            for (int i = 0; i < s.length(); i++) {
                freq[s.charAt(i)]++;
            }
            boolean stop = false;
            while(!stop) {
                Node root = getTree(freq);
                StringBuilder sb = new StringBuilder();
                Map<Character, String> table = buildTable(root);
                for (char character : s.toCharArray()) {
                    String bits = table.get(character);
                    if (bits == null) {
                        bits = Integer.toBinaryString(character);
                        if (bits.length() < 8) {
                            StringBuilder pad = new StringBuilder();
                            for (int i = 0; i < 8 - bits.length(); i++) {
                                pad.append(0);
                            }
                            bits = pad + bits;
                        }
                    }
                    sb.append(bits);
                }
                dos.writeBytes(fromBitString(sb.toString()));
                if (len >= dis.available()) {
                    len = dis.available();
                    stop = true;
                }
                dis.readFully(b, 0, len);
                s = new String(b);
                for (int i = 0; i < s.length(); i++) {
                    freq[s.charAt(i)]++;
                }
            }
            dis.close();
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String fromBitString(String bitstring) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < bitstring.length()-16; i += 16) {
            String sub1 = bitstring.substring(i, i+8);
            String sub2 = bitstring.substring(i+8, i+16);
            int b1 = Integer.parseInt(sub1, 2);
            int b2 = Integer.parseInt(sub2, 2);
            s.append((char)(((b1 & 0xff) << 8) | (b2 & 0xff)));
        }
        return s.toString();
    }

    @Override
    public void decompress(File src) { // TODO

    }

    /**
     * Creates a map where the characters can be mapped to their corresponding compressed bit string.
     * @param root is the root node of the Huffman Tree.
     * @return a map with the characters used in the text mapped to their bit string.
     */
    private Map<Character, String> buildTable(final Node root){
        final Map<Character, String> table = new HashMap<>();
        buildTableImpl(root, "", table);
        return table;
    }

    /**
     * Maps the characters to their corresponding compressed bit string.
     * @param node is the root node of the Huffman Tree.
     * @param s is the compressed string at the current location in the Huffman Tree.
     * @param table is a table with all the used characters.
     */
    private void buildTableImpl(final Node node, final String s, final Map<Character, String> table){
        if (!node.isLeaf()) {
            buildTableImpl(node.leftChild, s + '0', table);
            buildTableImpl(node.rightChild, s + '1', table);
        } else {
            table.put(node.character, s);
        }
    }

    /**
     * Generates a bit string from a map and a string
     *
     * @param table the character to bit string map to use.
     * @param s the string to convert into a bit string.
     * @return a bit string generated from {@code table} and {@code s}
     */
    private String getBitString(Map<Character, String> table, String s) {
        char[] chars = s.toCharArray();
        StringBuilder bits = new StringBuilder();
        for (char c : chars) {
            bits.append(table.get(c));
        }
        return bits.toString();
    }

    /**
     * Uses an estimate of bytes available to be read from the file. This estimate
     * is used to calculate a fitting block size to use with the algorithm.
     * @param src - The file to calculate the block size from.
     * @throws IOException - Throws any IOExceptions.
     */
    private void calcBlockSize(File src) throws IOException {
        blockSize = 256;
//        DataInputStream dis = new DataInputStream(new FileInputStream(src));
//        int bytes = dis.available();
//        blockSize = (int) Math.ceil(bytes / 20.0);
    }

    /**
     * Returns the root of the corresponding Huffman Tree to a string
     *
     * @param s the string to get the Huffman Tree for.
     * @return the root of the corresponding Huffman Tree to {@code s}
     */
    private Node getTree(String s) {
        char[] chars = s.toCharArray();
        int[] freq = new int[Character.MAX_VALUE+1]; // one entry for each possible character
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
        for (int i = 0; i < freq.length; i++) {
            char c = (char) i;
            if (freq[i] > 0) {
                nodes.add(new Node(c, freq[i]));
            }
        }
        while (nodes.size() > 1) {
            Node left = nodes.poll();
            Node right = nodes.poll();
            int combinedFreq = left.freq + right.freq;
//            if (right != null) combinedFreq += right.freq;
            Node link = new Node(combinedFreq, left, right);
            nodes.add(link);
        }
        return nodes.peek();
    }

    /**
     * Generates a string from a bit string and a Huffman Tree.
     *
     * @param root the root of the Huffman Tree.
     * @param bits the string of bits.
     * @return the string generated from {@code root} and {@code bits}
     */
    private String getString(Node root, String bits) {
        StringBuilder s = new StringBuilder();
        char[] bitChars = bits.toCharArray();
        Node n = root;
        for (char c : bitChars) {
            switch (c) {
                case '0':
                    n = n.leftChild;
                    break;
                case '1':
                    n = n.rightChild;
                    break;
                default:
                    throw new IllegalArgumentException("Bit string contains illegal characters");
            }
            if (!n.isLeaf()) {
                s.append(n.character);
                n = root;
            }
        }
        if (n != root) System.out.println("Unfinished bits");
        return s.toString();
    }

    /**
     * The Node class is used to create a Huffman Tree.
     * Each node stores a character and its frequency.
     *
     * @author Joakim Sæther
     * @author Rolv-Arild Braaten
     * @since 0.0.1
     */
    private static class Node implements Comparable<Node> {

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
