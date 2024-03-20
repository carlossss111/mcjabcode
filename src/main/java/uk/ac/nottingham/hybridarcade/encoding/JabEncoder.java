package uk.ac.nottingham.hybridarcade.encoding;

import javax.imageio.ImageIO;
import java.awt.*;
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
public class JabEncoder {
    private boolean mDoImageCorrection = true;

    static {
        System.loadLibrary("JabEncoder");
    }

    private native byte[] saveEncoding(byte[] data, int eccLevel);

    private native byte[] readEncoding(byte[] png);

    /**
     * Encodes an array of bytes into a JAB Encoding.
     * @param data Bytes to be encoded.
     * @return PNG image of the JAB encoding.
     * @throws IOException if the C Shared Object failed to encode the data.
     */
    public BufferedImage encode(byte[] data, int eccLevel) throws IOException {
        byte[] pngRaw = saveEncoding(data, eccLevel);
        if(pngRaw == null){
            throw new IOException("C Lib fail failed to generate JAB code");
        }
        return ImageIO.read(new ByteArrayInputStream(pngRaw));
    }

    /**
     * Rotates the image by some degrees. Used by the decompression algorithm
     * if it fails to read the barcode. Computationally expensive but only
     * used on decode errors.
     * @param png raw image
     * @param angle degrees to rotate
     * @return rotate image
     */
    private BufferedImage tryRotate(BufferedImage png, int angle){
        // Skip computation at zero angle
        if(angle == 0){
            return png;
        }

        // Calculate new width/height of image
        double sine = Math.abs(Math.sin(Math.toRadians(angle)));
        double cosine = Math.abs(Math.cos(Math.toRadians(angle)));
        int width = png.getWidth();
        int height = png.getHeight();
        int rotatedWidth = (int) Math.floor(width*cosine + height*sine);
        int rotatedHeight = (int) Math.floor(height*cosine + width*sine);

        // Center and rotate image using Graphics2D
        BufferedImage rotated = new BufferedImage(rotatedWidth, rotatedHeight, png.getType());
        Graphics2D graphic = rotated.createGraphics();
        graphic.translate((rotatedWidth-width)/2, (rotatedHeight-height)/2);
        graphic.rotate(Math.toRadians(angle), width/2.f, height/2.f);
        graphic.drawRenderedImage(png, null);
        graphic.dispose();
        return rotated;
    }

    /**
     * Sets whether to try image correction on failed decodes. Computationally
     * expensive but only runs on fails. On by default.
     * @param onOrOff true or false
     */
    public void setImageCorrection(boolean onOrOff){
        mDoImageCorrection = onOrOff;
    }

    /**
     * Decodes a JAB Encoding into an array of bytes.
     * @param image PNG image of the JAB encoding.
     * @return Bytes encoded in the image.
     * @throws IOException if the C Shared Object failed to decode the data.
     */
    public byte[] decode(BufferedImage image) throws IOException{
        int steps = 3, max = steps;
        if(mDoImageCorrection){
            max = 90;
        }

        // Try to decode, if oriented correctly it'll work first time
        for(int theta = 0; theta < max; theta += steps){
            BufferedImage rot = tryRotate(image, theta);

            ByteArrayOutputStream pngSteam = new ByteArrayOutputStream();
            ImageIO.write(rot, "png", pngSteam);
            byte[] dataStream = readEncoding(pngSteam.toByteArray());

            if(dataStream != null){
                return dataStream;
            }
        }

        throw new IOException("C Lib fail failed to read JAB code");
    }
}
