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

package illusnow.tjchase.util;

import illusnow.tjchase.attachment.ModAttachments;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public interface AngelTomPassive2Owner {
    static Optional<AngelTomPassive2Tracker> getPassive2Tracker(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return Optional.empty();
        }
        return Optional.of(entity.getData(ModAttachments.ANGEL_TOM_PASSIVE2_TRACKER));
    }

    static void mayUpdate(LivingEntity entity) {
        getPassive2Tracker(entity).ifPresent(tracker -> tracker.update(entity));
    }

    static void mayTriggerPassive2(LivingEntity entity) {
        getPassive2Tracker(entity).ifPresent(tracker -> tracker.triggerPassive2(entity));
    }

    static boolean hasBuff(LivingEntity entity) {
        return getPassive2Tracker(entity).map(tracker -> tracker.hasBuff(entity)).orElse(false);
    }

    static void resetAttributes(LivingEntity livingOwner, long interval, int healCount, float healAmount) {
        getPassive2Tracker(livingOwner).ifPresent(tracker -> tracker.resetAttributes(interval, healCount, healAmount));
    }
}
