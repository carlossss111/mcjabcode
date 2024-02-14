package uk.ac.nottingham.hybridarcade.encoding;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface IEncoder {

    // Encode bytestream to image, returns image if success
    BufferedImage encode(byte[] data) throws IOException;

    // Decode image into bytestream
    byte[] decode(BufferedImage image) throws IOException;
}
