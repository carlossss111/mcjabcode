package uk.ac.nottingham.hybridarcade.mechanics;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeItem;
import uk.ac.nottingham.hybridarcade.Constants;
import uk.ac.nottingham.hybridarcade.Utility;

public class MagicWand extends Item implements IForgeItem {
    private final AreaSelection mAreaSelection;

    public MagicWand() {
        super(new Item.Properties());
        mAreaSelection = new AreaSelection();
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        Level level = player.level();
        BlockState blockState = level.getBlockState(pos);
        if(level.isClientSide()){
            return true;
        }

        Constants.logger.info("Block Position: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());

        if(blockState.getBlock().getDescriptionId()
                .equals(String.format("block.%s.%s", Constants.MOD_ID, Constants.MARKER_BLOCK_ID))) {

            // Add a vertex
            mAreaSelection.addMarker(pos);

            // Print all the vertices as a chat message
            Utility.sendChat(String.format("%s, %s, %s, %s",
                    mAreaSelection.getMarkerAsString(0),
                    mAreaSelection.getMarkerAsString(1),
                    mAreaSelection.getMarkerAsString(2),
                    mAreaSelection.getMarkerAsString(3)));
        }
        return true;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if(context.getLevel().isClientSide()){
            return InteractionResult.PASS;
        }

        int blocksStored = mAreaSelection.storeAndPrintBlocks(context.getLevel());
        Utility.sendChat("Blocks Stored: " + blocksStored);
        return InteractionResult.PASS;
    }
}
