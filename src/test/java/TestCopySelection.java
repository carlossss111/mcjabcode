import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.nottingham.hybridarcade.mechanics.CopySelection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class TestCopySelection {
    CopySelection mCopySelection = null;
    LevelAccessor mLevel = null;

    @BeforeEach
    public void setup() {
        mCopySelection = new CopySelection();
        mLevel = mock(LevelAccessor.class);
    }

    /**** Basics ****/

    final static BlockPos[] EXAMPLE_VERTICES = {new BlockPos(1,2,3),
            new BlockPos(10,11,12), new BlockPos(21,22,23), new BlockPos(31,32,33),
            new BlockPos(41,42,43), new BlockPos(51,52,53), new BlockPos(61,62,63)};

    @Test
    public void testAddVertex() {
        for(int i = 0; i < 4; i++){
            mCopySelection.addMarker(EXAMPLE_VERTICES[i]);
        }
        for(int i = 0; i < 4; i++){
            assertEquals(EXAMPLE_VERTICES[i], mCopySelection.getMarker(i));
        }
    }

    @Test
    public void testVerticesClearWhenFull() {
        for(int i = 0; i < 4; i ++){
            mCopySelection.addMarker(EXAMPLE_VERTICES[0]);
        }
        mCopySelection.addMarker(EXAMPLE_VERTICES[4]);

        assertEquals(EXAMPLE_VERTICES[4], mCopySelection.getMarker(0));
        assertNull(mCopySelection.getMarker(1));
        assertNull(mCopySelection.getMarker(2));
        assertNull(mCopySelection.getMarker(3));
    }

    @Test
    public void testGetVertexAsString() {
        mCopySelection.addMarker(new BlockPos(100,200,300));
        assertEquals("[x100,y200,z300]", mCopySelection.getMarkerAsString(0));
    }

    @Test
    public void testGetVertexOutOfIndex() {
        assertNull(mCopySelection.getMarker(999));
    }

    /**** Mathsy Stuff ****/

    @Test
    public void testValidSelection() {
        // Standard
        mCopySelection.addMarker(new BlockPos(0,0,0));
        mCopySelection.addMarker(new BlockPos(1,0,0));
        mCopySelection.addMarker(new BlockPos(0,0,1));
        mCopySelection.addMarker(new BlockPos(0,1,0));
        assertNotEquals(0, mCopySelection.copyAndPrintBlocks(mLevel));

        // From a different direction
        mCopySelection.addMarker(new BlockPos(50,50,50));
        mCopySelection.addMarker(new BlockPos(50,30,50));
        mCopySelection.addMarker(new BlockPos(80,50,50));
        mCopySelection.addMarker(new BlockPos(50,50,10));
        assertNotEquals(0, mCopySelection.copyAndPrintBlocks(mLevel));
    }

    @Test
    public void testInvalidSelection() {
        // Diagonal
        mCopySelection.addMarker(new BlockPos(0,0,0));
        mCopySelection.addMarker(new BlockPos(1,0,0));
        mCopySelection.addMarker(new BlockPos(0,1,1));
        mCopySelection.addMarker(new BlockPos(0,1,0));
        assertEquals(0, mCopySelection.copyAndPrintBlocks(mLevel));

        // Square
        mCopySelection.addMarker(new BlockPos(0,0,0));
        mCopySelection.addMarker(new BlockPos(1,0,0));
        mCopySelection.addMarker(new BlockPos(0,1,0));
        mCopySelection.addMarker(new BlockPos(1,1,0));
        assertEquals(0, mCopySelection.copyAndPrintBlocks(mLevel));

        // Straight Line
        mCopySelection.addMarker(new BlockPos(0,0,1));
        mCopySelection.addMarker(new BlockPos(0,0,2));
        mCopySelection.addMarker(new BlockPos(0,0,3));
        mCopySelection.addMarker(new BlockPos(0,0,4));
        assertEquals(0, mCopySelection.copyAndPrintBlocks(mLevel));

        // Null values
        mCopySelection.addMarker(new BlockPos(0,0,0));
        mCopySelection.addMarker(null);
        mCopySelection.addMarker(null);
        mCopySelection.addMarker(null);
        assertEquals(0, mCopySelection.copyAndPrintBlocks(mLevel));
    }

    @Test
    public void testStoresCorrectNumberOfBlocks() {
        // Unit Cube
        mCopySelection.addMarker(new BlockPos(0,0,0));
        mCopySelection.addMarker(new BlockPos(1,0,0));
        mCopySelection.addMarker(new BlockPos(0,0,1));
        mCopySelection.addMarker(new BlockPos(0,1,0));
        assertEquals(8, mCopySelection.copyAndPrintBlocks(mLevel));

        // Negative Unit Cube
        mCopySelection.addMarker(new BlockPos(0,0,0));
        mCopySelection.addMarker(new BlockPos(-1,0,0));
        mCopySelection.addMarker(new BlockPos(0,0,1));
        mCopySelection.addMarker(new BlockPos(0,-1,0));
        assertEquals(8, mCopySelection.copyAndPrintBlocks(mLevel));

        // Cuboid
        // Remember +1 is added to the max of each so the marker itself is included
        mCopySelection.addMarker(new BlockPos(0,0,0));
        mCopySelection.addMarker(new BlockPos(-9,0,0));
        mCopySelection.addMarker(new BlockPos(0,0,29));
        mCopySelection.addMarker(new BlockPos(0,19,0));
        assertEquals(6000, mCopySelection.copyAndPrintBlocks(mLevel));
    }

}
