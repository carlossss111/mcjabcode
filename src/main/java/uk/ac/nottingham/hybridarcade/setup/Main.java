package uk.ac.nottingham.hybridarcade.setup;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import uk.ac.nottingham.hybridarcade.Constants;
import uk.ac.nottingham.hybridarcade.mechanics.MagicWand;

@Mod(Constants.MOD_ID)
public class Main {
    // Registries
    private static final DeferredRegister<Item> ITEMS
            = DeferredRegister
            .create(ForgeRegistries.ITEMS, Constants.MOD_ID);

    private static  final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS
            = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    private static final DeferredRegister<Block> BLOCKS
            = DeferredRegister
            .create(ForgeRegistries.BLOCKS, Constants.MOD_ID);

    // Blocks and Items
    static final RegistryObject<Block> MARKER_BLOCK
            = BLOCKS.register(Constants.MARKER_BLOCK_ID,
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));

    static final RegistryObject<Item> MARKER_BLOCK_ITEM
            = ITEMS.register(Constants.MARKER_BLOCK_ITEM_ID,
            () -> new BlockItem(MARKER_BLOCK.get(), new Item.Properties()));

    static final RegistryObject<Item> MAGIC_WAND
            = ITEMS.register(Constants.MAGIC_WAND_ID, MagicWand::new);

    // Called before anything else when the mod is loaded
    public Main() {
        final IEventBus modEventBus = FMLJavaModLoadingContext
                .get().getModEventBus();

        // Add registries to the mod event bus
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Add event handlers to mod the event
        modEventBus.register(new ModEventHandlers());
    }

}
