package uk.ac.nottingham.hybridarcade;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(Constants.MOD_ID)
public class Main {
    // Registries
    public static final DeferredRegister<Item> ITEMS
            = DeferredRegister
            .create(ForgeRegistries.ITEMS, Constants.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS
            = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    // Registered Items
    public static final RegistryObject<Item> MAGIC_WAND
            = ITEMS.register("magic_wand", MagicWand::new);

    // Called before anything else when the mod is loaded
    public Main() {
        final IEventBus modEventBus = FMLJavaModLoadingContext
                .get().getModEventBus();

        // Add registries to the mod event bus
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Add this mod class to the general event bus
        MinecraftForge.EVENT_BUS.register(this);

        // Add event handlers to mod the event
        modEventBus.register(new GlobalHandlers());
    }

}
