package uk.ac.nottingham.hybridarcade.game;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Holds a 3 dimensional array of {@link BlockState BlockStates} and places them into the world
 * at a given position.
 * @author Daniel Robinson 2024
 */
public class PasteSelection {
    private static final int REFRESH_BLOCKS_CODE = 3;

    private BlockState[][][] mBlocks;

    /**
     * Set the block selection.
     * @param blocks 3D array of BlockStates.
     */
    public void setBlocks(BlockState[][][] blocks){
        mBlocks = blocks;
    }

    /**
     * Paste the blocks set by {@link PasteSelection#setBlocks(BlockState[][][]) setBlocks()}
     * into the game world at the given position.
     * This method should be called from <b>BOTH</b> the renderer and server Thread.
     * @param level The current level returned by {@link Player#level()}
     * @param startMarker The block position from where to paste the blocks from.
     * @return The number of blocks pasted. If there is an error
     * then 0 will be returned instead of throwing an error.
     */
    public int pasteBlocks(LevelAccessor level, BlockPos startMarker) {
        // Validation
        if (mBlocks == null){
            return 0;
        }

        // Place blocks
        for(int i = 0; i < mBlocks.length; i++){
            for (int j = 0; j < mBlocks[i].length; j++){
                for (int k = 0; k < mBlocks[i][j].length; k++){
                    BlockPos position = new BlockPos(
                        startMarker.getX() + i,
                        startMarker.getY() + j,
                        startMarker.getZ() + k);
                    level.setBlock(position, mBlocks[i][j][k], REFRESH_BLOCKS_CODE);
                }
            }
        }

        return mBlocks.length * mBlocks[0].length * mBlocks[0][0].length;
    }
}
