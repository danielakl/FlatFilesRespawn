package no.flatfiles;

import java.io.*;
import java.util.BitSet;
import java.util.Scanner;

/**
 * @author Joppe
 * @author Daniel Klock
 * @version 0.1.0
 */
public final class Decompress {
    public static void decompress(File source, File destination, File treeFile) throws Exception {
        int[] tree = readFreq(treeFile);
//        for(int i = 0; i < tree.length; i++){
//            System.out.println(tree[i]);
//        }
    }

    private static int[] readFreq(File tree) throws Exception{
        Scanner scanner = new Scanner(tree);
        int [] freq = new int [256];
        int i = 0;
        while(scanner.hasNextByte()){
            freq[i] = scanner.nextByte();
            System.out.println(freq[i]);
            i++;
        }
        return freq;
    }

    private static String decodeMessage(File source, File destination, Node node) throws FileNotFoundException, IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(destination))) {
            final BitSet bitSet = (BitSet) ois.readObject();
            final StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < (bitSet.length() - 1);) {
                Node temp = node;
                // since huffman code generates full binary tree, temp.right is certainly null if temp.left is null.
                while (temp.getLeftChild() != null) {
                    if (!bitSet.get(i)) {
                        temp = temp.getLeftChild();
                    } else {
                        temp = temp.getRightChild();
                    }
                    i = i + 1;
                }
                stringBuilder.append(temp.getCharacter());
            }
            return stringBuilder.toString();
        }
    }
}
