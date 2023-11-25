package uk.ac.nottingham.hybridarcade;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
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
}
