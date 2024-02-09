package uk.ac.nottingham.hybridarcade.encoding;

public interface IEncoder {

    // Encode bytestream to image, return true if success
    boolean encode(byte[] data);

    // Decode image into bytestream
    byte[] decode(String filePath);
}
