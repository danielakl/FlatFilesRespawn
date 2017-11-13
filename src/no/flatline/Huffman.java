package no.flatline;

import no.flatline.file.FileUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import static java.lang.Math.min;

/**
 * @author Rolv-Arild Braaten
 * @author Daniel Klock
 * @author Joakim Sæther
 * @author Kristoffer Arntzen
 * @version 0.0.2
 */
public class Huffman implements Compressor {

    private static final int BLOCK_SIZE = 1 << 18;

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
                long[] freq = new long[256];
                int i1;
                while ((i1 = dis.read()) != -1) {
                    freq[i1]++;
                }
                long maxFreq = 0;
                for (long aFreq : freq) {
                    if (aFreq > maxFreq) {
                        maxFreq = aFreq;
                    }
                }
                byte first = (byte) (1 + Long.toBinaryString(maxFreq).length() / 8);
                dos.writeByte(first);
                for (int i = 0; i < freq.length; i++) {
                    write(dos, first, freq, i);
                }
                dos.writeByte(0);

                /* Now compress file using the frequencies */
                Node root = getTree(freq);
                Map<Byte, String> table = buildTable(root);

                dis.close();
                dis = new DataInputStream(new BufferedInputStream(new FileInputStream(src)));

                byte[] bytes;
                int len;
                while ((len = min(dis.available(), BLOCK_SIZE)) > 0) {
                    bytes = new byte[len];
                    dis.readFully(bytes, 0, len);

                    String s = new String(bytes);
                    StringBuilder build = new StringBuilder();
                    for (int i = 0; i < s.length(); i++) {
                        String bits = table.get((byte) (s.charAt(i) & 0xff));
                        if (bits != null) build.append(bits);
                    }

                    dos.write(fromBitString(build.toString()));
                }
                dis.close();
                dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void write(DataOutputStream dos, byte first, long[] freq, int i) throws IOException {
        if (freq[i] <= 0 && i != 0) return;
        if (i != 0) dos.writeByte(i);
        switch (first) {
            case 1:
                dos.writeByte((int) (freq[i] & 0xff));
                break;
            case 2:
                dos.writeShort((int) (freq[i] & 0xffff));
                break;
            case 3:
            case 4:
                dos.writeInt((int) freq[i]);
                break;
            default:
                dos.writeLong(freq[i]);
                break;
        }
    }

    private byte[] fromBitString(String bitstring) {
        ArrayList<Integer> list = new ArrayList<>();

        for(String str : bitstring.split("(?<=\\G.{8})"))
            list.add(Integer.parseInt(str, 2));

        byte[] bytes = new byte[list.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (list.get(i) & 0xff);
        }
        return bytes;
    }

    @Override
    public void decompress(File src) {
        if (src.isFile() && src.canRead()) {
            String extension = FileUtil.getExtension(src);
            if (extension.equals("cff")) {
                try {
                    DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(src)));
                    File dcompFile = FileUtil.createFile(src.getParent(), FileUtil.getBaseName(src).replace(".txt", "v2.txt"));
                    DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dcompFile)));

                    byte first = dis.readByte();
                    long[] freq = new long[256];

                    int b = 0;
                    do {
                        freq[b] = read(dis, first);
                        b = dis.readByte() & 0xff;
                    } while (b != 0);

                    Node root = getTree(freq);

                    byte[] bytes;

                    int len;
                    while ((len = min(dis.available(), BLOCK_SIZE)) > 0) {
                        bytes = new byte[len];
                        dis.readFully(bytes, 0, len);

                        StringBuilder bits = new StringBuilder();
                        for (byte b1 : bytes) {
                            String s = Integer.toBinaryString(b1 & 0xff);
                            if (s.length() < 8) {
                                int padL = 8 - s.length();
                                StringBuilder pad = new StringBuilder();
                                for (int i = 0; i < padL; i++) {
                                    pad.append("0");
                                }
                                s = pad + s;
                            }
                            bits.append(s);
                        }
                        write(dos, root, bits.toString());
                    }

                    dis.close();
                    dos.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            } else {
                throw new IllegalArgumentException("File is not valid format");
            }
        }
    }

    private long read(DataInputStream dis, byte first) throws IOException {
        switch (first) {
            case 1:
                return dis.readByte() & 0xff;
            case 2:
                return dis.readShort() & 0xffff;
            case 3:
            case 4:
                return dis.readInt();
            default:
                return dis.readLong();
        }
    }

    /**
     * Creates a map where the characters can be mapped to their corresponding compressed bit string.
     * @param root is the root node of the Huffman Tree.
     * @return a map with the characters used in the text mapped to their bit string.
     */
    private Map<Byte, String> buildTable(final Node root){
        final Map<Byte, String> table = new HashMap<>();
        buildTableImpl(root, "", table);
        return table;
    }

    /**
     * Maps the characters to their corresponding compressed bit string.
     * @param node is the root node of the Huffman Tree.
     * @param s is the compressed string at the current location in the Huffman Tree.
     * @param table is a table with all the used characters.
     */
    private void buildTableImpl(final Node node, final String s, final Map<Byte, String> table) {
        if (!node.isLeaf()) {
            buildTableImpl(node.leftChild, s + '0', table);
            buildTableImpl(node.rightChild, s + '1', table);
        } else {
            table.put((byte) (node.character & 0xff), s);
        }
    }

    /**
     * Generates a bit string from a map and a string
     *
     * @param table the character to bit string map to use.
     * @param s the string to convert into a bit string.
     * @return a bit string generated from {@code table} and {@code s}
     */
    private String getBitString(Map<Byte, String> table, String s) {
        StringBuilder bits = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            bits.append(table.get((byte)s.charAt(i)));
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
        long[] freq = new long[Character.MAX_VALUE+1]; // one entry for each possible character
        for (int i = 0; i < s.length(); i++) {
            freq[s.charAt(i)]++;
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
            if (freq[i] > 0) {
                nodes.add(new Node(i, freq[i]));
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
     */
    private void write(DataOutputStream dos, Node root, String bits) throws IOException {
        Node n = root;
        for (int i = 0; i < bits.length(); i++) {
            switch (bits.charAt(i)) {
                case '0':
                    n = n.leftChild;
                    break;
                case '1':
                    n = n.rightChild;
                    break;
                default:
                    throw new IllegalArgumentException("Bit string contains illegal characters");
            }
            if (n.isLeaf()) {
                dos.writeByte(n.character);
                n = root;
            }
        }
        if (n != root) System.out.println("Unfinished bits");
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

        static final byte LINK_CHAR = 0;

        final int character;
        final long freq;
        final Node leftChild;
        final Node rightChild;

        Node(final int character, final long freq, final Node leftChild, final Node rightChild){
            this.character = character;
            this.freq = freq;
            this.leftChild = leftChild;
            this.rightChild = rightChild;
        }

        Node(final int character, final long freq) {
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
