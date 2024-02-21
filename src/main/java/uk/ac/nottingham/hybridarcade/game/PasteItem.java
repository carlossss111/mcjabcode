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
import uk.ac.nottingham.hybridarcade.hardware.Scanner;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.http.HttpClient;

class PasteItem extends Item implements IForgeItem {
    private final PasteSelection mPasteSelection;
    private final BlockConverter mConverter;
    private final IEncoder mDecoder;
    private final Scanner mScanner;

    PasteItem() {
        super(new Properties());
        mPasteSelection = new PasteSelection();
        mConverter = BlockConverter.getInstance();
        mDecoder = new JabEncoder();
        mScanner = new Scanner(HttpClient.newHttpClient());
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        Level level = player.level();
        BlockState blockState = level.getBlockState(pos);
        Constants.logger.info("Block Position: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());

        if(blockState.getBlock().getDescriptionId()
                .equals(String.format("block.%s.%s", Constants.MOD_ID, Constants.MARKER_BLOCK_ID))) {

            // Paste blocks into the world
            int blocksPasted = mPasteSelection
                    .pasteBlocks(level, pos);
            if(!level.isClientSide()) {
                if(blocksPasted == 0){
                    Utility.sendChat("Failed to paste blocks!");
                    Constants.logger.warn("0 blocks returned from " +
                            "PasteSelection#pasteBlocks(...)");
                }
            }

        }
        return true;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if(context.getLevel().isClientSide()){
            return InteractionResult.PASS;
        }

        new Thread(() -> {
            Utility.sendChat("Scanning blocks...");

            // Scan PNG
            BufferedImage barcodePNG;
            try{
                barcodePNG = mScanner.scan();
            }
            catch(IOException e){
                Utility.sendChat("Failed to scan encoding!");
                Constants.logger.error("Scanner#scan() threw an IOException\n" + e);
                return;
            }

            // Read bytes from PNG
            byte[] blocksAsBytestream;
            try{
                blocksAsBytestream = mDecoder.decode(barcodePNG);
            }
            catch(IOException e){
                Utility.sendChat("Failed to decode encoding!");
                Constants.logger.error("IEncoder#decode(bytes) threw an IOException\n" + e);
                return;
            }

            // Convert bytes to blocktypes and save
            mPasteSelection.setBlocks(mConverter.toBlocks(blocksAsBytestream));

        }).start();

        Utility.sendChat("Finished!");
        return InteractionResult.PASS;
    }
}
