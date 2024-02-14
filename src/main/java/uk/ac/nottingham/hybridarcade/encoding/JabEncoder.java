package uk.ac.nottingham.hybridarcade.encoding;

import uk.ac.nottingham.hybridarcade.Constants;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class JabEncoder implements IEncoder{

    static {
        System.loadLibrary("JabEncoder");
    }

    private native byte[] saveEncoding(byte[] data);

    @Override
    public BufferedImage encode(byte[] data) throws IOException {
        byte[] pngRaw = saveEncoding(data);
        if(pngRaw == null){
            throw new IOException("C Lib fail failed to generate JAB code");
        }
        return ImageIO.read(new ByteArrayInputStream(pngRaw));
    }

    @Override
    public byte[] decode(String filePath) {
        return new byte[0];
    }
}
