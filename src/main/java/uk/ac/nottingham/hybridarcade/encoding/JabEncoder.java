package uk.ac.nottingham.hybridarcade.encoding;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class JabEncoder implements IEncoder{

    static {
        System.loadLibrary("JabEncoder");
    }

    private native byte[] saveEncoding(byte[] data);

    private native byte[] readEncoding(byte[] png);

    @Override
    public BufferedImage encode(byte[] data) throws IOException {
        byte[] pngRaw = saveEncoding(data);
        if(pngRaw == null){
            throw new IOException("C Lib fail failed to generate JAB code");
        }
        return ImageIO.read(new ByteArrayInputStream(pngRaw));
    }

    @Override
    public byte[] decode(BufferedImage image) throws IOException{
//        ByteArrayOutputStream pngStream = new ByteArrayOutputStream();
//        ImageIO.write(image, "png", pngStream);
//        byte[] dataStream = readEncoding(pngStream.toByteArray());
        File fp = new File("/home/daniel/Repos/dissertation/hybrid_arcade/src/test/resources/test/jabcode.png");
        byte[] fpb = Files.readAllBytes(fp.toPath());
        byte[] dataStream = readEncoding(fpb);
        if(dataStream == null){
            throw new IOException("C Lib fail failed to read JAB code");
        }
        return dataStream;
    }
}
