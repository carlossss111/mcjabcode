package unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testutil.Utility;
import uk.ac.nottingham.hybridarcade.encoding.JabEncoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class TestJabEncoder {
    private static final int ENCODE_MAX = 6797;
    private static final String JABCODE_TEST_PNG = "unittest/jabcode.png";
    private static final String JABCODE_INVALID_PNG = "unittest/unreadable.png";

    JabEncoder mEncoder;

    @BeforeEach
    public void setup() {
        mEncoder = new JabEncoder();
    }

    @Test
    public void testEncodeIsCorrect() throws IOException{
        BufferedImage expectedPNG = null;
        BufferedImage actualPNG;
        try {
            File expectedFile = new File(getClass()
                    .getClassLoader().getResource(JABCODE_TEST_PNG).getPath());
            expectedPNG = ImageIO.read(expectedFile);
        }
        catch(Exception e){
            fail("Problem with the test, jabcode.png not loaded.");
        }

        byte[] streamIn = new byte[3];
        streamIn[0] = 65;//A
        streamIn[1] = 66;//B
        streamIn[2] = 67;//C
        actualPNG = mEncoder.encode(streamIn, 0);

        assertNotNull(actualPNG);
        assertTrue(Utility.areImagesTheSame(expectedPNG, actualPNG));
    }

    @Test
    public void testEncodeCanTakeMaxValue() throws IOException{
        byte[] streamIn = new byte[ENCODE_MAX];
        Arrays.fill(streamIn, (byte) 'A');

        BufferedImage png = mEncoder.encode(streamIn, 0);
        assertNotNull(png);
    }

    @Test
    public void testEncodeThrowsOnFail(){
        byte[] streamIn = new byte[ENCODE_MAX + 1];
        Arrays.fill(streamIn, (byte) 'A');

        assertThrows(IOException.class, () -> mEncoder.encode(streamIn, 0));
    }

    @Test
    public void testDecodeIsCorrect() throws IOException{
        BufferedImage inputPNG = null;
        try {
            File inputFile = new File(getClass()
                    .getClassLoader().getResource(JABCODE_TEST_PNG).getPath());
            inputPNG = ImageIO.read(inputFile);
        }
        catch(Exception e){
            fail("Problem with the test, jabcode.png not loaded.");
        }

        byte[] actualOutput = mEncoder.decode(inputPNG);
        assertEquals(65, actualOutput[0]);//A
        assertEquals(66, actualOutput[1]);//B
        assertEquals(67, actualOutput[2]);//C
    }

    @Test
    public void testDecodeThrowsOnFail(){
        BufferedImage inputPNG = null;
        try {
            File inputFile = new File(getClass()
                    .getClassLoader().getResource(JABCODE_INVALID_PNG).getPath());
            inputPNG = ImageIO.read(inputFile);
        }
        catch(Exception e){
            fail("Problem with the test, jabcode.png not loaded.");
        }

        BufferedImage finalInputPNG = inputPNG;
        assertThrows(IOException.class, () -> mEncoder.decode(finalInputPNG));
    }

    @Test
    public void testECC() throws IOException{
        byte[] streamIn = new byte[1024];
        Arrays.fill(streamIn, (byte) 'A');

        int eccLow = 1;
        int eccHigh = 10;

        BufferedImage pngLow = mEncoder.encode(streamIn, eccLow);
        BufferedImage pngHigh = mEncoder.encode(streamIn, eccHigh);
        assertTrue(pngLow.getWidth() < pngHigh.getWidth());
    }

    @Test
    public void testInvalidECCThrows(){
        byte[] streamIn = new byte[1024];
        Arrays.fill(streamIn, (byte) 'A');

        int eccTooLow = -1;
        int eccTooHigh = 11;

        assertThrows(IOException.class, () -> mEncoder.encode(streamIn, eccTooLow));
        assertThrows(IOException.class, () -> mEncoder.encode(streamIn, eccTooHigh));
    }
}
