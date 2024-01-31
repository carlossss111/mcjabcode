package uk.ac.nottingham.hybridarcade.mechanics;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
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
    private final AreaSelection areaSelection;

    public MagicWand() {
        super(new Item.Properties());
        areaSelection = new AreaSelection();
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
            areaSelection.addVertex(pos);

            // Print all the verices as a chat message
            Utility.sendChat(String.format("%s, %s, %s, %s",
                    areaSelection.getVertexAsString(0),
                    areaSelection.getVertexAsString(1),
                    areaSelection.getVertexAsString(2),
                    areaSelection.getVertexAsString(3)));
        }
        return true;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if(player.level().isClientSide()){
            return InteractionResult.PASS;
        }

        int blocksStored = areaSelection.storeBlocks();
        Utility.sendChat("Blocks Stored: " + blocksStored);
        return InteractionResult.PASS;
    }
}
