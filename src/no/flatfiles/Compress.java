package no.flatfiles;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * @author Joppe
 * @author Daniel Klock
 * @version 0.1.0
 */
public final class Compress {
    public static void compress(File source, File destination, File tree) throws Exception {
        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(source)));
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(destination)));

        int length = dis.available();
        byte[] data = new byte[length];
        dis.readFully(data);
        String s = new String(data, "UTF-8");

        final int[] freq = frequensies(s);
        final Node root = buildTree(freq);
        final Map<Character, String> table = buildTable(root);
        Message m = new Message(generateEncodedData(s, table), root);

        String encoded = m.encoded;
        byte[] bval = new BigInteger(encoded, 2).toByteArray();
        dos.write(bval);
        dis.close();
        dos.close();
        writeTree(freq, tree);
    }

    public static void writeTree(int[] freq, File tree) throws Exception {
        DataOutputStream utfil = new DataOutputStream(new FileOutputStream(tree));
        int posisjon = 0;
        int mengde = 1024;
        ByteBuffer byteBuffer = ByteBuffer.allocate(freq.length * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(freq);

        byte[] array = byteBuffer.array();

        utfil.write(array, posisjon, mengde);
        utfil.close();
    }

    public static Map<Character, String> buildTable(final Node root) {
        final Map<Character, String> table = new HashMap<>();
        buildTableImpl(root, "", table);
        return table;
    }

    public static void buildTableImpl(final Node node, final String s, final Map<Character, String> table) {
        if (!node.isLeaf()) {
            buildTableImpl(node.getLeftChild(), s + '0', table);
            buildTableImpl(node.getRightChild(), s + '1', table);
        } else {
            table.put(node.getCharacter(), s);
        }
    }

    public static Node buildTree(int[] freq) {
        final PriorityQueue<Node> queue = new PriorityQueue<>();
        for (char i = 0; i < 65535; i++) {
            if (freq[i] > 0) {
                queue.add(new Node(i, freq[i], null, null));
            }
        }

        while (queue.size() > 1) {
            final Node left = queue.poll();
            final Node right = queue.poll();
            final Node parent = new Node('\0', left.getFreq() + right.getFreq(), left, right);
            queue.add(parent);
        }

        return queue.poll();
    }

    public static int[] frequensies(String data) {
        int[] freqTab = new int[65536];
        for(char character : data.toCharArray()) {
            if (character != 0) {
                freqTab[character]++;
            }
        }
        return freqTab;
    }

    public static String generateEncodedData(final String data, final Map<Character, String> table) {
        final StringBuilder builder = new StringBuilder();
        for (final char character : data.toCharArray()) {
            if(table.get(character) != null) {
                builder.append(table.get(character));
            }
        }
        return builder.toString();
    }

    static class Message {

        final Node root;
        final String encoded;

        Message(final String encoded, final Node root){
            this.encoded = encoded;
            this.root = root;
        }
    }
}
