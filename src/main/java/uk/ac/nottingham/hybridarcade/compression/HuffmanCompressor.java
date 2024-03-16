package uk.ac.nottingham.hybridarcade.compression;

import org.apache.commons.compress.utils.BitInputStream;
import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Compresses/decompresses a byte array to/from a byte array using Huffman Coding.
 * This coding works by getting the frequency of each character in a stream and
 * constructing a tree. The most frequently used characters appear near the top
 * of the tree. Going left from the tree is representing by a 0 and going right
 * is a 1, therefore the most common characters can be encoded in single or
 * few bits.
 */
public class HuffmanCompressor implements ICompressor {

    /** The root node of the tree built with
     * {@link #buildTree(Map)}  when compressing and decompressing */
    private Node mRoot;

    /** Hashmap used in the compression to loop over the payload
     * and output it's Huffman-Coded equivalent */
    private final HashMap<Byte, BitSetExtra> mBinaryTable = new HashMap<>();

    /** Comparator for constructing a priority queue based on frequency */
    private final Comparator<Node> huffComparator
            = Comparator.comparingInt((Node x) -> x.frequency);

    /**
     * Node class for the huffman coding tree. Each node should contain the
     * frequency of all characters underneath it including itself if it is a
     * character. If it is a character, then that will be stored in rawByte,
     * otherwise 255 is used for debugging.
     */
    private class Node {
        byte rawByte;
        int frequency = 0;
        Node left = null;
        Node right = null;
    }

    /**
     * Helper class that adds the length of the BitSet in Bits.
     * Used exclusively by the {@link #mBinaryTable}.
     */
    private static class BitSetExtra extends BitSet{
        private int realSize = 0;
        public void setRealSize(int realSize){this.realSize = realSize;}
        public int getRealSize(){return realSize;}
    }

    /**
     * Container class for the header of the compressed message. Contains
     * information so that the payload can be read when decompressing.
     * The header is physically stored like so: <br/>
     * <code>[header_size][header_size][payload_size][payload_size][byte][freq][freq]...</code>
     */
    private class HuffHeader{
        /** Size of the compressed content in bits */
        int mPayloadSizeInBits;
        /** Size of {@link #mBytes} */
        int mHeaderSizeInBytes;
        /** Each unique character and it's frequency in the original input */
        Map<Byte, Integer> mValueAndFrequency;
        /** The raw header */
        byte[] mBytes;

        /** First little endian byte of a short*/
        private byte toFirstByte(int num){
            short snum = (short) num;
            return (byte)(snum & 0xFF);
        }

        /** Second little endian byte of a short*/
        private byte toSecondByte(int num){
            short snum = (short) num;
            return (byte)((snum >> 8) & 0xFF);
        }

        /** Short built from two little endian bytes */
        private short toShort(byte b0, byte b1){
            return (short) ((short) ((b1 & 0xFF) << 8) | (b0 & 0xFF));
        }

        /**
         * Prepends the header to the payload.
         * @param payload the actual compressed data
         * @return the full compressed byte array
         */
        byte[] prependToPayload(BitSet payload){
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.writeBytes(mBytes);
            stream.writeBytes(payload.toByteArray());
            return stream.toByteArray();
        }

        /**
         * <b>Decompression</b><br/>
         * Reads the byte array and finds the header and stores it's
         * features as attributes of this class.
         * @param readBytes the entire compressed byte array
         */
        HuffHeader(byte[] readBytes) throws IllegalArgumentException{
            mBytes = readBytes;
            mHeaderSizeInBytes = toShort(readBytes[0], readBytes[1]);

            if(mHeaderSizeInBytes < 4 || mHeaderSizeInBytes > readBytes.length){
                throw new IllegalArgumentException("The huffman header is mangled.");
            }

            mPayloadSizeInBits = toShort(readBytes[2], readBytes[3]);
            mValueAndFrequency = new HashMap<>();
            for(int i = 4; i < mHeaderSizeInBytes; i+= 3){
                mValueAndFrequency.put(
                        readBytes[i],
                        (int) toShort(readBytes[i+1], readBytes[i+2])
                );
            }
        }

        /**
         * <b>Compression</b><br/>
         * Reads a map of the bytes stored and their frequencies, and uses
         * this to construct the header attributes.
         * @param byteFreq the bytes stored and their frequencies
         * @param payloadSize the size of the payload in BITs
         */
        HuffHeader(Map<Byte, Integer> byteFreq, int payloadSize) {
            mValueAndFrequency = byteFreq;

            PriorityQueue<Node> pQueue =
                    new PriorityQueue<>(mValueAndFrequency.size(), huffComparator);
            for (Byte key : mValueAndFrequency.keySet()) {
                Node node = new Node();
                node.rawByte = key;
                node.frequency = mValueAndFrequency.get(key);
                pQueue.add(node);
            }

            // Calculation: payload size (2 bytes) + header size (2 bytes) +
            // {each element (1 byte) + each element's frequency (2 bytes)} * num of elems
            mHeaderSizeInBytes = pQueue.size()*3 + 4;
            mPayloadSizeInBits = payloadSize;

            // First two bytes is the header size (short), next two are payload size (short),
            // then each next three bytes are the stored character and their frequency (short)
            ByteArrayOutputStream hStream = new ByteArrayOutputStream();
            hStream.write(toFirstByte(mHeaderSizeInBytes));
            hStream.write(toSecondByte(mHeaderSizeInBytes));
            hStream.write(toFirstByte(mPayloadSizeInBits));
            hStream.write(toSecondByte(mPayloadSizeInBits));
            while(pQueue.size() > 0){
                Node node = pQueue.remove();
                hStream.write(node.rawByte);
                hStream.write(toFirstByte(node.frequency));
                hStream.write(toSecondByte(node.frequency));
            }
            mBytes = hStream.toByteArray();
        }
    }

    /**
     * Builds a Huffman Coding Binary Tree. Starting from the root, going left
     * represents a '0' and right a '1'. The tree is created such that the
     * most common characters are found nearer to the top, so can be encoded in
     * less bits.
     * @param bytesAndFreq a map of the characters and the frequency their appear at
     */
    void buildTree(Map<Byte, Integer> bytesAndFreq){
        // Build Priority Queue
        PriorityQueue<Node> pQueue =
                new PriorityQueue<>(bytesAndFreq.size(), huffComparator);
        for (Byte key : bytesAndFreq.keySet()) {
            Node node = new Node();
            node.rawByte = key;
            node.frequency = bytesAndFreq.get(key);
            pQueue.add(node);
        }

        // Loop through and build the tree from the leaf-nodes upward
        while(pQueue.size() > 1){
            Node left = pQueue.remove();
            Node right = pQueue.remove();
            mRoot = new Node();
            mRoot.frequency = left.frequency + right.frequency;
            mRoot.rawByte = (byte) 255;
            mRoot.left = left;
            mRoot.right = right;
            pQueue.add(mRoot);
        }
    }

    /**
     * Recursive function that traverses the tree in a preorder traversal and constructs
     * a table mapping the character to the coding. This is to speed up the compression.
     * @param node current node pointer
     * @param bitStr string of 'l' and 'r's to indicate the node's depth and position
     */
    void buildTableRecursively(Node node, String bitStr) {
        if (node == null) {
            return;
        }

        // Act on current node by adding to binary table
        BitSetExtra bitSet = new BitSetExtra();
        for(int i = 0; i < bitStr.length(); i++){
            if(bitStr.charAt(i) == 'r'){
                bitSet.set(i);
            }
            bitSet.setRealSize(i+1);
        }
        mBinaryTable.put(node.rawByte, bitSet);

        // Recurse in both directions
        buildTableRecursively(node.left, bitStr + "l");
        buildTableRecursively(node.right, bitStr + "r");
    }

    /**
     * Compresses bytes with Huffman Coding.
     * @param rawBytes Bytes to compress.
     * @return compressed bytes
     * @see HuffmanCompressor
     */
    @Override
    public byte[] compress(byte[] rawBytes) {
        // Get Mapping of bytes and their frequencies
        List<Byte> list = Arrays.asList(ArrayUtils.toObject(rawBytes));
        Map<Byte, Integer> freqBytes = list.stream()
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        v -> Collections.frequency(list, v))
                );

        // Construct the coding
        buildTree(freqBytes);
        buildTableRecursively(mRoot, "");

        // Construct the message header containing the tree in some form
        BitSet resultSet = new BitSet();

        /*
            Loop through the entire raw byte array, and get each byte as a coded
            bitset by using the hashtable.
         */
        int resultIndex = 0;
        for(int rawIndex = 0; rawIndex < rawBytes.length; rawIndex++){
            BitSetExtra current = mBinaryTable.get(rawBytes[rawIndex]);
            for(int currIndex = 0; currIndex < current.getRealSize(); currIndex++){
                if(current.get(currIndex) == true){
                    resultSet.set(resultIndex);
                }
                resultIndex++;
            }
        }

        HuffHeader header = new HuffHeader(freqBytes, resultIndex);
        return header.prependToPayload(resultSet);
    }

    /**
     * Decompresses bytes with the Huffman Coding.
     * @param compressedBytes Compressed Bytes to decompress.
     * @return Decompressed bytes.
     * @throws IllegalArgumentException if the input is invalid
     */
    @Override
    public byte[] decompress(byte[] compressedBytes) throws IllegalArgumentException {
        // Construct the header
        HuffHeader header = new HuffHeader(compressedBytes);

        // Rebuild the tree the same as it was when compressed
        buildTree(header.mValueAndFrequency);

        /*
           Loop through the payload in bits and follow the tree down either
           left (bit = 0), or right (bit = 1) until a leaf node is reached, so
           it's value can be written to the output.
         */
        ByteArrayOutputStream rawStream = new ByteArrayOutputStream();
        ByteArrayInputStream compressedStream = new ByteArrayInputStream(compressedBytes);
        BitInputStream bits = new BitInputStream(compressedStream, ByteOrder.LITTLE_ENDIAN);
        try {
            // Skip Header
            for (int i = 0; i < header.mHeaderSizeInBytes; i++) {
                bits.readBits(8);
            }

            // The Work
            Node node = mRoot;
            for(int i = 0; i < header.mPayloadSizeInBits; i++) {
                long current = bits.readBits(1);
                // Go Left (when =-1 then we have run out of bytes)
                if (current == 0 || current == -1){
                    node = node.left;
                    if(node.left == null && node.right == null){
                        rawStream.write(node.rawByte);
                        node = mRoot;
                    }
                }
                // Go Right
                else if (current == 1){
                    node = node.right;
                    if(node.left == null && node.right == null){
                        rawStream.write(node.rawByte);
                        node = mRoot;
                    }
                }
            }
        }
        catch(IOException e){
            throw new IllegalArgumentException(e);
        }
        return rawStream.toByteArray();
    }
}
