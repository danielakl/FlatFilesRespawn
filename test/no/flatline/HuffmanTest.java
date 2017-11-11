package no.flatline;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Daniel Klock
 * @version 0.1.0
 */
public class HuffmanTest {
    private static final Path DIRECTORY = FileSystems.getDefault().getPath("resource", "testFiles");
    private static File FILE_1;
    private static File FILE_2;
    private static File FILE_3;

    /**
     * Clears test file directory before initializing class.
     * @throws Exception - Throws any exception.
     */
    @BeforeClass
    public static void initialSetUp() throws Exception {
        System.out.println("Running initial setup for Huffman Tests.");
        File[] testFiles = DIRECTORY.toFile().listFiles();
        if (testFiles != null) {
            int counter = 0;
            for (File file : testFiles) {
                if (file.delete()) {
                    System.out.println("INFO:\tDeleting test file " + file.getName());
                    counter++;
                }
            }
            if (counter != testFiles.length) {
                System.out.println("WARN:\tNot all test files was deleted during initialization.");
            }
        }
    }

    /**
     * Generate temporary files to use within the tests.
     * @throws Exception - Throws any exception.
     */
    @Before
    public void setUp() throws Exception {
        System.out.println("\nRunning setup for a test.");
        FILE_1 = Files.createTempFile(DIRECTORY, "test-1-", ".txt").toFile();
        FILE_2 = Files.createTempFile(DIRECTORY, "test-2-", ".txt").toFile();
        FILE_3 = Files.createTempFile(DIRECTORY, "test-3-", ".txt").toFile();
        BufferedWriter bw1 = new BufferedWriter(new FileWriter(FILE_1));
        BufferedWriter bw2 = new BufferedWriter(new FileWriter(FILE_2));
        BufferedWriter bw3 = new BufferedWriter(new FileWriter(FILE_3));
        bw1.write("Dette er en test for komprimeringen av filer.");
        bw1.close();
        System.out.println("INFO:\tGenerated test file " + FILE_1.getName());
        bw2.write("");
        bw2.close();
        System.out.println("INFO:\tGenerated test file " + FILE_2.getName());
        bw3.write("                                                                                                                                   \u0007                                                       \u0001                                                                                       \u0001                                                                                                                   \u0001               \b   \u0002   \u0001       \u0003       \u0001   \u0001   \u0002   \u0003   \u0002   \u0001       \u0005   \u0001   \u0004       \u0001                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    ");
        bw3.close();
        System.out.println("INFO:\tGenerated test file " + FILE_3.getName());
    }

    @Test
    public void compressTest() {
        Huffman h = new Huffman();
        long size = FILE_1.length();
        h.compress(FILE_1);
        assertTrue(FILE_1.length() < size);
    }

    @Test
    public void decompressTest() throws IOException {
        Huffman h = new Huffman();
        // TODO create copy of file to test for equality
        File f = null;
        h.compress(FILE_1);
        h.decompress(FILE_1);

        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(FILE_1)));
        DataInputStream dat = new DataInputStream(new BufferedInputStream(new FileInputStream(FILE_2)));

        int c;
        while ((c = dis.read()) != -1) {
            assertEquals(c, dat.read());
        }
    }
}