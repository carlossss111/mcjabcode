package uk.ac.nottingham.hybridarcade.mechanics;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;

public class AreaSelection {
    private final static int NUM_OF_MARKERS = 4; //this should never be changed!

    private BlockState[][][] mBlocks;
    private final BlockPos[] mMarkers = new BlockPos[NUM_OF_MARKERS];

    // Adds the next vertex along, unless it is full then clears and tries again
    public void addMarker(BlockPos blockPosition) {
        for(int i = 0; i < NUM_OF_MARKERS; i++){
            if(mMarkers[i] == null){
                mMarkers[i] = blockPosition;
                return;
            }
        }
        mMarkers[0] = blockPosition;
        mMarkers[1] = mMarkers[2] = mMarkers[3] = null;
    }

    @Nullable
    public BlockPos getMarker(int index){
        if (index < 4){
            return mMarkers[index];
        }
        return null;
    }

    public String getMarkerAsString(int index){
        if(mMarkers[index] == null){
            return "";
        }
        return String.format("[x%d,y%d,z%d]",
                mMarkers[index].getX(), mMarkers[index].getY(), mMarkers[index].getZ());
    }

    // Returns true if an orthogonal line can be drawn between two vertices
    private boolean isOrthogonalLine(BlockPos from, BlockPos to) {
        if (from == null || to == null) {
            return false;
        }

        int positionChanges = 0;
        if(from.getX() != to.getX()) { positionChanges++; }
        if(from.getY() != to.getY()) { positionChanges++; }
        if(from.getZ() != to.getZ()) { positionChanges++; }
        return positionChanges == 1;
    }

    /*
        One vertex should be able to draw an orthogonal line to
        all other vertices (three lines).

           D
           |
           B    <-- 'B' can draw an orthogonal line to all, therefore this is a 3d cuboid.
         /  `
        A     `C

    */
    private boolean isValidCuboid() {
        int numberOfCentreMarkers = 0;

        // Compare all vertices, check if they are orthogonal
        // If there are three orthogonal lines, then it is a 'center vertex'
        // like vertex 'B' in the diagram above.
        for (int i = 0; i < NUM_OF_MARKERS; i++) {
            int numberOfLines = 0;
            for (int j = 0; j < NUM_OF_MARKERS; j++) {
                if(isOrthogonalLine(mMarkers[i], mMarkers[j])){
                    numberOfLines++;
                }
            }
            if(numberOfLines == 3){
                numberOfCentreMarkers++;
            }
        }

        // If there is exactly 1 centre vertex, it is a valid 3D cuboid
        return numberOfCentreMarkers == 1;
    }

    // Stores the blocks *if valid*
    // Returns the number of blocks stored (hence 0 indicates the space was invalid)
    public int storeAndPrintBlocks(LevelAccessor level){
        // Check validity and pick starting vertex
        if(!isValidCuboid()){
            return 0;
        }

        // Get all vertices
        int minX = Collections.min(Arrays.stream(mMarkers).map(BlockPos::getX).toList());
        int maxX = Collections.max(Arrays.stream(mMarkers).map(BlockPos::getX).toList()) + 1;
        int minY = Collections.min(Arrays.stream(mMarkers).map(BlockPos::getY).toList());
        int maxY = Collections.max(Arrays.stream(mMarkers).map(BlockPos::getY).toList()) + 1;
        int minZ = Collections.min(Arrays.stream(mMarkers).map(BlockPos::getZ).toList());
        int maxZ = Collections.max(Arrays.stream(mMarkers).map(BlockPos::getZ).toList()) + 1;

        // Initialize size of arrays
        mBlocks = new BlockState[Math.abs(maxX - minX)][Math.abs(maxY - minY)][
                Math.abs(maxZ - minZ)];

        // Loop through every block and add it in order
        int i = 0;
        for(int x = minX; x < maxX; x++) {
            int j = 0;
            for(int y = minY; y < maxY; y++) {
                int k = 0;
                for(int z = minZ; z < maxZ; z++){
                    BlockState blockState = level.getBlockState(new BlockPos(x,y,z));
                    mBlocks[i][j][k] = blockState;
                    k++;
                }
                j++;
            }
            i++;
        }

        // TODO: print here
        // printToCard();

        // Return volume
        return Math.abs(maxX - minX) * Math.abs(maxY - minY) * Math.abs(maxZ - minZ);
    }
}
