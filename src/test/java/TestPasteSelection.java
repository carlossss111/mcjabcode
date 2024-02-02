import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.nottingham.hybridarcade.Utility;
import uk.ac.nottingham.hybridarcade.mechanics.PasteSelection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestPasteSelection {
    private final BlockPos CENTER_POSITION = new BlockPos(0,0,0);
    PasteSelection mPasteSelection = null;
    LevelAccessor mLevel = null;

    @BeforeEach
    public void setup() {
        mPasteSelection = new PasteSelection();
        mLevel = mock(LevelAccessor.class);
    }

    @Test
    public void testInvalidSelectionNull() {
        BlockState blocks[][][] = null;
        Utility.debugBlocks = blocks; // TODO: replace with mock
        mPasteSelection.scanAndStoreBlocks();
        int numStored = mPasteSelection.pasteBlocks(mLevel, CENTER_POSITION);
        verify(mLevel, times(0)).setBlock(any(), any(), anyInt());
        assertEquals(0, numStored);
    }

    @Test
    public void testPastesSelectionBasic() {
        BlockState[][][] blocks = new BlockState[2][2][2];
        Utility.debugBlocks = blocks; // TODO: replace with mock
        mPasteSelection.scanAndStoreBlocks();
        int numStored = mPasteSelection.pasteBlocks(mLevel, CENTER_POSITION);
        verify(mLevel, times(8)).setBlock(any(), any(), anyInt());
        assertEquals(8, numStored);
    }

    @Test
    public void testPastesSelectionInCorrectPlace() {
        BlockState[][][] blocks = new BlockState[2][2][2];
        Utility.debugBlocks = blocks; // TODO: replace with mock
        mPasteSelection.scanAndStoreBlocks();
        int numStored = mPasteSelection.pasteBlocks(mLevel, new BlockPos(10,10,10));
        verify(mLevel).setBlock(eq(new BlockPos(10,10,10)), any(), anyInt());
        verify(mLevel).setBlock(eq(new BlockPos(10,10,11)), any(), anyInt());
        verify(mLevel).setBlock(eq(new BlockPos(10,11,10)), any(), anyInt());
        verify(mLevel).setBlock(eq(new BlockPos(10,11,11)), any(), anyInt());
        verify(mLevel).setBlock(eq(new BlockPos(11,10,10)), any(), anyInt());
        verify(mLevel).setBlock(eq(new BlockPos(11,10,11)), any(), anyInt());
        verify(mLevel).setBlock(eq(new BlockPos(11,11,10)), any(), anyInt());
        verify(mLevel).setBlock(eq(new BlockPos(11,11,11)), any(), anyInt());
        assertEquals(8, numStored);
    }
}
