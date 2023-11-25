package uk.ac.nottingham.hybridarcade;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.Logger;

public class GlobalHandlers {
    final Logger log = Constants.logger;

    // Called on launch
    @SubscribeEvent
    public void commonSetup(final FMLCommonSetupEvent event) {
        log.info("Event handlers loaded.");
    }

    // Called on opening the 'Creative' menu
    @SubscribeEvent
    public void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        // Add to the Combat tab
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(Main.MAGIC_WAND);
        }
    }
}
