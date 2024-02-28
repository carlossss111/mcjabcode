package uk.ac.nottingham.hybridarcade.encoding;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * The encoder interface.
 * @author Daniel Robinson 2024
 */
public interface IEncoder {

    /**
     * Encode an array of bytes into a barcode of some type.
     * @param data Bytes to be encoded.
     * @return PNG image of the barcode.
     * @throws IOException if there is a barcode can not be created.
     */
    BufferedImage encode(byte[] data) throws IOException;

    /**
     * Decode a barcode of some type into an array of bytes.
     * @param image PNG image of the barcode.
     * @return Bytes encoded in the image.
     * @throws IOException if the barcode cannot be decoded.
     */
    byte[] decode(BufferedImage image) throws IOException;
}
