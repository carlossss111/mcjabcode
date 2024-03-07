package unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.nottingham.hybridarcade.Constants;
import uk.ac.nottingham.hybridarcade.compression.ICompressor;
import uk.ac.nottingham.hybridarcade.compression.RunLengthCompressor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static testutil.Utility.assertByteArrayEquals;

public class TestRunLengthCompressor {
    private final static byte RL = Constants.RESERVED_FOR_COMPRESSION_TK;
    private final static String TEST_PATH = "unittest/rawRL.bytes";

    ICompressor mCompressor;

    @BeforeEach
    public void setup(){
        mCompressor = new RunLengthCompressor();
    }

    /* Compression */

    @Test
    public void testCompressionIsCorrect(){
        byte[] testInput = "AAAAAAAABBCCCCD".getBytes(StandardCharsets.US_ASCII);
        byte[] testOutput = mCompressor.compress(testInput);

        byte[] expectedOutput = {RL, 8,'A','B','B',RL,4,'C','D'};
        assertByteArrayEquals(expectedOutput, testOutput);
    }

    @Test
    public void testCompressionIsCorrectWithSingleStart(){
        byte[] testInput = "ABBBBBCDDDD".getBytes(StandardCharsets.US_ASCII);
        byte[] testOutput = mCompressor.compress(testInput);

        byte[] expectedOutput = {'A',RL,5,'B','C',RL,4,'D'};
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

        byte[] expectedOutput = {'A', 'B'};
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

        byte[] expectedOutput = {RL, (byte) 255, 'A', RL, (byte) 255, 'A', 'A', 'B'};
        assertByteArrayEquals(expectedOutput, testOutput);
    }

    /* Decompression */

    @Test
    public void testDecompressionIsCorrect(){
        byte[] testInput = {RL, 8,'A','B','B',RL,4,'C','D'};
        byte[] testOutput = mCompressor.decompress(testInput);

        byte[] expectedOutput = "AAAAAAAABBCCCCD".getBytes(StandardCharsets.US_ASCII);
        assertByteArrayEquals(expectedOutput, testOutput);
    }

    @Test
    public void testDecompressionThrowsAtInvalid(){
        byte[] testInput = {RL, RL, RL};
        byte[] testInput2 = {'A','A', RL};
        assertThrows(IllegalArgumentException.class, () -> mCompressor.decompress(testInput));
        assertThrows(IllegalArgumentException.class, () -> mCompressor.decompress(testInput2));
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
