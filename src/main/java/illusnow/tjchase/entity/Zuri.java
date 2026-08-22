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

package illusnow.tjchase.entity;

import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.client.renderer.ModDataTickets;
import illusnow.tjchase.entity.goal.OwnerHurtByTargetGoal;
import illusnow.tjchase.entity.goal.OwnerHurtTargetGoal;
import illusnow.tjchase.entity.goal.ZuriAttackGoal;
import illusnow.tjchase.entity.proficiency.ProficiencyMainLevel;
import illusnow.tjchase.entity.proficiency.ProficiencyRelatedValue;
import illusnow.tjchase.entity.proficiency.interpolator.LinearInterpolator;
import illusnow.tjchase.entity.projectile.YogaBall;
import illusnow.tjchase.item.ModItems;
import illusnow.tjchase.network.PlayDanceTimePayload;
import illusnow.tjchase.sound.ModSoundEvents;
import illusnow.tjchase.util.DancingHelper;
import illusnow.tjchase.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;
import java.util.stream.Stream;

public class Zuri extends TJChaseFriendlyMob implements GeoEntity, RangedAttackMob {
    private static final ProficiencyRelatedValue RANGED_ATTACK_INTERVAL = ProficiencyRelatedValue.beginner0(80)
            .whenReached(ProficiencyMainLevel.APPRENTICE, 70)
            .whenReached(ProficiencyMainLevel.ELITE, 62)
            .whenReached(ProficiencyMainLevel.EXPERT, 55)
            .masterCutoff(50)
            .whenMaster(40)
            .whenMaster(3, 32)
            .whenMaster(2, 25)
            .masterCutoff(1, 16)
            .whenMaster(1, 12)
            .build();
    private static final ProficiencyRelatedValue ATTACK_INACCURACY = ProficiencyRelatedValue.beginner0(15)
            .whenReached(ProficiencyMainLevel.APPRENTICE, 11)
            .masterCutoff(7)
            .whenMaster(5)
            .masterCutoff(1, 2)
            .whenMaster(1, 0)
            .build(LinearInterpolator::create);
    private static final ProficiencyRelatedValue MISS_PROBABILITY = ProficiencyRelatedValue.beginner0(0.3)
            .whenReached(ProficiencyMainLevel.APPRENTICE, 0.2)
            .whenReached(ProficiencyMainLevel.ELITE, 0.13)
            .masterCutoff(0.1)
            .whenMaster(0.05)
            .whenMaster(2, 0.01)
            .whenMaster(1, 0)
            .build();
    private static final ProficiencyRelatedValue SEEK_POWER = ProficiencyRelatedValue.beginner0(0)
            .whenReached(ProficiencyMainLevel.APPRENTICE, 0)
            .whenReached(ProficiencyMainLevel.ELITE, 1E-3)
            .whenReached(ProficiencyMainLevel.EXPERT, 5E-3)
            .masterCutoff(0.01)
            .whenMaster(0.05)
            .masterCutoff(2, 0.08)
            .whenMaster(2, 0.2)
            .masterCutoff(1, 0.4)
            .whenMaster(1, 0.99)
            .build(LinearInterpolator::create);
    private static final Map<ProficiencyMainLevel, Integer> PROFICIENCY_POINTS_RANGED_ATTACK = Map.of(
            ProficiencyMainLevel.BEGINNER, 18,
            ProficiencyMainLevel.APPRENTICE, 14,
            ProficiencyMainLevel.ELITE, 10,
            ProficiencyMainLevel.EXPERT, 6,
            ProficiencyMainLevel.MASTER, 0
    );

    public static final RawAnimation ZURI_CLAW_ATTACK = RawAnimation.begin().thenPlay("attack.claw");
    public static final RawAnimation ZURI_IDLE_BOWKNOT = RawAnimation.begin().thenLoop("bowknot");
    public static final RawAnimation ZURI_THROW_YOGA_BALL = RawAnimation.begin().thenPlay("attack.throw_yoga_ball");
    public static final RawAnimation ZURI_DANCE = RawAnimation.begin().thenLoop("dance");
    public static final RawAnimation ZURI_START_WEAK = RawAnimation.begin().thenPlayAndHold("start_weak");
    public static final RawAnimation ZURI_WEAK = RawAnimation.begin().thenPlayAndHold("weak");
    public static final RawAnimation ZURI_STOP_WEAK = RawAnimation.begin().thenPlayAndHold("stop_weak");
    public static final double DANCE_TIME_PLAY_RADIUS = 16;
    public static final int DANCING_RADIUS = 7;
    public static final double INFLUENCE_RADIUS = 16;
    public static final double DANCING_TP_RADIUS = INFLUENCE_RADIUS;
    public static final int DANCING_TARGET_POS_UPDATE_COOLDOWN = 8;
    public static final double DANCE_SPEED = 0.3;
    public static final String DANCING_TAG = "Dancing";
    public static final String DANCING_MOBS_TAG = "DancingMobs";
    public static final String DANCING_MOB_TAG = "DancingMob";
    private static final String DANCING_TARGET_POS_TAG = "DancingTargetPos";
    private static final String DANCING_TARGET_POS_UPDATE_COOLDOWN_TAG = "DancingTargetPosUpdateCooldown";
    private static final String DEFAULT_CONTROLLER_NAME = "Dance/Walk/Idle";
    private static final String ATTACK_CONTROLLER_NAME = "Attack";
    private static final String THROW_YOGA_BALL_ANIM_NAME = "ThrowYogaBall";
    private static final String CLAW_ATTACK_ANIM_NAME = "ClawAttack";
    private static final String START_WEAK_ANIM_NAME = "StartWeak";
    private static final String WEAK_ANIM_NAME = "Weak";
    private static final String STOP_WEAK_ANIM_NAME = "StopWeak";
    private static final EntityDataAccessor<Integer> DATA_THROW_YOGA_BALL_TICKS = SynchedEntityData.defineId(
            Zuri.class, EntityDataSerializers.INT
    );
    private static final EntityDataAccessor<Boolean> DATA_DANCING = SynchedEntityData.defineId(
            Zuri.class, EntityDataSerializers.BOOLEAN
    );
    private static final EntityDimensions WEAK_DIMENSIONS = ModEntities.ZURI.get()
            .getDimensions()
            .scale(1, 0.64285713f);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final List<EntityReference<Mob>> dancingMobs = new ArrayList<>();
    @Nullable
    private BlockPos dancingTargetPos;
    private int dancingTargetPosUpdateCooldown = DANCING_TARGET_POS_UPDATE_COOLDOWN;

    public Zuri(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createTJChaseFriendlyMobAttributes(40, 8);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_THROW_YOGA_BALL_TICKS, 0);
        builder.define(DATA_DANCING, false);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new ZuriAttackGoal(this, 1, 1, 24, 6, 50));
        goalSelector.addGoal(6, new RandomStrollGoal(this, 0.8) {
            @Override
            public boolean canContinueToUse() {
                if (isDancing()) {
                    getNavigation().stop();
                    return false;
                }
                return super.canContinueToUse();
            }

            @Override
            public boolean canUse() {
                return super.canUse() && !isDancing();
            }
        });
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6) {
            @Override
            public boolean canUse() {
                return super.canUse() && !isDancing();
            }
        });
        goalSelector.addGoal(8, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return super.canUse() && !isDancing();
            }
        });
        targetSelector.addGoal(1, new OwnerHurtByTargetGoal<>(this, self -> true, (self, target, owner) -> true));
        targetSelector.addGoal(2, new OwnerHurtTargetGoal<>(this, self -> true, (self, target, owner) -> true));
        targetSelector.addGoal(3, new HurtByTargetGoal(this));
        targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Mob.class, true, (target, level) -> canActivelyAttack(target)));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND) {
            return super.mobInteract(player, hand);
        }
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(ModItems.PROFICIENCY_STICK) || stack.is(ModItems.ENTITY_DEBUG_STICK) || stack.is(ModItems.REMOTE_CONTROL)) {
            return super.mobInteract(player, hand);
        }
        if (isWeak()) {
            if (stack.is(Items.COOKED_BEEF)) {
                if (!level().isClientSide() && getRecoverTicks() < 0) {
                    heal(getMaxHealth());
                    usePlayerItem(player, hand, stack);
                    recoverFromWeak();
                }
                return InteractionResult.SUCCESS;
            }
            return super.mobInteract(player, hand);
        }
        if (!level().isClientSide()) {
            setDancing(!isDancing());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (getDancingTargetPos() == null) {
            setDancingTargetPos(getDefaultDancingTargetPos());
        }
        if (dancingTargetPosUpdateCooldown-- <= 0) {
            dancingTargetPosUpdateCooldown = DANCING_TARGET_POS_UPDATE_COOLDOWN;
            Vec3 dancingVec = Vec3.atCenterOf(getDancingTargetPos()).subtract(blockPosition().getCenter());
            setDancingTargetPos(BlockPos.containing(blockPosition().getCenter().add(dancingVec.yRot((float) (Math.PI / 8)))));
        }
        if (!level().isClientSide()) {
            if (entityData.get(DATA_THROW_YOGA_BALL_TICKS) > 0) {
                entityData.set(DATA_THROW_YOGA_BALL_TICKS, entityData.get(DATA_THROW_YOGA_BALL_TICKS) - 1);
            }
        }

        if (!level().isClientSide() && isDancing()) {
            for (Mob mob : level().getEntitiesOfClass(Mob.class, getBoundingBox().inflate(INFLUENCE_RADIUS))) {
                if (!DancingHelper.hasValidZuriDancingWith(mob) && DancingHelper.canDanceWithZuri(mob, this)) {
                    addDancingMob(mob);
                    Utils.setTarget(mob, null);
                }
            }
            checkAndTpDancingMobs();
        }
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return isWeak() ? WEAK_DIMENSIONS : super.getDefaultDimensions(pose);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        refreshDimensions();
    }

    public void addDancingMob(Mob mob) {
        dancingMobs.add(EntityReference.of(mob));
        mob.setData(ModAttachments.DANCING_WITH.get(), Optional.of(EntityReference.of(this)));
        mob.setData(ModAttachments.DANCE_EFFECT_TYPE.get(), DancingHelper.getDanceEffectType(mob, this));
    }

    public void removeDancingMob(Mob mob) {
        dancingMobs.removeIf(ref -> ref.getEntity(level(), Mob.class) == mob);
        DancingHelper.clearAttachmentData(mob);
    }

    private void clearDancingMobs() {
        getDancingMobsStream().forEach(DancingHelper::clearAttachmentData);
        dancingMobs.clear();
    }

    private void checkAndTpDancingMobs() {
        List<Mob> toRemove = new ArrayList<>();
        getDancingMobsStream().filter(mob -> distanceToSqr(mob) >= DANCING_TP_RADIUS * DANCING_TP_RADIUS)
                .forEach(mob -> {
                    DancingHelper.DanceEffectType effectType = DancingHelper.getDanceEffectType(mob, this);
                    if (effectType == DancingHelper.DanceEffectType.FULL) {
                        mob.snapTo(position());
                    } else {
                        toRemove.add(mob);
                    }
                });
        toRemove.forEach(this::removeDancingMob);
        getDancingMobsStream().filter(mob -> isAlliedTo(mob.getTarget())).forEach(mob -> Utils.setTarget(mob, null));
    }

    private Stream<Mob> getDancingMobsStream() {
        return dancingMobs.stream()
                .map(ref -> EntityReference.get(ref, level(), Mob.class))
                .filter(Objects::nonNull);
    }

    @Override
    protected void prepareMelee(List<LivingEntity> targets) {
        super.prepareMelee(targets);
        if (!isDancing()) {
            triggerAnim(ATTACK_CONTROLLER_NAME, CLAW_ATTACK_ANIM_NAME);
        }
    }

    @Override
    public float getVoicePitch() {
        return (random.nextFloat() - random.nextFloat()) * 0.1F + (isBaby() ? 1.5F : 1);
    }

    @Override
    protected void playAttackSound() {
        playSound(ModSoundEvents.ZURI_ATTACK.get());
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return isWeak() ? null : ModSoundEvents.ZURI_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getWeakSound() {
        return ModSoundEvents.ZURI_WEAK.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSoundEvents.ZURI_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(DEFAULT_CONTROLLER_NAME, test -> {
            if (isWeak()) {
                test.setControllerSpeed(1);
                if (getRecoverTicks() >= 0) {
                    return test.setAndContinue(ZURI_STOP_WEAK);
                }
                return test.setAndContinue(ZURI_WEAK);
            }
            boolean dancing = test.getDataOrDefault(ModDataTickets.DANCING, false);
            if (dancing) {
                test.setControllerSpeed(0.9316966F);
                return test.setAndContinue(ZURI_DANCE);
            }
            test.setControllerSpeed(1);
            return test.isMoving() ? test.setAndContinue(DefaultAnimations.WALK) : test.setAndContinue(DefaultAnimations.IDLE);
        })
                .triggerableAnim(START_WEAK_ANIM_NAME, ZURI_START_WEAK)
                .triggerableAnim(WEAK_ANIM_NAME, ZURI_WEAK)
                .triggerableAnim(STOP_WEAK_ANIM_NAME, ZURI_STOP_WEAK));
        controllers.add(new AnimationController<>(ATTACK_CONTROLLER_NAME, test -> PlayState.STOP)
                .triggerableAnim(THROW_YOGA_BALL_ANIM_NAME, ZURI_THROW_YOGA_BALL)
                .triggerableAnim(CLAW_ATTACK_ANIM_NAME, ZURI_CLAW_ATTACK));
        controllers.add(new AnimationController<>("IdleBowknot", test -> test.setAndContinue(ZURI_IDLE_BOWKNOT)));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (getDancingTargetPos() != null) {
            output.store(DANCING_TARGET_POS_TAG, BlockPos.CODEC, getDancingTargetPos());
        }
        output.putInt(DANCING_TARGET_POS_UPDATE_COOLDOWN_TAG, dancingTargetPosUpdateCooldown);
        output.putBoolean(DANCING_TAG, isDancing());
        ValueOutput.ValueOutputList dancingMobsTag = output.childrenList(DANCING_MOBS_TAG);
        for (EntityReference<Mob> dancingMob : dancingMobs) {
            ValueOutput dancingMobTag = dancingMobsTag.addChild();
            dancingMob.store(dancingMobTag, DANCING_MOB_TAG);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setDancingTargetPos(input.read(DANCING_TARGET_POS_TAG, BlockPos.CODEC).orElse(null));
        dancingTargetPosUpdateCooldown = input.getIntOr(DANCING_TARGET_POS_UPDATE_COOLDOWN_TAG, DANCING_TARGET_POS_UPDATE_COOLDOWN);
        setDancing(input.getBooleanOr(DANCING_TAG, false), true);
        ValueInput.ValueInputList dancingMobsTag = input.childrenListOrEmpty(DANCING_MOBS_TAG);
        dancingMobs.clear();
        for (ValueInput dancingMobTag : dancingMobsTag) {
            EntityReference<Mob> dancingMob = EntityReference.read(dancingMobTag, DANCING_MOB_TAG);
            if (dancingMob != null) {
                dancingMobs.add(dancingMob);
            }
        }
    }

    public boolean isDancing() {
        return entityData.get(DATA_DANCING);
    }

    public void setDancing(boolean dancing) {
        setDancing(dancing, false);
    }

    private void setDancing(boolean dancing, boolean fromEntityLoading) {
        entityData.set(DATA_DANCING, dancing);
        if (!level().isClientSide()) {
            updateControlFlags();
            if (dancing && !fromEntityLoading) {
                startDancing();
            }
        }
        if (!dancing && !fromEntityLoading) {
            stopDancing();
        }
    }

    private void startDancing() {
        if (!isSilent()) {
            playDanceTimeMusic();
        }
        heal(getMaxHealth() / 2);
        Utils.clearNegativeEffectsAndFire(this);
    }

    private void stopDancing() {
        clearDancingMobs();
    }

    private void playDanceTimeMusic() {
        level().players().stream()
                .filter(player -> distanceToSqr(player) <= DANCE_TIME_PLAY_RADIUS * DANCE_TIME_PLAY_RADIUS)
                .map(player -> (ServerPlayer) player)
                .forEach(player -> PacketDistributor.sendToPlayer(player, new PlayDanceTimePayload(getId())));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    public BlockPos getDefaultDancingTargetPos() {
        return blockPosition().south(DANCING_RADIUS);
    }

    @Nullable
    public BlockPos getDancingTargetPos() {
        return dancingTargetPos;
    }

    public void setDancingTargetPos(@Nullable BlockPos dancingTargetPos) {
        this.dancingTargetPos = dancingTargetPos;
    }

    @Override
    protected void onWeakStateStartedToChange(boolean weak) {
        super.onWeakStateStartedToChange(weak);
        if (weak) {
            triggerAnim(DEFAULT_CONTROLLER_NAME, START_WEAK_ANIM_NAME);
        } else {
            triggerAnim(DEFAULT_CONTROLLER_NAME, STOP_WEAK_ANIM_NAME);
        }
        if (weak) {
            setDeltaMovement(getDeltaMovement().multiply(0.5, 1, 0.5));
            setDancing(false);
            getNavigation().stop();
        }
    }

    @Override
    protected void onRecovered() {
        stopTriggeredAnim(DEFAULT_CONTROLLER_NAME, STOP_WEAK_ANIM_NAME);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        double dx = target.getX() - getX();
        double targetY = target.getY(0.5);
        double dz = target.getZ() - getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        YogaBall yogaBall = new YogaBall(this, level());
        yogaBall.snapTo(yogaBall.position().add(getLookAngle().scale(0.25)));
        yogaBall.setMaxCheckRange(1);
        yogaBall.setInflateProbabilityInsideRange(1);
        yogaBall.setDeflateTime(-YogaBall.DEFLATE_TICKS);
        yogaBall.setDamage(yogaBall.getDamage() * DEFAULT_PROFICIENCY_ATTACK_DAMAGE_MODIFIER.floatValue(getProficiencyPoints()));
        yogaBall.setTarget(target);
        yogaBall.setSeekPower(SEEK_POWER.doubleValue(getProficiencyPoints()));
        yogaBall.shoot(dx, targetY - yogaBall.getY() + distance * 0.15, dz, 1.5F, getAttackInaccuracy());
        if (random.nextDouble() < MISS_PROBABILITY.doubleValue(getProficiencyPoints())) {
            yogaBall.setMaxCheckRange(yogaBall.getMaxCheckRange() * (1.5 + random.nextDouble() * 2));
            yogaBall.setInflateProbabilityInsideRange(0.05 + random.nextDouble() * 0.25);
        }
        playSound(ModSoundEvents.ZURI_THROW_YOGA_BALL.get());
        if (!isDancing()) {
            triggerAnim(ATTACK_CONTROLLER_NAME, THROW_YOGA_BALL_ANIM_NAME);
        }
        addProficiencyPoints(PROFICIENCY_POINTS_RANGED_ATTACK.get(getProficiencyLevel().mainLevel()));
        level().addFreshEntity(yogaBall);
    }

    @Override
    public double getMaxMeleeAttackAngle() {
        if (isDancing()) {
            return Math.PI;
        }
        return super.getMaxMeleeAttackAngle();
    }

    @Override
    public double getMeleeAttackRange() {
        return super.getMeleeAttackRange() * (isDancing() ? 1.5 : 1);
    }

    public int getYogaBallAttackInterval() {
        int interval = RANGED_ATTACK_INTERVAL.intValue(getProficiencyPoints());
        if (isDancing()) {
            interval = interval * 3 / 2;
        }
        return interval;
    }

    @Override
    public int getMeleeAttackInterval() {
        return DEFAULT_MELEE_ATTACK_INTERVAL.intValue(getProficiencyPoints()) * (isDancing() ? 5 : 1);
    }

    @Override
    public float getMeleeAttackDamage() {
        return super.getMeleeAttackDamage() * DEFAULT_PROFICIENCY_ATTACK_DAMAGE_MODIFIER.floatValue(getProficiencyPoints()) * (isDancing() ? 2 : 1);
    }

    public float getAttackInaccuracy() {
        return ATTACK_INACCURACY.floatValue(getProficiencyPoints());
    }

    @Override
    public boolean hasSuperArmor() {
        return isDancing();
    }

    @Override
    public float getReducedStartDamage(DamageSource source, float damage, float originalDamage) {
        float damageReduction = DEFAULT_DAMAGE_REDUCTION_PRE.floatValue(getProficiencyPoints());
        damage = damage * (1 - damageReduction);
        float minDamage = Math.min(damage, 1 * (1 - damageReduction));
        if (isDancing()) {
            damage = Math.max(minDamage, damage - 5);
        }
        return damage;
    }
}
