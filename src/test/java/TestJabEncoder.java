import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.nottingham.hybridarcade.encoding.JabEncoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class TestJabEncoder {
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
        BufferedImage actualPNG = null;
        try {
            File expectedFile = new File(getClass().getClassLoader().getResource("test/expectedjabcode.png").getPath());
            expectedPNG = ImageIO.read(expectedFile);
        }
        catch(Exception e){
            fail("Problem with the test, expectedjabcode.png not loaded.");
        }

        byte[] streamIn = new byte[3];
        streamIn[0] = 65;
        streamIn[1] = 66;
        streamIn[2] = 67;
        actualPNG = mEncoder.encode(streamIn);
        ImageIO.write(actualPNG, "png", new File("test.png"));

        assertNotNull(actualPNG);
        assertTrue(areImagesTheSame(expectedPNG, actualPNG));
    }

    @Test
    public void testEncodeCanTakeMaxValue() throws IOException{
        BufferedImage png;

        //byte[] streamIn = new byte[4138];
        byte[] streamIn = new byte[6796];
        Arrays.fill(streamIn, (byte) 'A');

        png = mEncoder.encode(streamIn);
        assertNotNull(png);
    }
}
