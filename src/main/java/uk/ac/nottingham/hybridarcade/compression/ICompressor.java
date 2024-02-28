package uk.ac.nottingham.hybridarcade.compression;

/**
 * The lossless compression interface.
 * @author Daniel Robinson 2024
 */
public interface ICompressor {

    /**
     * Compresses a byte array into a (hopefully) shorter byte array. Must be lossless.
     * @param rawBytes Bytes to compress.
     * @return Compressed Bytes.
     */
    byte[] compress(byte[] rawBytes);

    /**
     * Decompresses a compressed byte array into the original byte array.
     * @param compressedBytes Compressed Bytes to decompress.
     * @return Original Bytes.
     * @throws IllegalArgumentException if the compressed byte array is invalid or corrupted.
     */
    byte[] decompress(byte[] compressedBytes) throws IllegalArgumentException;
}
