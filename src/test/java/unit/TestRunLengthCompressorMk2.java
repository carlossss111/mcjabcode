package unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.nottingham.hybridarcade.compression.ICompressor;
import uk.ac.nottingham.hybridarcade.compression.RunLengthCompressorMk2;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static testutil.Utility.assertByteArrayEquals;

public class TestRunLengthCompressorMk2 {
    private final static String TEST_PATH = "unittest/rawRL.bytes";

    ICompressor mCompressor;

    @BeforeEach
    public void setup(){
        mCompressor = new RunLengthCompressorMk2();
    }

    /* Compression */

    @Test
    public void testCompressionIsCorrect(){
        byte[] testInput = "XYZAAAAAAAABBCCCCD".getBytes(StandardCharsets.US_ASCII);
        byte[] testOutput = mCompressor.compress(testInput);

        byte[] expectedOutput = {1,'X',1,'Y',1,'Z',8,'A',2,'B',4,'C',1,'D'};
        assertByteArrayEquals(expectedOutput, testOutput);
    }

    @Test
    public void testCompressionIsCorrectWithSingleStart(){
        byte[] testInput = "ABBBBBCDDDD".getBytes(StandardCharsets.US_ASCII);
        byte[] testOutput = mCompressor.compress(testInput);

        byte[] expectedOutput = {1,'A',5,'B',1,'C',4,'D'};
        assertByteArrayEquals(expectedOutput, testOutput);
    }

    @Test
    public void testCompressionSurvivesEmptyInput(){
        byte[] testInput = new byte[0];
        byte[] testOutput = mCompressor.compress(testInput);

        assertEquals(0, testOutput.length);
    }

    @Test
    public void testCompressionVeryShort(){
        byte[] testInput = {'A', 'B'};
        byte[] testOutput = mCompressor.compress(testInput);

        byte[] expectedOutput = {1, 'A', 1, 'B'};
        assertByteArrayEquals(expectedOutput, testOutput);
    }

    @Test
    public void testCompressionVeryLong(){
        byte[] testInput = new byte[512];
        //A*513, B
        for(int i = 0; i < 512; i++){
            testInput[i] = 'A';
        }
        testInput[510] = 'A';
        testInput[511] = 'B';

        byte[] testOutput = mCompressor.compress(testInput);

        byte[] expectedOutput = {(byte) 255, 'A', (byte) 255, 'A', 1, 'A', 1, 'B'};
        assertByteArrayEquals(expectedOutput, testOutput);
    }

    /* Decompression */

    @Test
    public void testDecompressionIsCorrect(){
        byte[] testInput = {8,'A',2,'B',4,'C',1,'D'};
        byte[] testOutput = mCompressor.decompress(testInput);

        byte[] expectedOutput = "AAAAAAAABBCCCCD".getBytes(StandardCharsets.US_ASCII);
        assertByteArrayEquals(expectedOutput, testOutput);
    }

    @Test
    public void testSignByteBug() throws Exception {
        File fp = new File(getClass()
                .getClassLoader().getResource(TEST_PATH).getPath());
        byte[] rawBytes = Files.readAllBytes(fp.toPath());
        byte[] compressedBytes = mCompressor.compress(rawBytes);
        byte[] decompressedBytes = mCompressor.decompress(compressedBytes);
        assertByteArrayEquals(rawBytes, decompressedBytes);
    }

}
