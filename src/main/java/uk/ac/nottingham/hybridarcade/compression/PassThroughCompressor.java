package uk.ac.nottingham.hybridarcade.compression;

/**
 * An empty that does no compression. A concrete implementation of the interface
 * is still provided in-case any side-effects should be added in the future.
 */
public class PassThroughCompressor implements ICompressor{

    @Override
    public byte[] compress(byte[] rawBytes) {
        return rawBytes;
    }

    @Override
    public byte[] decompress(byte[] compressedBytes) throws IllegalArgumentException {
        return compressedBytes;
    }
}
