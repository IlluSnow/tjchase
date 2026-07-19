package illusnow.tjchase.entity.projectile;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public interface Seekable {
    private Projectile self() {
        return (Projectile) this;
    }

    double getSeekPower();

    default boolean canSeek() {
        return getSeekPower() > 0;
    }

    default void trySeek(Entity target) {
        Vec3 deltaMovement = self().getDeltaMovement();
        double dx = target.getX() - self().getX();
        double dy = target.getY(0.5) - self().getY(0.5);
        double dz = target.getZ() - self().getZ();
        double movementLen = deltaMovement.length();
        Vec3 vecToTarget = new Vec3(dx, dy, dz).normalize().scale(movementLen);
        double seekPower = Mth.clamp(getSeekPower() * movementLen / 2, 0, 1);
        Vec3 newDirection = deltaMovement.scale(1 - seekPower).add(vecToTarget.scale(seekPower));
        self().setDeltaMovement(newDirection.normalize().scale(movementLen));
    }
}
