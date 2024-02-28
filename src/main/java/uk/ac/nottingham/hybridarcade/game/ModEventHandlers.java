package uk.ac.nottingham.hybridarcade.game;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Handlers for Global Events using @SubscribeEvent.
 * @author Daniel Robinson 2024
 */
class ModEventHandlers {

    /**
     * Adds the Copy Item, Paste Item and Marker Block to the inventory.
     * @param event Handle for creative mode being opened by the player
     */
    @SubscribeEvent
    void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        // Add to the Items tab
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(Main.COPY_WAND);
            event.accept(Main.PASTE_WAND);
        }
        // Add to Functional Blocks tab
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(Main.MARKER_BLOCK_ITEM);
        }
    }
}
