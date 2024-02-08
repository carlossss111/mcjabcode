package uk.ac.nottingham.hybridarcade.encoding;

public interface IEncoder {

    // Encode bytestream to image, return true if success
    boolean encode(byte[] data, byte[] filePath);

    // Decode image into bytestream
    byte[] decode(String filePath);
}
