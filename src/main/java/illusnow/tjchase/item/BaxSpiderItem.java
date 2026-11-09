/*
 * Copyright 2026 IlluSnow
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package illusnow.tjchase.item;

import illusnow.tjchase.entity.projectile.BaxSpider;
import illusnow.tjchase.sound.ModSoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BaxSpiderItem extends TJChaseProjectileItem<BaxSpider> {
    private static final int COOLDOWN = 20;
    private static final float PROJECTILE_SHOOT_POWER = 0.75F;

    public BaxSpiderItem(Properties properties) {
        super(properties);
    }

    @Override
    protected BaxSpider createProjectileFromPos(Level level, double x, double y, double z, ItemStack stack) {
        return new BaxSpider(level, x, y, z);
    }

    @Override
    protected BaxSpider createProjectileFromOwner(ServerLevel level, LivingEntity owner, ItemStack stack) {
        return new BaxSpider(owner, level);
    }

    @Override
    protected void consume(ItemStack stack, Player player, InteractionHand hand, @Nullable BaxSpider baxSpider) {
        player.getCooldowns().addCooldown(stack, getCooldown());
        stack.hurtAndBreak(1, player, hand);
        player.awardStat(Stats.ITEM_USED.get(this));
        if (baxSpider != null) {
            baxSpider.setItemForm(stack.copy());
            baxSpider.setPreferredSlot(hand == InteractionHand.OFF_HAND ? Inventory.SLOT_OFFHAND : player.getInventory().getSelectedSlot());
            player.getInventory().removeItem(stack);
        }
    }

    @Override
    protected int getCooldown() {
        return COOLDOWN;
    }

    @Override
    protected float getProjectileShootPower() {
        return PROJECTILE_SHOOT_POWER;
    }

    @Nullable
    @Override
    protected SoundEvent getSound() {
        return ModSoundEvents.BAX_SPIDER_THROW.get();
    }
}
