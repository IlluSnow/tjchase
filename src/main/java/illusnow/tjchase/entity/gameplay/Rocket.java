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

package illusnow.tjchase.entity.gameplay;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.entity.ModEntityDataSerializers;
import illusnow.tjchase.entity.ModEntityNames;
import illusnow.tjchase.item.ModItems;
import illusnow.tjchase.network.s2c.PlayFuseSoundPayload;
import illusnow.tjchase.sound.ModSoundEvents;
import illusnow.tjchase.util.Freezable;
import illusnow.tjchase.world.gameplay.action.ActionHolder;
import illusnow.tjchase.world.gameplay.action.ModActions;
import illusnow.tjchase.world.gameplay.object.GameplayObjectType;
import illusnow.tjchase.world.gameplay.object.ModGameplayObjectTypes;
import illusnow.tjchase.world.gameplay.object.editablevalue.ModEditableValues;
import illusnow.tjchase.world.gameplay.struggle.StruggleInstance;
import illusnow.tjchase.world.gameplay.struggle.StruggleTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.TranslatableEnum;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;

public class Rocket extends InGamePlacedEntity<Rocket> implements GeoEntity, Freezable {
    private static final String HIT_CONTROLLER_NAME = "Hit";
    private static final String HIT_N_NAME = "hitN";
    private static final String HIT_S_NAME = "hitS";
    private static final String HIT_E_NAME = "hitE";
    private static final String HIT_W_NAME = "hitW";
    public static final RawAnimation EMPTY = RawAnimation.begin().thenPlayAndHold("empty");
    public static final RawAnimation TIED = RawAnimation.begin().thenPlayAndHold("tied");
    public static final RawAnimation HIT_N = RawAnimation.begin().thenPlay(HIT_N_NAME);
    public static final RawAnimation HIT_S = RawAnimation.begin().thenPlay(HIT_S_NAME);
    public static final RawAnimation HIT_E = RawAnimation.begin().thenPlay(HIT_E_NAME);
    public static final RawAnimation HIT_W = RawAnimation.begin().thenPlay(HIT_W_NAME);
    public static final RawAnimation FLY = RawAnimation.begin().thenPlayAndHold("fly");
    private static final EntityDataAccessor<Integer> DATA_FUSE = SynchedEntityData.defineId(Rocket.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_DEFAULT_FUSE_SECONDS = SynchedEntityData.defineId(Rocket.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FLY_TICKS = SynchedEntityData.defineId(Rocket.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_PRIMED = SynchedEntityData.defineId(Rocket.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_FLYING = SynchedEntityData.defineId(Rocket.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_FROZEN = SynchedEntityData.defineId(Rocket.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<FuseDisplayDirection> DATA_FUSE_DISPLAY_DIRECTION = SynchedEntityData.defineId(Rocket.class, ModEntityDataSerializers.FUSE_DISPLAY_DIRECTION.get());
    private static final EntityDataAccessor<Double> DATA_FUSE_DISPLAY_DISTANCE = SynchedEntityData.defineId(Rocket.class, ModEntityDataSerializers.DOUBLE.get());
    private static final EntityDataAccessor<Double> DATA_FUSE_DISPLAY_HEIGHT_OFFSET = SynchedEntityData.defineId(Rocket.class, ModEntityDataSerializers.DOUBLE.get());
    private static final EntityDataAccessor<Double> DATA_FUSE_DISPLAY_FONT_SCALE = SynchedEntityData.defineId(Rocket.class, ModEntityDataSerializers.DOUBLE.get());
    private static final int FUSE_DECREASE_PER_TICK = 100;
    public static final int DEFAULT_FUSE_SECONDS = 60;
    public static final int MAX_FLY_TICKS = 140;
    public static final int FLY_EXPLOSION_TICKS = 120;
    private static final int DEFAULT_FUSE = DEFAULT_FUSE_SECONDS * 20 * FUSE_DECREASE_PER_TICK;
    public static final int DEFAULT_TIE_DURATION = 35;
    public static final int DEFAULT_RESCUE_DURATION = 20;
    private static final String FUSE_TAG = "Fuse";
    private static final String PRIMED_TAG = "Primed";
    private static final String FLYING_TAG = "Flying";
    private static final String INITIAL_Y_ROT_TAG = "InitialYRot";
    private static final String RESCUE_TICKS_TAG = "RescueTicks";
    private static final String FROZEN_TAG = "Frozen";
    private static final String DISCONNECTED_PLAYER_TAG = "DisconnectedPlayer";
    private static final String STRUGGLE_COOLDOWN_TAG = "StruggleCooldown";
    private static final double FUSE_SOUND_PLAY_RADIUS = 16;
    private static final int MAX_STRUGGLE_COOLDOWN = 20 * 20;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    @Nullable
    private NameAndId disconnectedPlayerOnRocket;
    private double baseBurningSpeed = 1;
    private float initialYRot;
    private int instaburnSeconds;
    private int rescueTicks;
    private int struggleCooldown;
    private boolean occupied;

    public Rocket(EntityType<? extends Rocket> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FUSE, DEFAULT_FUSE);
        builder.define(DATA_DEFAULT_FUSE_SECONDS, DEFAULT_FUSE_SECONDS);
        builder.define(DATA_PRIMED, false);
        builder.define(DATA_FLYING, false);
        builder.define(DATA_FROZEN, false);
        builder.define(DATA_FLY_TICKS, 0);
        builder.define(DATA_FUSE_DISPLAY_DIRECTION, ModEditableValues.FUSE_DISPLAY_DIRECTION.get().defaultValue());
        builder.define(DATA_FUSE_DISPLAY_DISTANCE, ModEditableValues.FUSE_DISPLAY_DISTANCE.get().defaultValue());
        builder.define(DATA_FUSE_DISPLAY_HEIGHT_OFFSET, ModEditableValues.FUSE_DISPLAY_HEIGHT_OFFSET.get().defaultValue());
        builder.define(DATA_FUSE_DISPLAY_FONT_SCALE, ModEditableValues.FUSE_DISPLAY_FONT_SCALE.get().defaultValue());
    }

    @Override
    public void tick() {
        if (isFlying()) {
            if (!level().isClientSide()) {
                yRotO = getYRot();
                setYRot(calculateYRot(getFlyTicks()));
                needsSync = true;
                setFlyTicks(getFlyTicks() + 1);
                float flyTicks = getFlyTicks();
                if (flyTicks >= MAX_FLY_TICKS){
                    discard();
                } else if (flyTicks >= FLY_EXPLOSION_TICKS) {
                    Player tying = TyingHelper.getTying(this);
                    if (tying != null) {
                        TyingHelper.clearStruggleOrPrayAction(tying);
                    }
                    TyingHelper.tie(null, this);
                }
            } else {
                float flyTicks = getFlyTicks();
                if (flyTicks >= FLY_EXPLOSION_TICKS && flyTicks % 4 == 0) {
                    level().addParticle(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(), getZ(), 0, 0, 0);
                }
            }
        }
        if (!level().isClientSide()) {
            if (!isFlying()) {
                if (isPrimed()) {
                    int decreaseAmount = calculateDecreaseAmountPerTick();
                    if (rescueTicks <= 0) {
                        decreaseFuse(decreaseAmount);
                    } else {
                        rescueTicks--;
                    }
                    if (getFuse() == 0 && canFly()) {
                        setFlying(true);
                    }
                }
                Player tying = TyingHelper.getTying(this);
                if (tying != null && !isFrozen()) {
                    if (struggleCooldown == 0) {
                        StruggleInstance.setStruggle(tying, StruggleInstance.createNew(getRandom().nextBoolean() ? StruggleTypes.ROCKET_5.get() : StruggleTypes.ROCKET_10.get()));
                        struggleCooldown = MAX_STRUGGLE_COOLDOWN;
                    } else if (StruggleInstance.getStruggle(tying) == null) {
                        struggleCooldown--;
                    }
                }
            }
        } else {
            if (isFlying()) {
                spawnFuseParticles(level(), position().add(random.nextGaussian() * 0.15, 0.5, random.nextGaussian() * 0.15), random,
                        0.7F,
                        5,
                        0.7F,
                        0.15F,
                                    0.2,
                                    0.3,
                        0.06);
            } else if (isPrimed()) {
                spawnFuseParticles(level(), position().add(random.nextGaussian() * 0.15, 0.5, random.nextGaussian() * 0.15), random,
                        0.1F,
                        0.7F,
                        0.5F,
                        0,
                        0.02,
                        0.1,
                        0.2
                );
            }
        }
        super.tick();
    }

    public int getFlyTicks() {
        return entityData.get(DATA_FLY_TICKS);
    }

    public void setFlyTicks(int flyTicks) {
        entityData.set(DATA_FLY_TICKS, flyTicks);
    }

    private void spawnFuseParticles(Level level, Vec3 fusePos, RandomSource random,
                                    float flameProbability,
                                    float smokeProbability,
                                    float enlargeSmokeProbability,
                                    float lavaProbability,
                                    double flameSpeed,
                                    double smokeSpeed,
                                    double smokeSpacing) {
        double vx = (random.nextDouble() - 0.5) * 0.04;
        double vy = -random.nextDouble() * 0.05 - flameSpeed;
        double vz = (random.nextDouble() - 0.5) * 0.04;
        if (random.nextFloat() < flameProbability) {
            level.addParticle(ParticleTypes.SMALL_FLAME, fusePos.x, fusePos.y, fusePos.z, vx, vy, vz);
        }

        for (int i = 0; i < smokeProbability - 1; i++) {
            level.addParticle(random.nextFloat() < enlargeSmokeProbability ? ParticleTypes.SMOKE : ParticleTypes.LARGE_SMOKE,
                    fusePos.x + (random.nextDouble() - 0.5) * smokeSpacing,
                    fusePos.y,
                    fusePos.z + (random.nextDouble() - 0.5) * smokeSpacing,
                    random.nextDouble() * 0.2 - 0.1, -smokeSpeed, random.nextDouble() * 0.2 - 0.1
            );
        }
        if (random.nextFloat() < smokeProbability - (int) smokeProbability) {
            level.addParticle(random.nextFloat() < enlargeSmokeProbability ? ParticleTypes.SMOKE : ParticleTypes.LARGE_SMOKE,
                    fusePos.x + (random.nextDouble() - 0.5) * 0.02,
                    fusePos.y,
                    fusePos.z + (random.nextDouble() - 0.5) * 0.02,
                    random.nextDouble() * 0.2 - 0.1, -smokeSpeed, random.nextDouble() * 0.2 - 0.1
            );
        }

        if (random.nextFloat() < lavaProbability) {
            level.addParticle(ParticleTypes.LAVA, fusePos.x, fusePos.y - 0.2, fusePos.z, 0, 0, 0);
        }
    }

    @Override
    protected InteractionResult createInteractionResult(Player player, InteractionHand hand) {
        if (!player.level().isClientSide() && player.getItemInHand(hand).is(Items.FLINT_AND_STEEL) && !isPrimed()) {
            prime();
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.createInteractionResult(player, hand);
    }

    public boolean tryTiePlayerToSelf(Player player, boolean allowOccupied) {
        if (canTiePlayerToSelf(allowOccupied)) {
            if (!level().isClientSide()) {
                TyingHelper.tie(player, this);
                ActionHolder.setAction(player, ModActions.STRUGGLE.get());
            }
            return true;
        }
        return false;
    }

    public boolean canTiePlayerToSelf(boolean allowOccupied) {
        return !isFlying() && !isFrozen() && (allowOccupied || !isOccupied()) && TyingHelper.getTying(this) == null;
    }

    public void prime() {
        setPrimed(true);
        playFuseSound();
        decreaseFuseBySeconds(getInstaburnSeconds());
    }

    public void extinguish() {
        setPrimed(false);
    }

    private boolean canFly() {
        return rescueTicks <= 0 && !isFrozen();
    }

    private boolean canRescue() {
        return getFuse() > 0 && !isFrozen();
    }

    private int calculateDecreaseAmountPerTick() {
        return (int) Math.round(FUSE_DECREASE_PER_TICK * baseBurningSpeed);
    }

    @Override
    public boolean validItem(ItemStack stack) {
        return stack.is(ModItems.ROCKET_EDITOR);
    }

    @Override
    public GameplayObjectType<Rocket> getGameplayObjectType() {
        return ModGameplayObjectTypes.ROCKET.get();
    }

    @Override
    public boolean canBeCollidedWith(@Nullable Entity entity) {
        return entity != null && entity.isAlive() && !isFlying();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        if (isFlying()) {
            return false;
        }
        if (super.hurtServer(level, damageSource, amount)) {
            if (damageSource.getDirectEntity() != null && TyingHelper.getTying(this) == null) {
                playHitAnimation(damageSource.getDirectEntity());
            }
            return true;
        }
        return false;
    }

    @Override
    public void prepareCleaning(ItemStack stack, ServerPlayer player, ServerLevel level) {
        if (isFlying() || isVehicle()) {
            return;
        }
        super.prepareCleaning(stack, player, level);
    }

    private void playHitAnimation(Entity attacker) {
        Vec3 attackerLookAngle = attacker instanceof Projectile ? attacker.getDeltaMovement().normalize() : attacker.getLookAngle();
        if (attackerLookAngle.lengthSqr() <= 1e-4 || Math.abs(attackerLookAngle.dot(Direction.UP.getUnitVec3())) > 0.99) {
            return;
        }
        Vec3 toPlayerVec2 = new Vec3(-attackerLookAngle.x, 0, -attackerLookAngle.z);
        Vec3 lookAngle = getLookAngle();
        Vec3 lookingVec2 = new Vec3(lookAngle.x, 0, lookAngle.z);
        double dotProduct = lookingVec2.dot(toPlayerVec2);
        double angle = Math.acos(dotProduct / toPlayerVec2.length() / lookingVec2.length()) * 180 / Math.PI;
        Vec3 crossProduct = lookingVec2.cross(toPlayerVec2);
        if (crossProduct.y > 0) {
            angle = 360 - angle;
        }
        if (angle < 45 || angle >= 315) {
            triggerAnim(HIT_CONTROLLER_NAME, HIT_N_NAME);
        } else if (angle < 135) {
            triggerAnim(HIT_CONTROLLER_NAME, HIT_E_NAME);
        } else if (angle < 225) {
            triggerAnim(HIT_CONTROLLER_NAME, HIT_S_NAME);
        } else {
            triggerAnim(HIT_CONTROLLER_NAME, HIT_W_NAME);
        }
    }

    @Override
    public void onPlaced() {
        setFuseSeconds(getDefaultFuseSeconds());
    }

    public int getFuse() {
        return entityData.get(DATA_FUSE);
    }

    public int getFuseSeconds() {
        double fuse = getFuse();
        return Mth.ceil(fuse / FUSE_DECREASE_PER_TICK / 20);
    }

    public int getDefaultFuseSeconds() {
        return entityData.get(DATA_DEFAULT_FUSE_SECONDS);
    }

    public void setFuse(int fuse) {
        entityData.set(DATA_FUSE, Math.max(0, fuse));
    }

    public void setFuseSeconds(int fuseSeconds) {
        setFuse(fuseSeconds * FUSE_DECREASE_PER_TICK * 20);
    }

    public void setDefaultFuseSeconds(int defaultFuseSeconds) {
        entityData.set(DATA_DEFAULT_FUSE_SECONDS, defaultFuseSeconds);
    }

    public void decreaseFuse(int amount) {
        setFuse(getFuse() - amount);
    }

    public void decreaseFuseBySeconds(int seconds) {
        decreaseFuse(seconds * FUSE_DECREASE_PER_TICK * 20);
    }

    public void increaseFuseBySeconds(int seconds) {
        decreaseFuse(-seconds * FUSE_DECREASE_PER_TICK * 20);
    }

    public boolean isPrimed() {
        return entityData.get(DATA_PRIMED);
    }

    public void setPrimed(boolean primed) {
        entityData.set(DATA_PRIMED, primed);
    }

    public boolean shouldPlayFuseSound() {
        return isAlive() && isPrimed() && !isFlying();
    }

    public double getBaseBurningSpeed() {
        return baseBurningSpeed;
    }

    public void setBaseBurningSpeed(double baseBurningSpeed) {
        this.baseBurningSpeed = baseBurningSpeed;
    }

    public boolean isFlying() {
        return entityData.get(DATA_FLYING);
    }

    public void setFlying(boolean flying) {
        entityData.set(DATA_FLYING, flying);
        if (flying) {
            initialYRot = getYRot();
            playSound(ModSoundEvents.ROCKET_LAUNCH.get(), 1, 1);
            Player tying = TyingHelper.getTying(this);
            if (tying != null) {
                ActionHolder.setAction(tying, ModActions.PRAY.get());
            }
        }
    }

    @Override
    protected double getDefaultGravity() {
        if (isFlying()) {
            return -0.02;
        }
        return super.getDefaultGravity();
    }

    public float calculateYRot(int ticks) {
        float seconds = ticks / 20F;
        float yRot;
        float yRot1 = -720;
        float yRot2 = -1800;
        if (seconds <= 0) {
            yRot = 0;
        } else if (seconds <= 2) {
            float timeNormalized = seconds / 2F;
            float eased = timeNormalized * timeNormalized;
            yRot = yRot1 * eased;
        } else if (seconds <= 7) {
            float timeNormalized = (seconds - 2F) / 5F;
            float eased = 2 * timeNormalized - timeNormalized * timeNormalized;
            yRot = yRot1 + (yRot2 - yRot1) * eased;
        } else {
            yRot = yRot2;
        }
        return Mth.wrapDegrees(initialYRot + yRot);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(FUSE_TAG, getFuse());
        output.putBoolean(PRIMED_TAG, isPrimed());
        output.putBoolean(FLYING_TAG, isFlying());
        output.putFloat(INITIAL_Y_ROT_TAG, initialYRot);
        output.putInt(RESCUE_TICKS_TAG, rescueTicks);
        output.putInt(STRUGGLE_COOLDOWN_TAG, struggleCooldown);
        if (disconnectedPlayerOnRocket != null) {
            output.store(DISCONNECTED_PLAYER_TAG, NameAndId.CODEC, disconnectedPlayerOnRocket);
        }
        output.putBoolean(FROZEN_TAG, entityData.get(DATA_FROZEN));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setFuse(input.getIntOr(FUSE_TAG, getDefaultFuseSeconds() * 20 * FUSE_DECREASE_PER_TICK));
        setPrimed(input.getBooleanOr(PRIMED_TAG, false));
        setFlying(input.getBooleanOr(FLYING_TAG, false));
        initialYRot = input.getFloatOr(INITIAL_Y_ROT_TAG, 0);
        rescueTicks = input.getIntOr(RESCUE_TICKS_TAG, 0);
        struggleCooldown = input.getIntOr(STRUGGLE_COOLDOWN_TAG, 0);
        disconnectedPlayerOnRocket = input.read(DISCONNECTED_PLAYER_TAG, NameAndId.CODEC).orElse(null);
        setFrozen(input.getBooleanOr(FROZEN_TAG, false));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("Empty/Tied", test -> TyingHelper.getTying(this) != null ? test.setAndContinue(TIED) : test.setAndContinue(EMPTY)));
        controllers.add(new AnimationController<>("Fly", test -> isFlying() ? test.setAndContinue(FLY) : PlayState.STOP));
        controllers.add(new AnimationController<>(HIT_CONTROLLER_NAME, test -> PlayState.STOP)
                .triggerableAnim(HIT_N_NAME, HIT_N)
                .triggerableAnim(HIT_S_NAME, HIT_S)
                .triggerableAnim(HIT_E_NAME, HIT_E)
                .triggerableAnim(HIT_W_NAME, HIT_W)
        );
    }

    private void playFuseSound() {
        level().players().stream()
                .filter(player -> distanceToSqr(player) <= FUSE_SOUND_PLAY_RADIUS * FUSE_SOUND_PLAY_RADIUS)
                .map(player -> (ServerPlayer) player)
                .forEach(player -> PacketDistributor.sendToPlayer(player, new PlayFuseSoundPayload(getId())));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    public int getInstaburnSeconds() {
        return instaburnSeconds;
    }

    public void setInstaburnSeconds(int instaburnSeconds) {
        this.instaburnSeconds = instaburnSeconds;
    }

    public FuseDisplayDirection getFuseDisplayDirection() {
        return entityData.get(DATA_FUSE_DISPLAY_DIRECTION);
    }

    public void setFuseDisplayDirection(FuseDisplayDirection direction) {
        entityData.set(DATA_FUSE_DISPLAY_DIRECTION, direction);
    }

    public double getFuseDisplayDistance() {
        return entityData.get(DATA_FUSE_DISPLAY_DISTANCE);
    }

    public void setFuseDisplayDistance(double distance) {
        entityData.set(DATA_FUSE_DISPLAY_DISTANCE, distance);
    }

    public double getFuseDisplayHeightOffset() {
        return entityData.get(DATA_FUSE_DISPLAY_HEIGHT_OFFSET);
    }

    public void setFuseDisplayHeightOffset(double heightOffset) {
        entityData.set(DATA_FUSE_DISPLAY_HEIGHT_OFFSET, heightOffset);
    }

    public double getFuseDisplayFontScale() {
        return entityData.get(DATA_FUSE_DISPLAY_FONT_SCALE);
    }

    public void setFuseDisplayFontScale(double fontScale) {
        entityData.set(DATA_FUSE_DISPLAY_FONT_SCALE, fontScale);
    }

    @Override
    public boolean isFrozen() {
        if (level().isClientSide()) {
            return entityData.get(DATA_FROZEN);
        }
        return disconnectedPlayerOnRocket != null;
    }

    private void setFrozen(boolean frozen) {
        entityData.set(DATA_FROZEN, frozen);
    }

    @Override
    public void freezeOnDisconnect(ServerPlayer player) {
        disconnectedPlayerOnRocket = player.nameAndId();
        setFrozen(true);
    }

    @Override
    public void unfreezeOnConnect(ServerPlayer player) {
        if (Objects.equals(player.nameAndId(), disconnectedPlayerOnRocket)) {
            TyingHelper.tie(player, this);
            disconnectedPlayerOnRocket = null;
            setFrozen(false);
        }
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public enum FuseDisplayDirection implements StringRepresentable, TranslatableEnum {
        FRONT(0, "front", UnaryOperator.identity()),
        BACK(1, "back", lookAngle -> lookAngle.scale(-1)),
        LEFT(2, "left", lookAngle -> Direction.UP.getUnitVec3().cross(lookAngle)),
        RIGHT(3, "right", lookAngle -> lookAngle.cross(Direction.UP.getUnitVec3())),
        UP(4, "up", lookAngle -> Direction.UP.getUnitVec3());

        public static final EnumCodec<FuseDisplayDirection> CODEC = StringRepresentable.fromEnum(FuseDisplayDirection::values);
        private static final IntFunction<FuseDisplayDirection> BY_ID = ByIdMap.continuous(FuseDisplayDirection::getId, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, FuseDisplayDirection> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FuseDisplayDirection::getId);
        private final int id;
        private final String key;
        private final UnaryOperator<Vec3> operator;

        FuseDisplayDirection(int id, String key, UnaryOperator<Vec3> operator) {
            this.id = id;
            this.key = key;
            this.operator = operator;
        }

        @Override
        public String getSerializedName() {
            return key;
        }

        @Override
        public Component getTranslatedName() {
            return Component.translatable("entity." + TJChase.MODID + "." + ModEntityNames.ROCKET + "." + key);
        }

        public int getId() {
            return id;
        }

        public Vec3 apply(Vec3 lookAngle, double distance) {
            return operator.apply(lookAngle).scale(distance);
        }
    }
}
