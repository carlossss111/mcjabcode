package unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.nottingham.hybridarcade.Constants;
import uk.ac.nottingham.hybridarcade.compression.BestCompressor;
import uk.ac.nottingham.hybridarcade.compression.ICompressor;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static testutil.Utility.assertByteArrayEquals;

public class TestBestCompressor {
    private static final byte RL = Constants.RESERVED_FOR_COMPRESSION_TK;

    private static final byte UNCOMPRESSED = 0;
    private static final byte RUN_LENGTH = 1;
    private static final byte RUN_LENGTH_MK2 = 2;
    private static final byte HUFFMAN = 3;

    ICompressor mCompressor;

    @BeforeEach
    public void setup(){
        mCompressor = new BestCompressor();
    }

    @Test
    public void testCompressesCorrectly(){
        byte[] useRlMk1 = "AAAAAAAAAAAAAAAABCDE".getBytes(StandardCharsets.US_ASCII);
        byte[] useRlMk2 = "AAAAAAAAAAAAAAAAAABB".getBytes(StandardCharsets.US_ASCII);
        byte[] useHuffman = "AABABABABABABABABA".getBytes(StandardCharsets.US_ASCII);

        assertEquals(RUN_LENGTH,    mCompressor.compress(useRlMk1)[0]);
        assertEquals(RUN_LENGTH_MK2,mCompressor.compress(useRlMk2)[0]);
        assertEquals(HUFFMAN,       mCompressor.compress(useHuffman)[0]);
    }

    @Test
    public void testDecompressesCorrectly(){
        byte[] useNone = new byte[] {UNCOMPRESSED, 'A', 'A'};
        byte[] useRlMk1 = new byte[] {RUN_LENGTH, RL, 2, 'A'};
        byte[] useRlMk2 = new byte[] {RUN_LENGTH_MK2, 2, 'A'};
        byte[] useHuffman = new byte[] {HUFFMAN, 10, 0, 4, 0, 'A', 2, 0, 'B', 2, 0, 12};

        assertByteArrayEquals(new byte[] {'A','A'}, mCompressor.decompress(useNone));
        assertByteArrayEquals(new byte[] {'A','A'}, mCompressor.decompress(useRlMk1));
        assertByteArrayEquals(new byte[] {'A','A'}, mCompressor.decompress(useRlMk2));
        assertByteArrayEquals(new byte[] {'A','A','B','B'}, mCompressor.decompress(useHuffman));

    }

    @Test
    public void testDecompressionThrows(){
        byte[] invalidBytes = new byte[] {10, 'A', 'A'};

        assertThrows(IllegalArgumentException.class,
                () -> mCompressor.decompress(invalidBytes));
    }
}
