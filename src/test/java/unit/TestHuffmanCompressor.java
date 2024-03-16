package unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.nottingham.hybridarcade.compression.HuffmanCompressor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static testutil.Utility.assertByteArrayEquals;

public class TestHuffmanCompressor {
    private final static String TEST_PATH = "performancetest/world.bytes";
    private final static String TEST_PATH_2 = "unittest/rawHF.bytes";
    private HuffmanCompressor mCompressor;

    @BeforeEach
    public void setup(){
        mCompressor = new HuffmanCompressor();
    }

    @Test
    public void testCompressionIsCorrect(){
        byte[] testInput = "XYZAAAAAAAABBCCCCD".getBytes(StandardCharsets.US_ASCII);
        byte[] testOutput = mCompressor.compress(testInput);

        byte[] expectedOutput = new byte[] {
                25,0,42,0, //42 payload BITS in total, 25 BYTES in the header
                'A',8,0,
                'B',2,0,
                'C',4,0,
                'D',1,0,
                'X',1,0,
                'Y',1,0,
                'Z',1,0,
                -17, 47, -64, 93, -43};
        assertByteArrayEquals(expectedOutput, testOutput);
    }

    @Test
    public void testDecompressionIsCorrect(){
        byte[] testInput = new byte[] {
                25,0,42,0,
                'D',1,0,
                'Z',1,0,
                'X',1,0,
                'Y',1,0,
                'B',2,0,
                'C',4,0,
                'A',8,0,
                -17, 47, -64, 93, -43};
        byte[] testOutput = mCompressor.decompress(testInput);

        byte[] expectedOutput = "XYZAAAAAAAABBCCCCD".getBytes(StandardCharsets.US_ASCII);
        assertByteArrayEquals(expectedOutput, testOutput);
    }

    @Test
    public void testLargeFile() throws Exception {
        File fp = new File(getClass()
                .getClassLoader().getResource(TEST_PATH).getPath());
        byte[] rawBytes = Files.readAllBytes(fp.toPath());
        byte[] compressedBytes = mCompressor.compress(rawBytes);
        byte[] decompressedBytes = mCompressor.decompress(compressedBytes);
        assertByteArrayEquals(rawBytes, decompressedBytes);
    }

    @Test
    public void testEqualityOrderingBug() throws Exception {
        File fp = new File(getClass()
                .getClassLoader().getResource(TEST_PATH_2).getPath());
        byte[] rawBytes = Files.readAllBytes(fp.toPath());
        byte[] compressedBytes = mCompressor.compress(rawBytes);
        byte[] decompressedBytes = mCompressor.decompress(compressedBytes);
        assertByteArrayEquals(rawBytes, decompressedBytes);
    }

}
