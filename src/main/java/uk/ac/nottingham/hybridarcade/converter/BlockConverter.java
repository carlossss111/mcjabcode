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

    // Constructor initializes the map from the resources directory
    // This maps the block name e.g. 'block.cobblestone' to a number.
    private BlockConverter() {
        try {
            String path = getClass().getClassLoader().getResource(BLOCKMAP_PATH).getPath();
            if(path.split("%.*!").length > 1) { // I don't know why but a weird substring gets inserted and needs to be removed, e.g. "%124!"
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

    //Getters
    public HashMap<String, Byte> getBlockMap() {
        return mBlockMap;
    }

    public HashMap<Byte, String> getBlockMapInverted() {
        return mBlockMapInverted;
    }


    // Inverts map so that the original key is now the value etc.
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

    // Convert blocks into a bytestream
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
        return stream.toByteArray();
    }

    // Convert bytestream into blocks
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
                    blockId = String.format("%s:%s", blockId.split("\\.")[1], blockId.split("\\.")[2]);
                    blocks[i][j][k] = ForgeRegistries.BLOCKS.getValue(
                            ResourceLocation.tryParse(blockId)
                    ).defaultBlockState();
                }
            }
        }

        return blocks;
    }
}
