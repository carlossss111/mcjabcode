import org.junit.jupiter.api.Test;
import uk.ac.nottingham.hybridarcade.encoding.JabEncoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TestJabEncoder {

    @Test
    public void testTemp() throws IOException {
        byte[] bytesTest = new byte[4];
        bytesTest[0] = 65;
        bytesTest[1] = 66;
        bytesTest[2] = 67;
        bytesTest[3] = 0;

        BufferedImage img = new JabEncoder().encode(bytesTest);
        ImageIO.write(img, "png", new File("test7.png"));

    }
}
