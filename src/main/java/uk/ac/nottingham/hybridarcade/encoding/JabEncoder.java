package uk.ac.nottingham.hybridarcade.encoding;

import uk.ac.nottingham.hybridarcade.Constants;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class JabEncoder implements IEncoder{

    private static final String TEMPORARY_FILE_PATH = "jabcode.png";

    static {
        System.loadLibrary("png16");
        System.loadLibrary("tiff");
        System.loadLibrary("z");
        System.loadLibrary("JabEncoder");
    }

    private native byte[] saveEncoding(byte[] data);

    @Override
    public boolean encode(byte[] data) {
        saveEncoding(data);
        try {
            BufferedImage jabcode = ImageIO.read(new File(TEMPORARY_FILE_PATH));
            ImageIO.write(jabcode, "png", new File("test6.png"));
        }
        catch(IOException e){
            Constants.logger.error("Unable to save/read JAB code.");
        }
        return false;
    }

    @Override
    public byte[] decode(String filePath) {
        return new byte[0];
    }
}
