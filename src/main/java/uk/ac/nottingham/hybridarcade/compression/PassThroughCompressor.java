package uk.ac.nottingham.hybridarcade.compression;

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
