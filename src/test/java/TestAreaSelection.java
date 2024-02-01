import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.nottingham.hybridarcade.mechanics.AreaSelection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class TestAreaSelection {
    AreaSelection mAreaSelection = null;
    LevelAccessor mLevel = null;

    @BeforeEach
    public void setup() {
        mAreaSelection = new AreaSelection();
        mLevel = mock(LevelAccessor.class);
    }

    /**** Basics ****/

    final static BlockPos[] EXAMPLE_VERTICES = {new BlockPos(1,2,3),
            new BlockPos(10,11,12), new BlockPos(21,22,23), new BlockPos(31,32,33),
            new BlockPos(41,42,43), new BlockPos(51,52,53), new BlockPos(61,62,63)};

    @Test
    public void testAddVertex() {
        for(int i = 0; i < 4; i++){
            mAreaSelection.addMarker(EXAMPLE_VERTICES[i]);
        }
        for(int i = 0; i < 4; i++){
            assertEquals(EXAMPLE_VERTICES[i], mAreaSelection.getMarker(i));
        }
    }

    @Test
    public void testVerticesClearWhenFull() {
        for(int i = 0; i < 4; i ++){
            mAreaSelection.addMarker(EXAMPLE_VERTICES[0]);
        }
        mAreaSelection.addMarker(EXAMPLE_VERTICES[4]);

        assertEquals(EXAMPLE_VERTICES[4], mAreaSelection.getMarker(0));
        assertNull(mAreaSelection.getMarker(1));
        assertNull(mAreaSelection.getMarker(2));
        assertNull(mAreaSelection.getMarker(3));
    }

    @Test
    public void testGetVertexAsString() {
        mAreaSelection.addMarker(new BlockPos(100,200,300));
        assertEquals("[x100,y200,z300]", mAreaSelection.getMarkerAsString(0));
    }

    @Test
    public void testGetVertexOutOfIndex() {
        assertNull(mAreaSelection.getMarker(999));
    }

    /**** Mathsy Stuff ****/

    @Test
    public void testValidSelection() {
        // Standard
        mAreaSelection.addMarker(new BlockPos(0,0,0));
        mAreaSelection.addMarker(new BlockPos(1,0,0));
        mAreaSelection.addMarker(new BlockPos(0,0,1));
        mAreaSelection.addMarker(new BlockPos(0,1,0));
        assertNotEquals(0, mAreaSelection.storeAndPrintBlocks(mLevel));

        // From a different direction
        mAreaSelection.addMarker(new BlockPos(50,50,50));
        mAreaSelection.addMarker(new BlockPos(50,30,50));
        mAreaSelection.addMarker(new BlockPos(80,50,50));
        mAreaSelection.addMarker(new BlockPos(50,50,10));
        assertNotEquals(0, mAreaSelection.storeAndPrintBlocks(mLevel));
    }

    @Test
    public void testInvalidSelection() {
        // Diagonal
        mAreaSelection.addMarker(new BlockPos(0,0,0));
        mAreaSelection.addMarker(new BlockPos(1,0,0));
        mAreaSelection.addMarker(new BlockPos(0,1,1));
        mAreaSelection.addMarker(new BlockPos(0,1,0));
        assertEquals(0, mAreaSelection.storeAndPrintBlocks(mLevel));

        // Square
        mAreaSelection.addMarker(new BlockPos(0,0,0));
        mAreaSelection.addMarker(new BlockPos(1,0,0));
        mAreaSelection.addMarker(new BlockPos(0,1,0));
        mAreaSelection.addMarker(new BlockPos(1,1,0));
        assertEquals(0, mAreaSelection.storeAndPrintBlocks(mLevel));

        // Straight Line
        mAreaSelection.addMarker(new BlockPos(0,0,1));
        mAreaSelection.addMarker(new BlockPos(0,0,2));
        mAreaSelection.addMarker(new BlockPos(0,0,3));
        mAreaSelection.addMarker(new BlockPos(0,0,4));
        assertEquals(0, mAreaSelection.storeAndPrintBlocks(mLevel));

        // Null values
        mAreaSelection.addMarker(new BlockPos(0,0,0));
        mAreaSelection.addMarker(null);
        mAreaSelection.addMarker(null);
        mAreaSelection.addMarker(null);
        assertEquals(0, mAreaSelection.storeAndPrintBlocks(mLevel));
    }

    @Test
    public void testStoresCorrectNumberOfBlocks() {
        // Unit Cube
        mAreaSelection.addMarker(new BlockPos(0,0,0));
        mAreaSelection.addMarker(new BlockPos(1,0,0));
        mAreaSelection.addMarker(new BlockPos(0,0,1));
        mAreaSelection.addMarker(new BlockPos(0,1,0));
        assertEquals(8, mAreaSelection.storeAndPrintBlocks(mLevel));

        // Negative Unit Cube
        mAreaSelection.addMarker(new BlockPos(0,0,0));
        mAreaSelection.addMarker(new BlockPos(-1,0,0));
        mAreaSelection.addMarker(new BlockPos(0,0,1));
        mAreaSelection.addMarker(new BlockPos(0,-1,0));
        assertEquals(8, mAreaSelection.storeAndPrintBlocks(mLevel));

        // Cuboid
        // Remember +1 is added to the max of each so the marker itself is included
        mAreaSelection.addMarker(new BlockPos(0,0,0));
        mAreaSelection.addMarker(new BlockPos(-9,0,0));
        mAreaSelection.addMarker(new BlockPos(0,0,29));
        mAreaSelection.addMarker(new BlockPos(0,19,0));
        assertEquals(6000, mAreaSelection.storeAndPrintBlocks(mLevel));
    }

}
