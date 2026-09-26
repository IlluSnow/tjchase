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

package illusnow.tjchase.mixin;

import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.entity.HealthLockable;
import illusnow.tjchase.world.gameplay.WeakState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public abstract class PlayerMixin extends Avatar implements HealthLockable {
    private PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Unique
    @Override
    public float tjChase$getLockedHealth() {
        if (WeakState.weakInsteadOfDie((Player) (Object) this)) {
            return WeakState.WEAK_THRESHOLD_HEALTH;
        }
        return 0;
    }

    @Unique
    @Override
    public void tjChase$onHealthLockingHasEffect() {
        Player self = (Player) (Object) this;
        WeakState weak = getData(ModAttachments.WEAK_STATE);
        if (WeakState.weakInsteadOfDie(self) && !weak.isWeak()) {
            weak.setWeak(self, WeakState.DEFAULT_RECOVER_TICKS);
        }
    }
}
