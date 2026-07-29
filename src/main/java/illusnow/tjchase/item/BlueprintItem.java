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

import illusnow.tjchase.entity.projectile.Blueprint;
import illusnow.tjchase.sound.ModSoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BlueprintItem extends TJChaseProjectileItem {
    public BlueprintItem(Properties properties) {
        super(properties);
    }

    @Override
    protected Projectile createProjectileFromPos(Level level, double x, double y, double z, ItemStack stack) {
        return new Blueprint(level, x, y, z, stack);
    }

    @Override
    protected Projectile createProjectileFromOwner(ServerLevel level, LivingEntity owner, ItemStack stack) {
        return new Blueprint(level, owner, stack);
    }

    @Override
    protected int getCooldown() {
        return 60;
    }

    @Nullable
    @Override
    protected SoundEvent getSound() {
        return ModSoundEvents.BLUEPRINT_THROW.get();
    }
}
