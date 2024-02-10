package uk.ac.nottingham.hybridarcade.encoding;

import uk.ac.nottingham.hybridarcade.Constants;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public class JabEncoder implements IEncoder{

    static {
        System.loadLibrary("JabEncoder");
    }

    private native byte[] saveEncoding(byte[] data);

    @Override
    public BufferedImage encode(byte[] data) {
        BufferedImage pngImage = null;
        try {
            byte[] pngRaw = saveEncoding(data);
            pngImage = ImageIO.read(new ByteArrayInputStream(pngRaw));
        }
        catch(Exception e){
            Constants.logger.error("Failure generating and saving JABcode.");
        }
        return pngImage;
    }

    @Override
    public byte[] decode(String filePath) {
        return new byte[0];
    }
}
