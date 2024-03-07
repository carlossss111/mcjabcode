package uk.ac.nottingham.hybridarcade.compression;

import java.io.ByteArrayOutputStream;

/**
 * Compresses/decompresses a byte array to/from a short byte array using simple
 * Run Length Compression.
 * Marginal performance gains/losses compared to MK1 depending on the input.
 * @author Daniel Robinson 2024
 */
public class RunLengthCompressorMk2 implements ICompressor{

    /**
     * Compresses a chunk of bytes that are the same value.
     * <br/>E.g. <code>'[4][A] == AAAA'</code><br/>
     * A sequence of more than 256 bytes
     * is compressed into multiple parts.
     * @param byteChunk Array of bytes where all bytes are the same value.
     * @param size Size of byteChunk.
     * @return Array of compressed bytes.
     */
    private byte[] convertToRunlength(byte[] byteChunk, int size){
        ByteArrayOutputStream rlChunk = new ByteArrayOutputStream();
        do{
            rlChunk.write(Math.min(size, 255));
            rlChunk.write(byteChunk[0]);
            size -= 255;
        }while(size > 0);

        return rlChunk.toByteArray();
    }

    /**
     * Compresses an array of bytes into a shorter array of bytes with run-length
     * compression.
     * @param rawBytes Bytes to compress.
     * @return Compressed Byte Array.
     * @see #convertToRunlength(byte[], int)
     */
    @Override
    public byte[] compress(byte[] rawBytes) {
        if(rawBytes.length == 0){
            return rawBytes;
        }

        ByteArrayOutputStream compressedBytes = new ByteArrayOutputStream();
        ByteArrayOutputStream byteChunk = new ByteArrayOutputStream();
        for(int i = 0; i < rawBytes.length; i++){
            /*
             * If byte i is the same as last, write it into the chunk
             * if it is different or we have reached the end, copy the chunk and write into a fresh one
             */
            if(i >= 1 && rawBytes[i] != rawBytes[i-1]) {
                compressedBytes.writeBytes(
                        convertToRunlength(byteChunk.toByteArray(), byteChunk.size())
                );
                byteChunk.reset();
            }
            byteChunk.write(rawBytes[i]);
        }
        // On last time when exiting loop
        compressedBytes.writeBytes(
                convertToRunlength(byteChunk.toByteArray(), byteChunk.size())
        );
        return  compressedBytes.toByteArray();
    }

    /**
     * Decompresses a byte array of run-length bytes into the original byte array.
     * @param compressedBytes Compressed Bytes to decompress.
     * @return Original array of bytes
     * @throws IllegalArgumentException if compressedBytes is not a valid compression.
     */
    @Override
    public byte[] decompress(byte[] compressedBytes) throws IllegalArgumentException {
        ByteArrayOutputStream rawBytes = new ByteArrayOutputStream();
        for(int i = 0; i < compressedBytes.length; i++){
            // Decompresses and moves the pointer along
            int runLengthNum;
            if(compressedBytes[i] < 0){
                runLengthNum = (int) compressedBytes[i] + 256;
            }
            else{
                runLengthNum = compressedBytes[i];
            }
            byte runLengthChar = compressedBytes[i+1];
            for(int j = 0; j < runLengthNum; j++){
                rawBytes.write(runLengthChar);
            }
            i++;
        }
        return rawBytes.toByteArray();
    }
}
