package uk.ac.nottingham.hybridarcade;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;

public class CoreEventHandlers {
    final Logger log = Constants.logger;

    // Loaded on level load
    @SubscribeEvent
    public void levelLoads(LevelEvent.Load levelAccessor) {
        if (levelAccessor.getLevel().isClientSide()) {
            return;
        }

        LevelAccessor level = levelAccessor.getLevel();
        LinkedList<String> blockNames = new LinkedList<>();
        for(int x = 0; x < 30; x++) {
            for(int y = 0; y < 30; y++) {
                for(int z = 0; z < 30; z++) {
                    BlockState blockState = level.getBlockState(new BlockPos(x,y,z));
                    String name = blockState.getBlock().getDescriptionId();
                    blockNames.add(name);
                }
            }
        }
        log.info(blockNames);
    }
}
