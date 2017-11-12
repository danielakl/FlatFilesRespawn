package no.flatline;

import no.flatline.file.FileUtil;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * @author Rolv-Arild Braaten
 * @author Daniel Klock
 * @author Joakim Sæther
 * @author Kristoffer Arntzen
 * @version 0.0.2
 */
public class Huffman implements Compressor {

    private static final int BLOCK_SIZE = 1 << 20;

    /**
     * Default constructor.
     */
    public Huffman() {}

    @Override
    public void compress(File src) {
        if (src.isFile() && src.canRead()) {
            try {
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(src)));
                File compFile = FileUtil.createFile(src.getParent(), src.getName(), "cff");
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(compFile)));

                /* First write frequency array */
                long[] freq = new long[Character.MAX_VALUE+1];
                int i1;
                while ((i1 = dis.read()) != -1) {
                    int i2 = dis.read();
                    if (i2 != -1) {
                        freq[((i1 & 0xff) << 8) | (i2 & 0xff)]++;
                    }
                }
                long maxFreq = 0;
                for (long aFreq : freq) {
                    if (aFreq > maxFreq) {
                        maxFreq = aFreq;
                    }
                }
                if (maxFreq == 0) throw new IllegalArgumentException("Wut");
                byte first = (byte) (1 + Long.toBinaryString(maxFreq).length() / 8);
                dos.writeByte(first);
                for (int i = 0; i < freq.length; i++) {
                    write(dos, first, freq, i);
                }
                dos.writeChar(0);

            /* Now compress file using the frequencies */
                Map<Character, String> table = buildTable(getTree(freq));

                dis = new DataInputStream(new BufferedInputStream(new FileInputStream(src)));
                StringBuilder s = new StringBuilder();

                byte[] bytes = new byte[BLOCK_SIZE];
                int len = dis.available();
                while (BLOCK_SIZE < len) {
                    dis.readFully(bytes, 0, BLOCK_SIZE);

                    // TODO write bytes

                    len = dis.available();
                }
                // TODO write remaining bytes
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void write(DataOutputStream dos, byte first, long[] freq, int i) throws IOException {
        if (freq[i] <= 0) return;
        switch (first) {
            case 1:
                dos.writeByte((byte) freq[i]);
                break;
            case 2:
                dos.writeShort((short) freq[i]);
                break;
            case 3:
            case 4:
                dos.writeInt((int) freq[i]);
                break;
            default:
                dos.writeLong(freq[i]);
                break;
        }
        if (i != 0) dos.writeChar((char) i);
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
        if (src.isFile() && src.canRead()) {
            String extension = FileUtil.getExtension(src);
            if (extension.equals("cff")) {
//                try {
//                    DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(src)));
//                    File dcompFile = FileUtil.createFile(src.getParent(), FileUtil.getBaseName(src));
//                    DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dcompFile)));
//                    blockSize = dis.readInt();
//                    byte[] bytes = new byte[blockSize];
//                    dis.readFully(bytes, 0, blockSize);
//                    dis.close();
//                    dos.close();
//                } catch (IOException ioe) {
//                    ioe.printStackTrace();
//                }
            } else {
                throw new IllegalArgumentException("File is not valid format");
            }
        }
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
     * Returns the root of the corresponding Huffman Tree to a string
     *
     * @param s the string to get the Huffman Tree for.
     * @return the root of the corresponding Huffman Tree to {@code s}
     */
    private Node getTree(String s) {
        char[] chars = s.toCharArray();
        long[] freq = new long[Character.MAX_VALUE+1]; // one entry for each possible character
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
    private Node getTree(long[] freq) {
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
            long combinedFreq = left.freq + right.freq;
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
        final long freq;
        final Node leftChild;
        final Node rightChild;

        Node(final char character, final long freq, final Node leftChild, final Node rightChild){
            this.character = character;
            this.freq = freq;
            this.leftChild = leftChild;
            this.rightChild = rightChild;
        }

        Node(final char character, final long freq) {
            this(character, freq, null, null);
        }

        Node(final long freq, final Node leftChild, final Node rightChild) {
            this(LINK_CHAR, freq, leftChild, rightChild);
        }

        boolean isLeaf(){
            return this.leftChild == null && this.rightChild == null;
        }

        @Override
        public int compareTo(final Node o){
            final int freqCompare = Long.compare(this.freq, o.freq);
            if (freqCompare != 0) return freqCompare;
            return Integer.compare(this.character, o.character);
        }
    }
}
