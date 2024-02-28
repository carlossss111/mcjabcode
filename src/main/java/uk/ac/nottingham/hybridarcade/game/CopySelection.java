package uk.ac.nottingham.hybridarcade.game;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;

/**
 * Stores four {@link net.minecraft.core.Position Position} coordinates and uses
 * these to get all blocks between these coordinates. These blocks then stored
 * as a 3-dimensional array.
 * @author Daniel Robinson
 */
public class CopySelection {
    /** This should not be changed from 4 without changing the code that uses it. */
    private final static int NUM_OF_MARKERS = 4;

    private BlockState[][][] mBlocks;
    private final BlockPos[] mMarkers = new BlockPos[NUM_OF_MARKERS];

    /**
     * Adds the position of a marker to an array. If the array is full then
     * it is cleared and overwritten from the first position.
     * @param blockPosition Position of next marker
     */
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

    /**
     * Gets the marker at index.
     * @param index Index of marker
     * @return Marker position if it exists. This could be null.
     */
    @Nullable
    public BlockPos getMarker(int index){
        if (index < NUM_OF_MARKERS){
            return mMarkers[index];
        }
        return null;
    }

    /**
     * Gets the marker coordinates as a string for utility purposes.
     * @param index Index of marker
     * @return Either an empty string or coordinates in the format '[x%d,y%d,z%d]'
     */
    public String getMarkerAsString(int index){
        if(mMarkers[index] == null){
            return "";
        }
        return String.format("[x%d,y%d,z%d]",
                mMarkers[index].getX(), mMarkers[index].getY(), mMarkers[index].getZ());
    }

    /**
     * Calculates whether a line is orthogonal and returns the result.
     * @param from Position measuring from.
     * @param to Position measuring to.
     * @return True if the line between the two arguments is orthogonal. Otherwise returns False.
     */
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


    /**
     * Checks if a shape formed from all four coordinates set
     * in {@link CopySelection#addMarker(BlockPos)} is a cuboid.
     * This is true if an orthogonal line can be drawn from exactly one
     * vertex to all the other vertices.
     * @return True if the coordinates form a cuboid. Otherwise returns False.
     * @see CopySelection#isOrthogonalLine(BlockPos, BlockPos) 
    */
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

    public BlockState[][][] getBlocks(){
        return mBlocks;
    }

    /**
     * Initialises a 3D member array of {@link BlockState Blocks} and stores
     * the blocks between the coordinates added by {@link CopySelection#addMarker(BlockPos)}.
     * Only stores if the coordinates form a valid cuboid.
     * @param level The level returned from {@link Player#level()}
     * @return The number of blocks successfully pasted. No exceptions are return
     * on a failure, instead 0 is returned.
     * @see CopySelection#isValidCuboid() 
     */
    public int copyBlocks(LevelAccessor level){
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

        // Return volume
        return Math.abs(maxX - minX) * Math.abs(maxY - minY) * Math.abs(maxZ - minZ);
    }
}
