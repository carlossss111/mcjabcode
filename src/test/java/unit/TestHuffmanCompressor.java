package unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.nottingham.hybridarcade.compression.HuffmanCompressor;

public class TestHuffmanCompressor {
    private HuffmanCompressor mCompressor;

    @BeforeEach
    public void setup(){
        mCompressor = new HuffmanCompressor();
    }

    @Test
    public void testShortCompression(){
        byte[] b = mCompressor.compress(new byte[] {'D','A','D','D','D','A','B','B','C','B'});
    }
}
