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
 * WIP
 */
public class HuffmanCompressor implements ICompressor {
    /**
     * Node for the huffman coding tree. Each node should contain the frequency
     * of all characters underneath it including itself if it is a character.
     * If it is a character, then that will be stored in rawByte, otherwise
     * 255 is used as reserved.
     */
    private class Node {
        byte rawByte;
        int frequency = 0;
        Node left = null;
        Node right = null;
    }

    // Used to build the tree
    private class HuffComparator implements Comparator<Node> {
        public int compare(Node x, Node y){
            return x.frequency - y.frequency;
        }
    }

    // Used when coding with the hashmap
    private class BitSetExtra extends BitSet{
        private int realSize = 0;
        public void setRealSize(int realSize){this.realSize = realSize;}
        public int getRealSize(){return realSize;}
    }

    // Root node of the tree
    private Node mRoot;

    // Map used in coding that maps each byte to a bitset with a length
    private final HashMap<Byte, BitSetExtra> mBinaryTable = new HashMap<>();

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
                new PriorityQueue<>(bytesAndFreq.size(), new HuffComparator());
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
     * a table mapping the character to the coding.
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

    private byte shortToFirstByte(int num){
        short snum = (short) num;
        return (byte)(snum & 0xFF);
    }

    private byte shortToSecondByte(int num){
        short snum = (short) num;
        return (byte)((snum >> 8) & 0xFF);
    }

    private short bytesToShort(byte b0, byte b1){
        return (short) ((short) ((b1 & 0xFF) << 8) | (b0 & 0xFF));
    }

    ByteArrayOutputStream buildHeader(Map<Byte, Integer> bytesAndFreq){
        ByteArrayOutputStream header = new ByteArrayOutputStream();

        PriorityQueue<Node> pQueue =
                new PriorityQueue<>(bytesAndFreq.size(), new HuffComparator());
        for (Byte key : bytesAndFreq.keySet()) {
            Node node = new Node();
            node.rawByte = key;
            node.frequency = bytesAndFreq.get(key);
            pQueue.add(node);
        }

        header.write(0);
        header.write(0);
        header.write(shortToFirstByte(pQueue.size()*3 + 4));
        header.write(shortToSecondByte(pQueue.size()*3 + 4));
        while(pQueue.size() > 0){
            Node node = pQueue.remove();
            header.write(node.rawByte);
            header.write(shortToFirstByte(node.frequency));
            header.write(shortToSecondByte(node.frequency));
        }

        return header;
    }

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
        ByteArrayOutputStream resultStream = buildHeader(freqBytes);

        /*
            Loop through the entire raw byte array, and get each byte as a coded
            bitset by using the newly constructed hashtable.
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
        //Little Endian, smallest bits first
        resultStream.writeBytes(resultSet.toByteArray());
        byte[] resultArr = resultStream.toByteArray();
        resultArr[0] = shortToFirstByte(resultIndex);
        resultArr[1] = shortToSecondByte(resultIndex);
        return resultArr;
    }

    @Override
    public byte[] decompress(byte[] compressedBytes) throws IllegalArgumentException {

        int compressedBitSize = bytesToShort(compressedBytes[0], compressedBytes[1]);

        // Read the header
        Map<Byte, Integer> byteFreq = new HashMap<>();
        int headerSize = bytesToShort(compressedBytes[2], compressedBytes[3]);
        for(int i = 4; i < headerSize; i+= 3){
            int freq = bytesToShort(compressedBytes[i+1], compressedBytes[i+2]);
            byteFreq.put(compressedBytes[i], freq);
        }

        // Tree built should be exactly the same.
        buildTree(byteFreq);

        /*
        Loop through compressedBytes bits:
            follow the tree,
            when reaching a leaf node:
                add corresponding byte to output,
                reset position in the tree to the root
         */
        ByteArrayOutputStream rawStream = new ByteArrayOutputStream();
        ByteArrayInputStream compressedStream = new ByteArrayInputStream(compressedBytes);
        BitInputStream bits = new BitInputStream(compressedStream, ByteOrder.LITTLE_ENDIAN);
        try {
            //Skip Header
            for (int i = 0; i < headerSize; i++) {
                bits.readBits(8);
            }

            Node node = mRoot;
            for(int i = 0; i < compressedBitSize; i++) {
                long current = bits.readBits(1);
                // LEFT
                if (current == 0 || current == -1){
                    node = node.left;
                    if(node.left == null && node.right == null){
                        rawStream.write(node.rawByte);
                        node = mRoot;
                    }
                }
                //RIGHT
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
