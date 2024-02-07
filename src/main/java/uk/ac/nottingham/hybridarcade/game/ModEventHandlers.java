package uk.ac.nottingham.hybridarcade.game;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.Logger;
import uk.ac.nottingham.hybridarcade.Constants;

class ModEventHandlers {
    final Logger log = Constants.logger;

    // Called on launch
    @SubscribeEvent
    void commonSetup(final FMLCommonSetupEvent event) {
        log.info("Event handlers loaded.");
    }

    // Called on opening the 'Creative' menu
    @SubscribeEvent
    void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        // Add to the Combat tab
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(Main.COPY_WAND);
            event.accept(Main.PASTE_WAND);
            event.accept(Main.MARKER_BLOCK_ITEM);
        }
    }
}
