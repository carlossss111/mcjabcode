import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.nottingham.hybridarcade.converter.BlockConverter;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestBlockConverter {

    /**
     * converter/blockmap256.json:
     * {
     *   "block.minecraft.air":            0,
     *   "block.minecraft.stone":          1,
     *   "block.minecraft.grass_block":    2
     * }
     */

    @BeforeEach
    public void setup() {

    }

    @Test
    public void testConstructsMap() {
        BlockConverter blockConverter = BlockConverter.getInstance();
        HashMap<String, Byte> blockMap = blockConverter.getBlockMap();
        Map<String, Byte> targetMap =
                Map.of( "block.minecraft.air",          (byte) 0,
                        "block.minecraft.stone",        (byte) 1,
                        "block.minecraft.grass_block",  (byte) 2
                );
        assertEquals(3, blockMap.size());
        assertEquals(targetMap, blockMap);
    }

    @Test
    public void testConstructsMapInverted() {
        BlockConverter blockConverter = BlockConverter.getInstance();
        HashMap<Byte, String> blockMap = blockConverter.getBlockMapInverted();
        Map<Byte, String> targetMap =
                Map.of( (byte) 0, "block.minecraft.air",
                        (byte) 1, "block.minecraft.stone",
                        (byte) 2, "block.minecraft.grass_block"
                );
        assertEquals(3, blockMap.size());
        assertEquals(targetMap, blockMap);
    }
}
