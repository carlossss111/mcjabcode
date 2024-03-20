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
import uk.ac.nottingham.hybridarcade.compression.*;
import uk.ac.nottingham.hybridarcade.converter.BlockConverter;
import uk.ac.nottingham.hybridarcade.encoding.IEncoder;
import uk.ac.nottingham.hybridarcade.encoding.JabEncoder;
import uk.ac.nottingham.hybridarcade.hardware.Scanner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;

/**
 * Item that scans blocks and pastes them into the world.
 * When the player right-clicks on any block, an encoding will be read and saved in
 * {@link PasteSelection}.
 * When the player left-clicks on a Marker Block, then the blocks stored in {@link PasteSelection}
 * will be pasted into the world at the position of the marker.
 * <br/><br/>
 * Conceptually, PasteItem is a Controller in the Model-View-Controller pattern.
 * @see PasteSelection
 * @see Scanner
 * @see IEncoder
 * @see ICompressor
 * @see BlockConverter
 * @author Daniel Robinson 2024
 */
class PasteItem extends Item implements IForgeItem {

    private static final boolean MOCK_SCANNER = true;

    private final PasteSelection mPasteSelection;
    private final BlockConverter mConverter;
    private final IEncoder mDecoder;
    private final ICompressor mDecompressor;
    private final Scanner mScanner;

    /**
     * Constructor instantiates a copy of each part of the Model.
     */
    PasteItem() {
        super(new Properties());
        mPasteSelection = new PasteSelection();
        mConverter = BlockConverter.getInstance();
        mDecoder = new JabEncoder();
        mDecompressor = new BestCompressor();
        mScanner = new Scanner(HttpClient.newHttpClient());
    }

    /**
     * Called twice by the Renderer Thread and Server Thread upon left-clicking
     * while holding the item. Pastes the blocks stored in {@link PasteSelection} into
     * the given position. The block left-clicked on must be the Marker Block.
     * @param itemstack The current ItemStack
     * @param pos       Block's position in world
     * @param player    The Player that is wielding the item
     * @return Always True to prevent block being harvested
     */
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
                else{
                    Utility.sendChat("Blocks pasted: " + blocksPasted);
                }
            }

        }
        return true;
    }

    /**
     * Called on the Server Thread by right-clicking any block while holding the Paste Item.
     * Tries each part of the MVC Model sequentially in a newly spawned thread:<br/>
     * Scans the barcode; decodes it into bytes; decompresses it; converts it
     * to {@link BlockState BlockStates} using a map; and spawns it into the game view.<br/>
     * Any errors thrown by the model are caught here and an error message is returned to the
     * player and the logger so that the game can continue without crashing.
     * @param stack The current ItemStack
     * @param context The Item's Context
     * @return Always True so that interaction did not hold errors
     */
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
                if (MOCK_SCANNER) {
                    barcodePNG = ImageIO.read(new File("mock_barcode.png"));
                }
                else {
                    barcodePNG = mScanner.scan();
                }
            }
            catch(IOException e){
                Utility.sendChat("Failed to scan encoding!");
                Constants.logger.error("Scanner#scan() threw an IOException\n" + e);
                return;
            }

            // Read bytes from PNG
            byte[] decodedBytes;
            try{
                decodedBytes = mDecoder.decode(barcodePNG);
            }
            catch(IOException e){
                Utility.sendChat("Failed to decode encoding!");
                Constants.logger.error("IEncoder#decode(bytes) threw an IOException\n" + e);
                return;
            }

            // Decompress bytes
            byte[] rawBytes;
            try{
                rawBytes = mDecompressor.decompress(decodedBytes);
            }
            catch(IllegalArgumentException e){
                Utility.sendChat("Failed to decompress encoding!");
                Constants.logger.error("ICompressor#decompress(bytes) threw an IllegalArgumentException\n" + e);
                return;
            }

            // Convert bytes to blocktypes and paste
            mPasteSelection.setBlocks(mConverter.toBlocks(rawBytes));

            Utility.sendChat("Finished!");
        }).start();

        return InteractionResult.PASS;
    }
}
