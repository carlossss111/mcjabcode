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
import uk.ac.nottingham.hybridarcade.compression.BestCompressor;
import uk.ac.nottingham.hybridarcade.compression.ICompressor;
import uk.ac.nottingham.hybridarcade.converter.BlockConverter;
import uk.ac.nottingham.hybridarcade.encoding.IEncoder;
import uk.ac.nottingham.hybridarcade.encoding.JabEncoder;
import uk.ac.nottingham.hybridarcade.hardware.Printer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;

/**
 * Item that copies blocks from the world and stores them as an encoding.
 * When the player left-clicks on a Marker Block, it's position will be added to
 * {@link CopySelection} according it's constraints.
 * When the player right-clicks on any block, the blocks stored between the
 * {@link CopySelection} coordinates will be saved as an encoding.
 * <br/><br/>
 * Conceptually, CopyItem is a Controller in the Model-View-Controller pattern.
 * @see CopySelection
 * @see IEncoder
 * @see ICompressor
 * @see BlockConverter
 * @author Daniel Robinson 2024
 */
class CopyItem extends Item implements IForgeItem {
    private final static int ENCODING_ECC_LEVEL = 1;

    private final static boolean MOCK_PRINTER = true;

    private final CopySelection mCopySelection;
    private final BlockConverter mConverter;
    private final IEncoder mEncoder;
    private final ICompressor mCompressor;
    private final Printer mPrinter;

    /**
     * Constructor instantiates each part of the model.
     */
    CopyItem() {
        super(new Item.Properties());
        mCopySelection = new CopySelection();
        mConverter = BlockConverter.getInstance();
        mEncoder = new JabEncoder();
        mCompressor = new BestCompressor();
        mPrinter = new Printer(PrinterJob.getPrinterJob());
    }

    /**
     * Called by the Server Thread upon left-clicking while holding the item.
     * Adds the position of the block hit into {@link CopySelection}. The block
     * must be the Marker Block.
     * @param itemstack The current ItemStack
     * @param pos       Block's position in world
     * @param player    The Player that is wielding the item
     * @return Always True so that the block is not harvested.
     */
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

    /**
     * Called by the Server Thread upon right-clicking while holding the item.
     * Tries each part of the MVC Model sequentially in a newly spawned Thread: <br/>
     * Copies the blocks between the {@link CopySelection} coordinates; converts
     * them into bytes using a map; compresses them; generates an encoding; and
     * saves it to disk.<br/>
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
            // Copy the blocks from the selection if possible
            int blocksStored = mCopySelection.copyBlocks(context.getLevel());
            if(blocksStored == 0) {
                Utility.sendChat("Failed to copy blocks!");
                Constants.logger.warn("0 blocks returned from " +
                        "CopySelection#copyBlocks(context.getLevel())");
                return;
            }

            // Store the blocks as a stream of bytes according to the mapping
            byte[] convertedBytes = mConverter.toBytes(mCopySelection.getBlocks());

            // Compress bytestream
            byte[] compressedBytes = mCompressor.compress(convertedBytes);

            // Get the blocks as a barcode PNG image
            BufferedImage barcodePNG;
            try {
                barcodePNG = mEncoder.encode(compressedBytes);
            }
            catch(IOException e){
                Utility.sendChat("Failed to generate encoding!");
                Constants.logger.error("IEncoder#encode(bytes) threw an IOException\n" + e);
                return;
            }

            // Print out the barcode in a given dimension
            try {
                if(MOCK_PRINTER){
                    ImageIO.write(barcodePNG,"png",new File("mock_barcode.png"));
                }
                else {
                    mPrinter.print(barcodePNG);
                    Utility.sendChat("Sent to printer!");
                }
            }
            catch(PrinterException e){
                Utility.sendChat("Failed to interact with printer, saved to file instead.");
                Constants.logger.error("Printer#print threw a PrinterException\n" + e);
            }
            catch(IOException | IllegalArgumentException e){
                Utility.sendChat("Failed to print out encoding!");
                Constants.logger.error("Printer#print threw an Exception\n" + e);
                return;
            }

            // Confirm
            Utility.sendChat("Bytes Stored: " + compressedBytes.length);
            Utility.sendChat("Blocks Copied: " + blocksStored);
        }).start();

        return InteractionResult.PASS;
    }
}
