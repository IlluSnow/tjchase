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
