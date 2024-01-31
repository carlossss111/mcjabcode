package uk.ac.nottingham.hybridarcade;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraftforge.common.extensions.IForgeItem;
import org.apache.logging.log4j.Logger;

public class MagicWand extends SwordItem implements IForgeItem {
    final Logger log = Constants.logger;

    private final static int DMG_BEFORE_TIER_BONUS = 3;
    private final static float ATK_SPEED = -2.4F;

    public MagicWand() {
        super(Tiers.STONE, DMG_BEFORE_TIER_BONUS, ATK_SPEED,
                new Item.Properties());
    }

    // Called on making an attack while holding the item
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        log.info("Mob Type: " + entity.getType().getDescriptionId());
        return false;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        log.info("Block Position: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        return true;
    }
}
