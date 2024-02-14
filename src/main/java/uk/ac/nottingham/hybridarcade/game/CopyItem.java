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
import uk.ac.nottingham.hybridarcade.converter.BlockConverter;
import uk.ac.nottingham.hybridarcade.encoding.IEncoder;
import uk.ac.nottingham.hybridarcade.encoding.JabEncoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class CopyItem extends Item implements IForgeItem {
    private final CopySelection mCopySelection;
    private final BlockConverter mConverter;
    private final IEncoder mEncoder;

    CopyItem() {
        super(new Item.Properties());
        mCopySelection = new CopySelection();
        mConverter = BlockConverter.getInstance();
        mEncoder = new JabEncoder();
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
            mCopySelection.addMarker(pos);

            // Print all the vertices as a chat message
            Utility.sendChat(String.format("%s, %s, %s, %s",
                    mCopySelection.getMarkerAsString(0),
                    mCopySelection.getMarkerAsString(1),
                    mCopySelection.getMarkerAsString(2),
                    mCopySelection.getMarkerAsString(3)));
        }
        return true;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if(context.getLevel().isClientSide()){
            return InteractionResult.PASS;
        }

        new Thread(() -> {
            // Copy the blocks from the selection if possible
            int blocksStored = mCopySelection.copyBlocks(context.getLevel());
            if(blocksStored == 0) {
                Utility.sendChat("Failed to copy blocks!");
                Constants.logger.warn("0 blocks returned from " +
                        "CopySelection#copyBlocks(context.getLevel())");
                return;
            }

            // Store the blocks as a stream of bytes according to the mapping
            byte[] blocksAsBytestream = mConverter.toBytes(mCopySelection.getBlocks());

            // Get the blocks as a barcode PNG image
            BufferedImage barcodePNG;
            try {
                barcodePNG = mEncoder.encode(blocksAsBytestream);
            }
            catch(IOException e){
                Utility.sendChat("Failed to generate encoding!");
                Constants.logger.error("IEncoder#encode(bytes) threw an IOException\n" + e);
                return;
            }

            // Print out the barcodePNG TODO
            try {
                ImageIO.write(barcodePNG, "png", new File("mockoutput.png"));
            }
            catch(IOException e){
                Utility.sendChat("Failed to print out encoding!");
                Constants.logger.error("W.I.P.");
                return;
            }

            // Confirm
            Utility.sendChat("Blocks Copied: " + blocksStored);
        }).start();

        return InteractionResult.PASS;
    }
}
