package no.flatline;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.*;

public class HuffmanTest {

    @Test
    public void test() {
        try {
            File file1 = new File("testFiles/test1.txt");
            File file2 = new File("testFiles/test2.txt");
            File file3 = new File("testFiles/test3.txt");

            Huffman h = new Huffman();

            h.compress(file1);
            h.decompress(file2);

            BufferedReader br1 = new BufferedReader(new FileReader(file1));
            BufferedReader br2 = new BufferedReader(new FileReader(file2));

            assertTrue(file2.length() <= file1.length());

            assertTrue(file1.length() == file3.length());

            String s1;
            String s2;
            while ((s1 = br1.readLine()) != null) {
                s2 = br2.readLine();
                assertTrue(s1.equals(s2));
            }
        } catch (IOException e) {
            assertTrue(false);
        }
    }
}