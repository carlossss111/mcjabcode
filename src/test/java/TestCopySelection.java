import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.ac.nottingham.hybridarcade.converter.BlockConverter;
import uk.ac.nottingham.hybridarcade.game.CopySelection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestCopySelection {
    CopySelection mCopySelectionMock = null;
    LevelAccessor mLevelMock = null;
    BlockConverter mBlockConverterMock = null;

    @BeforeEach
    public void setup() {
        mBlockConverterMock = mock(BlockConverter.class);
        mCopySelectionMock = new CopySelection(mBlockConverterMock);
        mLevelMock = mock(LevelAccessor.class);
    }

    @Nested
    class TestBasics {
        final static BlockPos[] EXAMPLE_VERTICES = {new BlockPos(1, 2, 3),
                new BlockPos(10, 11, 12), new BlockPos(21, 22, 23), new BlockPos(31, 32, 33),
                new BlockPos(41, 42, 43), new BlockPos(51, 52, 53), new BlockPos(61, 62, 63)};

        @Test
        public void testAddVertex() {
            for (int i = 0; i < 4; i++) {
                mCopySelectionMock.addMarker(EXAMPLE_VERTICES[i]);
            }
            for (int i = 0; i < 4; i++) {
                assertEquals(EXAMPLE_VERTICES[i], mCopySelectionMock.getMarker(i));
            }
        }

        @Test
        public void testVerticesClearWhenFull() {
            for (int i = 0; i < 4; i++) {
                mCopySelectionMock.addMarker(EXAMPLE_VERTICES[0]);
            }
            mCopySelectionMock.addMarker(EXAMPLE_VERTICES[4]);

            assertEquals(EXAMPLE_VERTICES[4], mCopySelectionMock.getMarker(0));
            assertNull(mCopySelectionMock.getMarker(1));
            assertNull(mCopySelectionMock.getMarker(2));
            assertNull(mCopySelectionMock.getMarker(3));
        }

        @Test
        public void testGetVertexAsString() {
            mCopySelectionMock.addMarker(new BlockPos(100, 200, 300));
            assertEquals("[x100,y200,z300]", mCopySelectionMock.getMarkerAsString(0));
        }

        @Test
        public void testGetVertexOutOfIndex() {
            assertNull(mCopySelectionMock.getMarker(999));
        }
    }

    @Nested
    class TestMaths {
        @Test
        public void testInvalidSelectionDiagonal() {
            mCopySelectionMock.addMarker(new BlockPos(0, 0, 0));
            mCopySelectionMock.addMarker(new BlockPos(1, 0, 0));
            mCopySelectionMock.addMarker(new BlockPos(0, 1, 1));
            mCopySelectionMock.addMarker(new BlockPos(0, 1, 0));
            int numStored = mCopySelectionMock.copyAndPrintBlocks(mLevelMock);
            verify(mLevelMock, times(0)).getBlockState(any());
            assertEquals(0, numStored);
        }

        @Test
        public void testInvalidSelectionSquare() {
            mCopySelectionMock.addMarker(new BlockPos(0, 0, 0));
            mCopySelectionMock.addMarker(new BlockPos(1, 0, 0));
            mCopySelectionMock.addMarker(new BlockPos(0, 1, 0));
            mCopySelectionMock.addMarker(new BlockPos(1, 1, 0));
            int numStored = mCopySelectionMock.copyAndPrintBlocks(mLevelMock);
            verify(mLevelMock, times(0)).getBlockState(any());
            assertEquals(0, numStored);
        }

        @Test
        public void testInvalidSelectionStraightLine() {
            mCopySelectionMock.addMarker(new BlockPos(0, 0, 1));
            mCopySelectionMock.addMarker(new BlockPos(0, 0, 2));
            mCopySelectionMock.addMarker(new BlockPos(0, 0, 3));
            mCopySelectionMock.addMarker(new BlockPos(0, 0, 4));
            int numStored = mCopySelectionMock.copyAndPrintBlocks(mLevelMock);
            verify(mLevelMock, times(0)).getBlockState(any());
            assertEquals(0, numStored);
        }

        @Test
        public void testInvalidSelectionNullValues() {
            mCopySelectionMock.addMarker(new BlockPos(0, 0, 0));
            mCopySelectionMock.addMarker(null);
            mCopySelectionMock.addMarker(null);
            mCopySelectionMock.addMarker(null);
            int numStored = mCopySelectionMock.copyAndPrintBlocks(mLevelMock);
            verify(mLevelMock, times(0)).getBlockState(any());
            assertEquals(0, numStored);
        }

        @Test
        public void testStoresCorrectNumberOfBlocksUnitCube() {
            mCopySelectionMock.addMarker(new BlockPos(0, 0, 0));
            mCopySelectionMock.addMarker(new BlockPos(1, 0, 0));
            mCopySelectionMock.addMarker(new BlockPos(0, 0, 1));
            mCopySelectionMock.addMarker(new BlockPos(0, 1, 0));
            int numStored = mCopySelectionMock.copyAndPrintBlocks(mLevelMock);
            verify(mLevelMock, times(8)).getBlockState(any());
            assertEquals(8, numStored);
        }

        @Test
        public void testStoresCorrectNumberOfBlocksNegativeUnitCube() {
            mCopySelectionMock.addMarker(new BlockPos(0, 0, 0));
            mCopySelectionMock.addMarker(new BlockPos(-1, 0, 0));
            mCopySelectionMock.addMarker(new BlockPos(0, 0, 1));
            mCopySelectionMock.addMarker(new BlockPos(0, -1, 0));
            int numStored = mCopySelectionMock.copyAndPrintBlocks(mLevelMock);
            verify(mLevelMock, times(8)).getBlockState(any());
            assertEquals(8, numStored);
        }

        @Test
        public void testStoresCorrectNumberOfBlocksCuboid() {
            // Remember +1 is added to the max of each so the marker itself is included
            mCopySelectionMock.addMarker(new BlockPos(0, 0, 0));
            mCopySelectionMock.addMarker(new BlockPos(-9, 0, 0));
            mCopySelectionMock.addMarker(new BlockPos(0, 0, 29));
            mCopySelectionMock.addMarker(new BlockPos(0, 19, 0));
            int numStored = mCopySelectionMock.copyAndPrintBlocks(mLevelMock);
            verify(mLevelMock, times(6000)).getBlockState(any());
            assertEquals(6000, numStored);
        }
    }

}
