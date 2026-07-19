package illusnow.tjchase.entity.projectile;

import net.minecraft.world.entity.projectile.ProjectileDeflection;

public final class ModProjectileDeflections {
    public static final ProjectileDeflection YOGA_BALL_BOUNCE_FORWARD_X = (projectile, entity, random) -> {
        projectile.setDeltaMovement(projectile.getDeltaMovement().multiply(-1, 1, 1));
        ((YogaBall) projectile).updateRotationWhenBouncing();
        projectile.needsSync = true;
    };
    public static final ProjectileDeflection YOGA_BALL_BOUNCE_FORWARD_Y = (projectile, entity, random) -> {
        projectile.setDeltaMovement(projectile.getDeltaMovement().multiply(1, -1, 1));
        ((YogaBall) projectile).updateRotationWhenBouncing();
        projectile.needsSync = true;
    };
    public static final ProjectileDeflection YOGA_BALL_BOUNCE_FORWARD_Z = (projectile, entity, random) -> {
        projectile.setDeltaMovement(projectile.getDeltaMovement().multiply(1, 1, -1));
        ((YogaBall) projectile).updateRotationWhenBouncing();
        projectile.needsSync = true;
    };

    private ModProjectileDeflections() {}
}
