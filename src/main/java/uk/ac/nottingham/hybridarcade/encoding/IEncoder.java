package uk.ac.nottingham.hybridarcade.encoding;

import java.awt.image.BufferedImage;

public interface IEncoder {

    // Encode bytestream to image, returns image if success
    BufferedImage encode(byte[] data);

    // Decode image into bytestream
    byte[] decode(String filePath);
}
