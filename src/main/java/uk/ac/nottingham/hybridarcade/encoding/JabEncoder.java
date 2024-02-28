package uk.ac.nottingham.hybridarcade.encoding;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Encodes/decodes a stream of Bytes to/from a JAB Code image.
 * Uses a C Shared Object with help from the Java Native Interface.
 * @see <a href="https://jabcode.org/">jabcode.org</a>
 * @author Daniel Robinson 2024
 */
public class JabEncoder implements IEncoder{

    static {
        System.loadLibrary("JabEncoder");
    }

    private native byte[] saveEncoding(byte[] data);

    private native byte[] readEncoding(byte[] png);

    /**
     * Encodes an array of bytes into a JAB Encoding.
     * @param data Bytes to be encoded.
     * @return PNG image of the JAB encoding.
     * @throws IOException if the C Shared Object failed to encode the data.
     */
    @Override
    public BufferedImage encode(byte[] data) throws IOException {
        byte[] pngRaw = saveEncoding(data);
        if(pngRaw == null){
            throw new IOException("C Lib fail failed to generate JAB code");
        }
        return ImageIO.read(new ByteArrayInputStream(pngRaw));
    }

    /**
     * Decodes a JAB Encoding into an array of bytes.
     * @param image PNG image of the JAB encoding.
     * @return Bytes encoded in the image.
     * @throws IOException if the C Shared Object failed to decode the data.
     */
    @Override
    public byte[] decode(BufferedImage image) throws IOException{
        ByteArrayOutputStream pngSteam = new ByteArrayOutputStream();
        ImageIO.write(image, "png", pngSteam);
        byte[] dataStream = readEncoding(pngSteam.toByteArray());
        if(dataStream == null){
            throw new IOException("C Lib fail failed to read JAB code");
        }
        return dataStream;
    }
}
