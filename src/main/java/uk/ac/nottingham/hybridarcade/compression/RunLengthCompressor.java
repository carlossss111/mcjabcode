package uk.ac.nottingham.hybridarcade.compression;

import uk.ac.nottingham.hybridarcade.Constants;

import java.io.ByteArrayOutputStream;

public class RunLengthCompressor implements ICompressor{
    private final static byte RL = Constants.RESERVED_FOR_COMPRESSION_TK;

    private byte[] convertToRunlength(byte[] byteChunk, int size){
        // compress with RL flag, followed by the number of bytes, followed by byte type
        // e.g. [RL][4][A] == AAAA
        ByteArrayOutputStream rlChunk = new ByteArrayOutputStream();
        do{
            // at lengths less than 4, there is no profit from RL compression
            if(size < 4){
                for(int i = 0; i < size; i++){
                    rlChunk.write(byteChunk[0]);
                }
                break;
            }
            rlChunk.write(RL);
            rlChunk.write(Math.min(size, 255));
            rlChunk.write(byteChunk[0]);
            size -= 256;
        }while(size > 0);

        return rlChunk.toByteArray();
    }

    @Override
    public byte[] compress(byte[] rawBytes) {
        ByteArrayOutputStream compressedBytes = new ByteArrayOutputStream();
        ByteArrayOutputStream byteChunk = new ByteArrayOutputStream();
        for(int i = 0; i < rawBytes.length; i++){
            /**
             * If byte i is the same as last, write it into the chunk
             * if it is different or we have reached the end, copy the chunk and write into a fresh one
             */
            if(i >= 1 && rawBytes[i] != rawBytes[i-1]){
                compressedBytes.writeBytes(
                        convertToRunlength(byteChunk.toByteArray(), byteChunk.size())
                );
                byteChunk.reset();;
            }
            byteChunk.write(rawBytes[i]);
        }
        // On last time when exiting loop
        compressedBytes.writeBytes(
                convertToRunlength(byteChunk.toByteArray(), byteChunk.size())
        );
        return  compressedBytes.toByteArray();
    }

    @Override
    public byte[] decompress(byte[] compressedBytes) throws IllegalArgumentException{
        ByteArrayOutputStream rawBytes = new ByteArrayOutputStream();
        for(int i = 0; i < compressedBytes.length; i++){
            if(compressedBytes[i] == RL){
                // Validation
                if(i + 2 >= compressedBytes.length
                        || compressedBytes[i+1] == RL
                        || compressedBytes[i+2] == RL){
                    throw new IllegalArgumentException(
                            "The bytestream being decompressed is not a valid compression!");
                }

                // Decompresses and moves the pointer along
                byte runLengthNum = compressedBytes[i+1];
                byte runLengthChar = compressedBytes[i+2];
                for(int j = 0; j < runLengthNum; j++){
                    rawBytes.write(runLengthChar);
                }
                i+=2;
            }
            else{
                rawBytes.write(compressedBytes[i]);
            }
        }
        return rawBytes.toByteArray();
    }
}
