import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.nottingham.hybridarcade.mechanics.AreaSelection;

import static org.junit.jupiter.api.Assertions.*;

public class TestAreaSelection {
    AreaSelection areaSelection = null;

    final static BlockPos[] EXAMPLE_VERTICES = {new BlockPos(1,2,3),
            new BlockPos(10,11,12), new BlockPos(21,22,23), new BlockPos(31,32,33),
            new BlockPos(41,42,43), new BlockPos(51,52,53), new BlockPos(61,62,63)};

    @BeforeEach
    public void setup() {
        areaSelection = new AreaSelection();
    }

    @Test
    public void testAddVertex() {
        for(int i = 0; i < 4; i++){
            areaSelection.addVertex(EXAMPLE_VERTICES[i]);
        }
        for(int i = 0; i < 4; i++){
            assertEquals(EXAMPLE_VERTICES[i], areaSelection.getVertex(i));
        }
    }

    @Test
    public void testVerticesClearWhenFull() {
        for(int i = 0; i < 4; i ++){
            areaSelection.addVertex(EXAMPLE_VERTICES[0]);
        }
        areaSelection.addVertex(EXAMPLE_VERTICES[4]);

        assertEquals(EXAMPLE_VERTICES[4], areaSelection.getVertex(0));
        assertNull(areaSelection.getVertex(1));
        assertNull(areaSelection.getVertex(2));
        assertNull(areaSelection.getVertex(3));
    }

    @Test
    public void testGetVertexAsString() {
        areaSelection.addVertex(new BlockPos(100,200,300));
        assertEquals("[x100,y200,z300]", areaSelection.getVertexAsString(0));
    }

    @Test
    public void testGetVertexOutOfIndex() {
        assertNull(areaSelection.getVertex(999));
    }

    @Test
    public void testValidSelection() {
        // Standard
        areaSelection.addVertex(new BlockPos(0,0,0));
        areaSelection.addVertex(new BlockPos(1,0,0));
        areaSelection.addVertex(new BlockPos(0,0,1));
        areaSelection.addVertex(new BlockPos(0,1,0));
        assertNotEquals(0, areaSelection.storeBlocks());

        // From a different direction
        areaSelection.addVertex(new BlockPos(50,50,50));
        areaSelection.addVertex(new BlockPos(50,30,50));
        areaSelection.addVertex(new BlockPos(80,50,50));
        areaSelection.addVertex(new BlockPos(50,50,10));
        assertNotEquals(0, areaSelection.storeBlocks());
    }

    @Test
    public void testInvalidSelection() {
        // Diagonal
        areaSelection.addVertex(new BlockPos(0,0,0));
        areaSelection.addVertex(new BlockPos(1,0,0));
        areaSelection.addVertex(new BlockPos(0,1,1));
        areaSelection.addVertex(new BlockPos(0,1,0));
        assertEquals(0, areaSelection.storeBlocks());

        // Square
        areaSelection.addVertex(new BlockPos(0,0,0));
        areaSelection.addVertex(new BlockPos(1,0,0));
        areaSelection.addVertex(new BlockPos(0,1,0));
        areaSelection.addVertex(new BlockPos(1,1,0));
        assertEquals(0, areaSelection.storeBlocks());

        // Straight Line
        areaSelection.addVertex(new BlockPos(0,0,1));
        areaSelection.addVertex(new BlockPos(0,0,2));
        areaSelection.addVertex(new BlockPos(0,0,3));
        areaSelection.addVertex(new BlockPos(0,0,4));
        assertEquals(0, areaSelection.storeBlocks());

        // Null values
        areaSelection.addVertex(new BlockPos(0,0,0));
        areaSelection.addVertex(null);
        areaSelection.addVertex(null);
        areaSelection.addVertex(null);
        assertEquals(0, areaSelection.storeBlocks());
    }

}
