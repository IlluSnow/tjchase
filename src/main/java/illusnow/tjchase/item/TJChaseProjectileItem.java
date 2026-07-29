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

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class TJChaseProjectileItem extends Item implements ProjectileItem {
    private static final float PROJECTILE_SHOOT_POWER = 1.5F;
    private static final float INACCURACY = 1;

    public TJChaseProjectileItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        SoundEvent sound = getSound();
        if (sound != null && !player.isSilent()) {
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    sound,
                    SoundSource.NEUTRAL,
                    0.5F,
                    1
            );
        }
        if (level instanceof ServerLevel serverlevel) {
            Projectile.spawnProjectileFromRotation(this::createProjectileFromOwner, serverlevel, stack, player, 0, getProjectileShootPower(), getInaccuracy());
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(stack, getCooldown());
        stack.consume(1, player);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack stack, Direction direction) {
        return createProjectileFromPos(level, position.x(), position.y(), position.z(), stack);
    }

    protected abstract Projectile createProjectileFromPos(Level level, double x, double y, double z, ItemStack stack);

    protected abstract Projectile createProjectileFromOwner(ServerLevel level, LivingEntity owner, ItemStack stack);

    protected abstract int getCooldown();

    protected float getProjectileShootPower() {
        return PROJECTILE_SHOOT_POWER;
    }

    protected float getInaccuracy() {
        return INACCURACY;
    }

    @Nullable
    protected abstract SoundEvent getSound();
}
