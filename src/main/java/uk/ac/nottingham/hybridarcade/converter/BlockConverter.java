package uk.ac.nottingham.hybridarcade.converter;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import uk.ac.nottingham.hybridarcade.Constants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts a 3-dimensional array of blocks into an array of Bytes and visa versa.
 * This class uses a mapping of blocks to byte values (0-255) to do the conversion.
 * <br/><br/>See also: resources/converter/blockmap256.json
 * @author Daniel Robinson 2024
 */
public class BlockConverter {
    private static final String BLOCKMAP_PATH = "converter/blockmap256.json";
    private static final String PLACEHOLDER_BLOCK = "block.minecraft.air";

    HashMap<String, Byte> mBlockMap;
    HashMap<Byte, String> mBlockMapInverted;

    // Singleton pattern prevents repeated computation initializing the block maps
    private static BlockConverter instance;
    public static BlockConverter getInstance() {
        if(instance == null) {
            instance = new BlockConverter();
        }
        return instance;
    }

    /**
     * Constructor loads the BlockMap from disk into memory as a hashmap. It also
     * stores an inverted version of the map.
     * @see #invertMap(Map) 
     */
    private BlockConverter() {
        try {
            String path = getClass().getClassLoader().getResource(BLOCKMAP_PATH).getPath();
            // I don't know why but a weird substring gets inserted and needs to be removed, e.g. "%124!"
            if(path.split("%.*!").length > 1) {
                path = path.split("%.*!")[0] + path.split("%.*!")[1];
            }
            Reader reader = Files.newBufferedReader(Path.of(path));
            Type T = new TypeToken<HashMap<String, Byte>>() {}.getType();
            mBlockMap = new Gson().fromJson(reader, T);
            mBlockMapInverted = (HashMap<Byte, String>) invertMap(mBlockMap);
        }
        catch (NullPointerException | IOException e ) {
            Constants.logger.error("BlockConverter could not be initialised with a map: "
                    + BLOCKMAP_PATH + " is invalid");
            throw new RuntimeException(e);
        }
    };

    /**
     * Gets the block map as a hashmap.
     */
    public HashMap<String, Byte> getBlockMap() {
        return mBlockMap;
    }

    /**
     * Gets the block map as an inverted hashmap.
     */
    public HashMap<Byte, String> getBlockMapInverted() {
        return mBlockMapInverted;
    }


    /**
     * Inverts the map such that each key is now the value and visa versa.
     * @param map The map to invert
     * @return The inverted map
     */
    private  <K, V> Map<V, K> invertMap(Map<K, V> map) {
        return map.entrySet()
                .stream().collect(
                        Collectors.toMap(
                                Map.Entry::getValue,
                                Map.Entry::getKey,
                                (oldValue, newValue) -> oldValue
                        )
                );
    }

    /**
     * Converts a 3-dimensional array of blocks into a Byte array using the
     * block map.
     * @param blocks 3-dimensional array of game blocks to convert.
     * @return Array of mapped bytes.
     */
    public byte[] toBytes(BlockState[][][] blocks){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // First 3 bytes indicate the dimensions of the array
        stream.write(blocks.length);
        stream.write(blocks[0].length);
        stream.write(blocks[0][0].length);

        // Populate with values form the 3d array
        for(int i = 0; i < blocks.length; i++){
            for (int j = 0; j < blocks[i].length; j++){
                for (int k = 0; k < blocks[i][j].length; k++){
                    String blockId = blocks[i][j][k].getBlock().getDescriptionId();
                    if(mBlockMap.containsKey(blockId)) {
                        stream.write(mBlockMap.get(blockId));
                    }
                    else{
                        stream.write(mBlockMap.get(PLACEHOLDER_BLOCK));
                    }
                }
            }
        }
        stream.write(0);
        return stream.toByteArray();
    }

    /**
     * Converts an array of Bytes into a 3-dimensional array of blocks using
     * the block map.
     * @param bytes Bytes to convert.
     * @return 3-dimensional array of game blocks mapped from bytes.
     */
    public BlockState[][][] toBlocks(byte[] bytes){
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

        // First 3 bytes indicate sizes
        int i_length = stream.read();
        int j_length = stream.read();
        int k_length = stream.read();

        BlockState[][][] blocks = new BlockState[i_length][j_length][k_length];

        // Populate blocks by getting their name from the map and looking it up in the registry
        for(int i = 0; i < i_length; i++){
            for (int j = 0; j < j_length; j++){
                for (int k = 0; k < k_length; k++){
                    byte blockByte = (byte) stream.read();
                    String blockId = mBlockMapInverted.get(blockByte);
                    if(blockId != null && blockId != "RESERVED") {
                        blockId = String.format("%s:%s", blockId.split("\\.")[1], blockId.split("\\.")[2]);
                    }
                    else{
                        blockId = "minecraft:air";
                    }
                    blocks[i][j][k] = ForgeRegistries.BLOCKS.getValue(
                            ResourceLocation.tryParse(blockId)
                    ).defaultBlockState();
                }
            }
        }

        return blocks;
    }
}
