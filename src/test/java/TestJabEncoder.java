import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.nottingham.hybridarcade.encoding.JabEncoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class TestJabEncoder {
    private static final int ENCODE_MAX = 6797;
    private static final String JABCODE_TEST_PNG = "test/jabcode.png";

    JabEncoder mEncoder;

    @BeforeEach
    public void setup() {
        mEncoder = new JabEncoder();
    }

    public boolean areImagesTheSame(BufferedImage imageA, BufferedImage imageB){
        int width  = imageA.getWidth();
        int height = imageA.getHeight();

        if(width != imageB.getWidth() || height != imageB.getHeight()){
            return false;
        }

        // Compare every pixel in the image
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                if(imageA.getRGB(x,y) != imageB.getRGB(x,y)){
                    return false;
                }
            }
        }
        return true;
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
        actualPNG = mEncoder.encode(streamIn);

        assertNotNull(actualPNG);
        assertTrue(areImagesTheSame(expectedPNG, actualPNG));
    }

    @Test
    public void testEncodeCanTakeMaxValue() throws IOException{
        byte[] streamIn = new byte[ENCODE_MAX];
        Arrays.fill(streamIn, (byte) 'A');

        BufferedImage png = mEncoder.encode(streamIn);
        assertNotNull(png);
    }

    @Test
    public void testEncodeThrowsOnFail(){
        byte[] streamIn = new byte[ENCODE_MAX + 1000];
        Arrays.fill(streamIn, (byte) 'A');

        assertThrows(IOException.class, () -> {
            mEncoder.encode(streamIn);
        });
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
}
