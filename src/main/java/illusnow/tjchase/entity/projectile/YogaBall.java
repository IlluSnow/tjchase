package illusnow.tjchase.entity.projectile;

import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.entity.TJChaseFriendlyMob;
import illusnow.tjchase.sound.ModSoundEvents;
import illusnow.tjchase.world.ModDamageSources;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.LoopType;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class YogaBall extends ThrowableProjectile implements Seekable, GeoEntity {
    public static final RawAnimation INFLATE = RawAnimation.begin().then("inflate", LoopType.HOLD_ON_LAST_FRAME);
    public static final int INFLATE_TICKS = 5;
    public static final int DEFLATE_TICKS = 10;
    public static final float INFLATE_SIZE = 2.5F;
    public static final float DEFAULT_DAMAGE = 10.5F;
    public static final double DAMAGE_RANGE = 1; // The additional range of the damage area when inflated.
    public static final double DEFAULT_INFLATE_PROBABILITY_INSIDE_RANGE = 1;
    private static final EntityDataAccessor<Integer> INFLATE_TIME = SynchedEntityData.defineId(YogaBall.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DEFLATE_TIME = SynchedEntityData.defineId(YogaBall.class, EntityDataSerializers.INT);
    private static final String INFLATE_TIME_TAG = "InflateTime";
    private static final String DEFLATE_TIME_TAG = "DeflateTime";
    private static final String DAMAGE_TAG = "Damage";
    private static final String AUTO_INFLATE_TAG = "AutoInflate";
    private static final String MAX_CHECK_RANGE_TAG = "MaxCheckRange";
    private static final String INFLATE_PROBABILITY_INSIDE_RANGE_TAG = "InflateProbabilityInsideRange";
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private float damage = 10.5F;
    private double maxCheckRange = DAMAGE_RANGE; // Higher value results in more inaccuracy. Value higher than 0.5 may cause the ball not to hit anything
    private double inflateProbabilityInsideRange = DEFAULT_INFLATE_PROBABILITY_INSIDE_RANGE;
    private boolean autoInflate = true;
    private static final Logger LOGGER = LogManager.getLogger();

    public YogaBall(EntityType<? extends YogaBall> type, Level level) {
        super(type, level);
    }

    public YogaBall(Entity owner, Level level) {
        super(ModEntities.YOGA_BALL.get(), owner.getX(), owner.getEyeY() - 0.1F, owner.getZ(), level);
        setOwner(owner);
        setRot(owner.getYRot(), owner.getXRot());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(INFLATE_TIME, -1);
        builder.define(DEFLATE_TIME, -1);
    }

    @Override
    public void syncPacketPositionCodec(double x, double y, double z) {
        super.syncPacketPositionCodec(x, y, z);
    }

    public int getInflateTime() {
        return entityData.get(INFLATE_TIME);
    }

    public void setInflateTime(int inflateTime) {
        entityData.set(INFLATE_TIME, inflateTime);
    }

    public int getDeflateTime() {
        return entityData.get(DEFLATE_TIME);
    }

    public void setDeflateTime(int deflateTime) {
        entityData.set(DEFLATE_TIME, deflateTime);
    }

    @Override
    public double getSeekPower() {
        return 0;
    }

    @Override
    public boolean deflect(ProjectileDeflection deflection, @Nullable Entity entity, @Nullable EntityReference<Entity> owner, boolean deflectionByPlayer) {
        deflection.deflect(this, entity, random);
        if (!level().isClientSide()) {
//          Owner unchanged
            onDeflection(deflectionByPlayer);
        }
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (isInflated()) {
            setInflateTime(getInflateTime() + 1);
        }
        if (getDeflateTime() != -1) {
            setDeflateTime(getDeflateTime() + 1);
            if (getDeflateTime() >= DEFLATE_TICKS) {
                discard();
                return;
            }
        }
        if (!level().isClientSide() && !isInflated() && random.nextDouble() < getInflateProbabilityInsideRange()) {
            AABB hurtBox = getHurtBox(DAMAGE_RANGE + random.nextDouble() * (getMaxCheckRange() - DAMAGE_RANGE));
            for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, hurtBox, EntitySelector.NO_SPECTATORS)) {
                if (canDamage(entity)) {
                    inflate();
                    break;
                }
            }
        }
        if (!canBounce() && getDeflateTime() == -1) {
            setDeflateTime(0);
        }
    }

    private boolean canDamage(LivingEntity entity) {
        Entity owner = getOwner();
        if (owner == null) {
            return true;
        }
        if (!(owner instanceof TJChaseFriendlyMob tjchaseMob)) {
            if (owner instanceof Mob mob) {
                if (mob.getTarget() == null) {
                    return mob.canAttack(entity);
                }
                return mob.getTarget() == entity || mob.getTarget().getType() == entity.getType();
            }
            return owner.canBeCollidedWith(entity);
        }
        return tjchaseMob.canActivelyAttack(entity);
    }

    @Override
    protected ProjectileDeflection hitTargetOrDeflectSelf(HitResult hitResult) {
        ProjectileDeflection projectileDeflection0 = super.hitTargetOrDeflectSelf(hitResult);
        if (projectileDeflection0 != ProjectileDeflection.NONE) {
            return projectileDeflection0;
        }
        if (canBounce() && hitResult instanceof BlockHitResult blockHitResult) {
            ProjectileDeflection deflection = getProjectileDeflection(blockHitResult);
            if (deflect(deflection, null, owner, false)) {
                setDeltaMovement(getDeltaMovement().scale(isInflated() ? 0.4 : 0.7));
                return deflection;
            }
        }
        return ProjectileDeflection.NONE;
    }

    private ProjectileDeflection getProjectileDeflection(BlockHitResult blockHitResult) {
        return switch (blockHitResult.getDirection().getAxis()) {
            case X -> ModProjectileDeflections.YOGA_BALL_BOUNCE_FORWARD_X;
            case Y -> ModProjectileDeflections.YOGA_BALL_BOUNCE_FORWARD_Y;
            case Z -> ModProjectileDeflections.YOGA_BALL_BOUNCE_FORWARD_Z;
        };
    }

    private boolean isInflated() {
        return getInflateTime() >= 0;
    }

    private boolean canBounce() {
        return getDeltaMovement().length() > 0.1;
    }

    public void inflate() {
        if (level().isClientSide()) {
            return;
        }
        setInflateTime(0);
        setDeltaMovement(getDeltaMovement().scale(0.5));
        AABB hurtBox = getHurtBox(DAMAGE_RANGE);
        boolean damaged = false;
        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, hurtBox, EntitySelector.NO_SPECTATORS)) {
            if (canDamage(entity)) {
                entity.hurtServer((ServerLevel) level(), ModDamageSources.yogaBall(this, getOwner()), getDamage());
                damaged = true;
            }
        }
        if (damaged) {
            playSound(ModSoundEvents.YOGA_BALL_HIT.get());
        }
    }

    private AABB getHurtBox(double additionalInflateValue) {
        double inflateValue = getBoundingBox().getYsize() * (INFLATE_SIZE - 1) / 2 + additionalInflateValue;
        return getBoundingBox().inflate(inflateValue);
    }

    @Override
    protected void updateRotation() {
        Vec3 deltaMovement = getDeltaMovement();
        setYRot(lerpRotation(yRotO, (float) (Mth.atan2(deltaMovement.x, deltaMovement.z) * 180 / Math.PI)));
        setXRot(0);
    }

    public void updateRotationWhenBouncing() {
        Vec3 deltaMovement = getDeltaMovement();
        setYRot((float) (Mth.atan2(deltaMovement.x, deltaMovement.z) * 180 / Math.PI));
        setOldRot();
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, velocity, inaccuracy);
        setXRot(0);
        setOldRot();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("inflate", state -> {
            if (isInflated()) {
                return state.setAndContinue(INFLATE);
            }
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(INFLATE_TIME_TAG, getInflateTime());
        output.putInt(DEFLATE_TIME_TAG, getDeflateTime());
        output.putFloat(DAMAGE_TAG, getDamage());
        output.putBoolean(AUTO_INFLATE_TAG, autoInflate);
        output.putDouble(MAX_CHECK_RANGE_TAG, getMaxCheckRange());
        output.putDouble(INFLATE_PROBABILITY_INSIDE_RANGE_TAG, getInflateProbabilityInsideRange());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setInflateTime(input.getIntOr(INFLATE_TIME_TAG, -1));
        setDeflateTime(input.getIntOr(DEFLATE_TIME_TAG, -1));
        setDamage(input.getFloatOr(DAMAGE_TAG, DEFAULT_DAMAGE));
        autoInflate = input.getBooleanOr(AUTO_INFLATE_TAG, true);
        setMaxCheckRange(input.getDoubleOr(MAX_CHECK_RANGE_TAG, DAMAGE_RANGE));
        setInflateProbabilityInsideRange(input.getDoubleOr(INFLATE_PROBABILITY_INSIDE_RANGE_TAG, DEFAULT_INFLATE_PROBABILITY_INSIDE_RANGE));
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public double getMaxCheckRange() {
        return maxCheckRange;
    }

    public void setMaxCheckRange(double maxCheckRange) {
        this.maxCheckRange = maxCheckRange;
    }

    public double getInflateProbabilityInsideRange() {
        return inflateProbabilityInsideRange;
    }

    public void setInflateProbabilityInsideRange(double inflateProbabilityInsideRange) {
        this.inflateProbabilityInsideRange = inflateProbabilityInsideRange;
    }
}
