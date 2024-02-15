package uk.ac.nottingham.hybridarcade.game;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class PasteSelection {
    private static final int REFRESH_BLOCKS_CODE = 3;

    private BlockState[][][] mBlocks;

    public void setBlocks(BlockState[][][] blocks){
        mBlocks = blocks;
    }

    // Paste blocks into the level if stored,
    // should be called on BOTH the renderer and server Thread.
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
