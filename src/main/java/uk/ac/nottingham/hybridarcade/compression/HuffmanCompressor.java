package uk.ac.nottingham.hybridarcade.compression;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;

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
     * @param rawBytes bytes to code
     */
    void buildTree(byte[] rawBytes){
        // Get frequency of each byte
        List<Byte> list = Arrays.asList(ArrayUtils.toObject(rawBytes));
        Map<Byte, Integer> freqBytes = list.stream()
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        v -> Collections.frequency(list, v))
                );

        // Build Priority Queue
        PriorityQueue<Node> pQueue =
                new PriorityQueue<>(freqBytes.size(), new HuffComparator());
        for (Byte key : freqBytes.keySet()) {
            Node node = new Node();
            node.rawByte = key;
            node.frequency = freqBytes.get(key);
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

    // TODO wip
    byte getFromTree(BitSet bits){
        Node node = mRoot;
        // Traverse tree according to bits
        int i = 0;
        for(;;){
            if(bits.get(i) == true){
                if(node.right != null) {
                    node = node.right;
                }
                else{
                    return node.rawByte;
                }
            }
            else{
                if(node.left != null) {
                    node = node.left;
                }
                else{
                    return node.rawByte;
                }
            }
            i++;
        }
    }


    /**
     * Recursive function that traverses the tree in a preorder traversal and constructs
     * a table mapping the character to the coding.
     * @param node current node pointer
     * @param bitStr string of 'l' and 'r's to indicate the node's depth and position
     */
    void traverseAndStore(Node node, String bitStr) {
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
        traverseAndStore(node.left, bitStr + "l");
        traverseAndStore(node.right, bitStr + "r");
    }

    void buildTableFromTree(){
        traverseAndStore(mRoot, "");
    };

    void buildHeader(){
        throw new NotImplementedException();
    }

    void buildTableFromHeader(){
        throw new NotImplementedException();
    }

    void buildTreeFromTable(){
        throw new NotImplementedException();
    }



    @Override
    public byte[] compress(byte[] rawBytes) {
        // Construct the coding
        buildTree(rawBytes);
        buildTableFromTree();

        // Construct the message header containing the tree in some form
        BitSet resultSet = new BitSet();
        //buildHeader(); //TODO

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
        return resultSet.toByteArray();
    }

    @Override
    public byte[] decompress(byte[] compressedBytes) throws IllegalArgumentException {
        buildTableFromHeader();
        buildTreeFromTable();

        /*
        TODO
        <<Psuedocode>>
        Loop through compressedBytes bits:
            follow the tree,
            when reaching a leaf node:
                add corresponding byte to output,
                reset position in the tree to the root
         */
        return null;
    }
}
