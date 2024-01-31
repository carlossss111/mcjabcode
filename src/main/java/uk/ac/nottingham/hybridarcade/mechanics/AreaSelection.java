package uk.ac.nottingham.hybridarcade.mechanics;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class AreaSelection {
    private final static int VERTICES_SIZE = 4;

    private List<BlockState> blocks;
    private final BlockPos[] vertices = new BlockPos[VERTICES_SIZE];

    public int storeBlocks(){
        // TODO: store the blocks inside the vertices and return number stored
        return 0;
    }

    // Adds the next vertex along, unless it is full then clears and tries again
    public void addVertex(BlockPos blockPosition) {
        for(int i = 0; i < VERTICES_SIZE; i++){
            if(vertices[i] == null){
                vertices[i] = blockPosition;
                return;
            }
        }
        vertices[0] = blockPosition;
        vertices[1] = vertices[2] = vertices[3] = null;
    }

    @Nullable
    public BlockPos getVertex(int index){
        if (index < 4){
            return vertices[index];
        }
        return null;
    }
}
