import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.nottingham.hybridarcade.compression.RunLengthCompressor;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class TestRunLengthCompressor {
    private final static byte RL = (byte) 255;

    RunLengthCompressor mCompressor;

    private static void assertByteArrayEquals(byte[] expected, byte[] actual){
        if(expected.length != actual.length){
            fail("Expected size != actual size");
            return;
        }

        for(int i = 0; i < actual.length; i++){
            assertEquals(expected[i], actual[i]);
        }
    }

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
        byte[] testInput = new byte[514];
        //A*513, B
        for(int i = 0; i < 512; i++){
            testInput[i] = 'A';
        }
        testInput[512] = 'A';
        testInput[513] = 'B';

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
        byte[] testInput = {RL, RL, 'A'};
        byte[] testInput2 = {'A','A', RL};
        assertThrows(IllegalArgumentException.class, () -> mCompressor.decompress(testInput));
        assertThrows(IllegalArgumentException.class, () -> mCompressor.decompress(testInput2));
    }
}
