package uk.ac.nottingham.hybridarcade.game;

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

class PasteItem extends Item implements IForgeItem {
    private final PasteSelection mPasteSelection;

    PasteItem() {
        super(new Properties());
        mPasteSelection = new PasteSelection();
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        Level level = player.level();
        BlockState blockState = level.getBlockState(pos);
        Constants.logger.info("Block Position: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());

        if(blockState.getBlock().getDescriptionId()
                .equals(String.format("block.%s.%s", Constants.MOD_ID, Constants.MARKER_BLOCK_ID))) {

            // Paste blocks stored in the paste selection
            int blocksPasted = mPasteSelection.pasteBlocks(level, pos);
            if(!level.isClientSide()) {
                Utility.sendChat("Blocks Pasted: " + blocksPasted);
            }

        }
        return true;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if(context.getLevel().isClientSide()){
            return InteractionResult.PASS;
        }

        // Scan blocks from scanner
        Utility.sendChat("Scanning blocks...");
        if(mPasteSelection.scanAndStoreBlocks()){
            Utility.sendChat("Finished scanning and ready to paste.");
        }
        else{
            Utility.sendChat("Failed to scan! Try again.");
        }

        return InteractionResult.PASS;
    }
}
