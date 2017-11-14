package no.flatline;

import no.flatline.file.FileUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Daniel Klock
 * @author Rolv-Arild Braaten
 * @version 0.1.0
 */
public class HuffmanTest {

    private static final Path DIRECTORY = FileSystems.getDefault().getPath("resource/source");
    private static File FILE_1;
    private static File FILE_2;

    /**
     * Clears test file directory before initializing class.
     * @throws Exception - Throws any exception.
     */
    @BeforeClass
    public static void initialSetUp() throws Exception {
        System.out.println("Running initial setup for Huffman Tests.");
        File[] testFiles = DIRECTORY.toFile().listFiles();
        delete(testFiles);
        File[] compFiles = FileSystems.getDefault().getPath("resource\\compressed").toFile().listFiles();
        delete(compFiles);
        File[] decompFiles = FileSystems.getDefault().getPath("resource\\decompressed").toFile().listFiles();
        delete(decompFiles);
        System.out.println("\nRunning setup for a test.");
        FILE_1 = FileUtil.createFile("resource\\source", "test-1", "txt");
        FILE_2 = FileUtil.createFile("resource\\source", "test-2", "txt");
//        FILE_3 = Files.createTempFile(DIRECTORY, "test-3", ".txt").toFile();
        BufferedWriter bw1 = new BufferedWriter(new FileWriter(FILE_1));
        BufferedWriter bw2 = new BufferedWriter(new FileWriter(FILE_2));
//        BufferedWriter bw3 = new BufferedWriter(new FileWriter(FILE_3));
        bw1.write("Dette er en test for komprimeringen av filer.\nDette er ekstra tekst for å gjøre filstørrelsen stø");
        bw1.close();
        System.out.println("INFO:\tGenerated test file " + FILE_1.getName());
        bw2.write("                                                                                                                                   \u0007                                                       \u0001                                                                                       \u0001                                                                                                                   \u0001               \b   \u0002   \u0001       \u0003       \u0001   \u0001   \u0002   \u0003   \u0002   \u0001       \u0005   \u0001   \u0004       \u0001                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    ");
        bw2.close();
        System.out.println("INFO:\tGenerated test file " + FILE_2.getName());
//        bw3.close();
//        System.out.println("INFO:\tGenerated test file " + FILE_3.getName());

    }

    private static void delete(File[] testFiles) {
        if (testFiles != null) {
            int counter = testFiles.length;
            for (File file : testFiles) {
                if (file.getName().startsWith("test")) {
                    if (file.delete()) {
                        System.out.println("INFO:\tDeleting test file " + file.getName());
                    } else {
                        counter = 0;
                    }
                }
            }
            if (counter != testFiles.length) {
                System.out.println("WARN:\tNot all test files was deleted during initialization.");
            }
        }
    }


    @Test
    public void compress() throws Exception {
        Huffman h = new Huffman();
        long size = FILE_1.length();
        h.compress(FILE_1);
        File newFile = new File(FILE_1.toString().replace("resource\\source", "resource\\compressed") + ".cff");
        assertTrue(newFile.length() < size);
    }

    @Test
    public void decompress() throws Exception {
        Huffman h = new Huffman();

        h.compress(FILE_2);
        h.decompress(new File(FILE_2.toString().replace("resource\\source", "resource\\compressed") + ".cff"));

        File decomp = new File(FILE_2.toString().replace("resource\\source", "resource\\decompressed"));
        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(FILE_2)));
        DataInputStream dat = new DataInputStream(new BufferedInputStream(new FileInputStream(decomp)));

        assertEquals(dis.available(), dat.available());
        int c;
        while ((c = dis.read()) != -1) {
            assertEquals(c, dat.read());
        }
    }
}