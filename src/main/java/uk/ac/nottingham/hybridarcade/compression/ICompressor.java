package uk.ac.nottingham.hybridarcade.compression;

public interface ICompressor {

    // Compress a stream of bytes into a smaller stream of bytes
    byte[] compress(byte[] rawBytes);

    // Decompress a stream of bytes into a full stream
    byte[] decompress(byte[] compressedBytes) throws IllegalArgumentException;
}
