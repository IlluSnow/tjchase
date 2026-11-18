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

import illusnow.tjchase.entity.projectile.AbstractRascal;
import illusnow.tjchase.sound.ModSoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class RascalItem<T extends AbstractRascal> extends TJChaseProjectileItem<T> {
    private static final int COOLDOWN = 20;
    private final RascalFactoryPos<? extends T> factoryPos;
    private final RascalFactoryOwner<? extends T> factoryOwner;
    private final float shootPower;

    public RascalItem(RascalFactoryPos<? extends T> factoryPos, RascalFactoryOwner<? extends T> factoryOwner, float shootPower, Properties properties) {
        super(properties);
        this.factoryPos = factoryPos;
        this.factoryOwner = factoryOwner;
        this.shootPower = shootPower;
    }

    @Override
    protected T createProjectileFromPos(Level level, double x, double y, double z, ItemStack stack) {
        return factoryPos.createProjectileFromPos(level, x, y, z, stack);
    }

    @Override
    protected T createProjectileFromOwner(ServerLevel level, LivingEntity owner, ItemStack stack) {
        return factoryOwner.createProjectileFromOwner(level, owner, stack);
    }

    @Override
    protected float getProjectileShootPower() {
        return shootPower;
    }

    @Override
    protected int getCooldown() {
        return COOLDOWN;
    }

    @Nullable
    @Override
    protected SoundEvent getSound() {
        return ModSoundEvents.RASCAL_THROW.get();
    }

    @Override
    public DispenseConfig createDispenseConfig() {
        return ProjectileItem.DispenseConfig.builder()
                .uncertainty(ProjectileItem.DispenseConfig.DEFAULT.uncertainty() * 0.75F)
                .power(getProjectileShootPower() * 1.25F)
                .build();
    }

    @FunctionalInterface
    public interface RascalFactoryPos<T extends AbstractRascal> {
        T createProjectileFromPos(Level level, double x, double y, double z, ItemStack stack);
    }

    @FunctionalInterface
    public interface RascalFactoryOwner<T extends AbstractRascal> {
        T createProjectileFromOwner(ServerLevel level, LivingEntity owner, ItemStack stack);
    }
}
