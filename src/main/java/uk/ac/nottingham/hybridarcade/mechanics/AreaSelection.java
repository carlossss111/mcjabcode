package uk.ac.nottingham.hybridarcade.mechanics;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class AreaSelection {
    private final static int VERTICES_SIZE = 4;

    private List<BlockState> blocks;
    private final BlockPos[] vertices = new BlockPos[VERTICES_SIZE];

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
    private boolean isValidSelection() {
        int numberOfCenterVertices = 0;
        for (int i = 0; i < VERTICES_SIZE; i++) {
            int numberOfLines = 0;
            for (int j = 0; j < VERTICES_SIZE; j++) {
                if(isOrthogonalLine(vertices[i], vertices[j])){
                    numberOfLines++;
                }
            }
            if(numberOfLines == 3){
                numberOfCenterVertices++;
            }
        }
        return numberOfCenterVertices == 1;
    }

    // Stores the blocks *if valid*
    // Returns the number of blocks stored (hence 0 indicates the space was invalid)
    public int storeBlocks(){
        if(!isValidSelection()){
            return 0;
        }
        // TODO: store the blocks inside the vertices and return number stored
        return 1;
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

    public String getVertexAsString(int index){
        if(vertices[index] == null){
            return "";
        }
        return String.format("[x%d,y%d,z%d]",
                vertices[index].getX(), vertices[index].getY(), vertices[index].getZ());
    }
}
