package uk.ac.nottingham.hybridarcade.compression;

import uk.ac.nottingham.hybridarcade.Constants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Tries each compression type (none, run-length, huffman) and picks
 * which stores the data in the least number of bytes.
 * @see PassThroughCompressor
 * @see RunLengthCompressor
 * @see RunLengthCompressorMk2
 * @see HuffmanCompressor
 */
public class BestCompressor implements ICompressor{
    private static final byte UNCOMPRESSED = 0;
    private static final byte RUN_LENGTH = 1;
    private static final byte RUN_LENGTH_MK2 = 2;
    private static final byte HUFFMAN = 3;

    /**
     * Chooses the best compression algorithm and returns the compressed bytes.
     * Prepends a byte to the data that indicates which algorithm was chosen.
     * @param rawBytes Bytes to compress.
     * @return compressed bytes
     */
    @Override
    public byte[] compress(byte[] rawBytes) {
        byte[] pt = new PassThroughCompressor().compress(rawBytes);
        byte[] rl = new RunLengthCompressor().compress(rawBytes);
        byte[] rl2 = new RunLengthCompressorMk2().compress(rawBytes);
        byte[] hf = new HuffmanCompressor().compress(rawBytes);

        if(rl.length < rl2.length && rl.length < hf.length){
            Constants.logger.info("Run Length Mk1 Selected");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.write(RUN_LENGTH);
            stream.writeBytes(rl);
            return stream.toByteArray();
        }
        else if(rl2.length < rl.length && rl2.length < hf.length){
            Constants.logger.info("Run Length Mk2 Selected");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.write(RUN_LENGTH_MK2);
            stream.writeBytes(rl2);
            return stream.toByteArray();
        }
        else if(hf.length < rl.length && hf.length < rl2.length){
            Constants.logger.info("Huffman Selected");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.write(HUFFMAN);
            stream.writeBytes(hf);
            return stream.toByteArray();
        }
        else{
            Constants.logger.info("No Compression Selected");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.write(UNCOMPRESSED);
            stream.writeBytes(pt);
            return stream.toByteArray();
        }
    }

    /**
     * Reads the first byte of the data to determine which algorithm compressed
     * the data, then runs that compression.
     * @param compressedBytes Compressed Bytes to decompress.
     * @return the uncompressed data
     * @throws IllegalArgumentException if the data is unreadable
     */
    @Override
    public byte[] decompress(byte[] compressedBytes) throws IllegalArgumentException {
        ByteArrayInputStream stream = new ByteArrayInputStream(compressedBytes);
        byte type = (byte) stream.read();
        byte[] payload = stream.readAllBytes();
        switch(type){
            case UNCOMPRESSED:
                return new PassThroughCompressor().decompress(payload);
            case RUN_LENGTH:
                return new RunLengthCompressor().decompress(payload);
            case RUN_LENGTH_MK2:
                return new RunLengthCompressorMk2().decompress(payload);
            case HUFFMAN:
                return new HuffmanCompressor().decompress(payload);
            default:
                throw new IllegalArgumentException("Compression-type byte unreadable!");
        }
    }
}
