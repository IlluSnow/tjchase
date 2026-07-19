package illusnow.tjchase.entity;

import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.client.renderer.ModDataTickets;
import illusnow.tjchase.entity.goal.OwnerHurtByTargetGoal;
import illusnow.tjchase.entity.goal.OwnerHurtTargetGoal;
import illusnow.tjchase.entity.goal.ZuriAttackGoal;
import illusnow.tjchase.entity.projectile.YogaBall;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class Zuri extends TJChaseFriendlyMob implements TJChaseCharacter, GeoEntity, RangedAttackMob {
    public static final RawAnimation ZURI_CLAW_ATTACK = RawAnimation.begin().thenPlay("attack.claw");
    public static final RawAnimation ZURI_IDLE_BOWKNOT = RawAnimation.begin().thenLoop("bowknot");
    public static final RawAnimation ZURI_IDLE_TAIL = RawAnimation.begin().thenLoop("tail");
    public static final RawAnimation ZURI_WALKING_TAIL = RawAnimation.begin().thenLoop("tail_walking");
    public static final RawAnimation ZURI_THROW_YOGA_BALL = RawAnimation.begin().thenPlay("attack.throw_yoga_ball");
    public static final RawAnimation ZURI_DANCE = RawAnimation.begin().thenLoop("dance");
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
    private static final String ATTACK_CONTROLLER_NAME = "Attack";
    private static final String THROW_YOGA_BALL_ANIM_NAME = "ThrowYogaBall";
    private static final String CLAW_ATTACK_ANIM_NAME = "ClawAttack";
    private static final EntityDataAccessor<Integer> DATA_THROW_YOGA_BALL_TICKS = SynchedEntityData.defineId(
            Zuri.class, EntityDataSerializers.INT
    );
    private static final EntityDataAccessor<Boolean> DATA_DANCING = SynchedEntityData.defineId(
            Zuri.class, EntityDataSerializers.BOOLEAN
    );
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final List<EntityReference<Mob>> dancingMobs = new ArrayList<>();
    @Nullable
    private BlockPos dancingTargetPos;
    private int dancingTargetPosUpdateCooldown = DANCING_TARGET_POS_UPDATE_COOLDOWN;

    public Zuri(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.ATTACK_DAMAGE, 15)
                .add(Attributes.FOLLOW_RANGE, 24);
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
        if (!level().isClientSide()) {
            setDancing(!isDancing(), false);
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
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
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
    protected void playAttackSound() {
        playSound(ModSoundEvents.ZURI_ATTACK.get());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSoundEvents.ZURI_AMBIENT.get();
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
        controllers.add(new AnimationController<>("Dance/Walk/Idle",  5, test -> {
            boolean dancing = test.getDataOrDefault(ModDataTickets.DANCING, false);
            if (dancing) {
                test.setControllerSpeed(0.9316966F);
                return test.setAndContinue(ZURI_DANCE);
            }
            test.setControllerSpeed(1);
            return test.isMoving() ? test.setAndContinue(DefaultAnimations.WALK) : test.setAndContinue(DefaultAnimations.IDLE);
        }));
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

    public void setDancing(boolean dancing, boolean fromEntityLoading) {
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
    protected void updateControlFlags() {
        super.updateControlFlags();
        boolean dancing = isDancing();
//        goalSelector.setControlFlag(Goal.Flag.MOVE, !dancing);
//        goalSelector.setControlFlag(Goal.Flag.LOOK, !dancing);
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
        yogaBall.shoot(dx, targetY - yogaBall.getY() + distance * 0.15, dz, 1.5F, getAttackInaccuracy());
        playSound(ModSoundEvents.ZURI_THROW_YOGA_BALL.get(), 1, 1);
        if (!isDancing()) {
            triggerAnim(ATTACK_CONTROLLER_NAME, THROW_YOGA_BALL_ANIM_NAME);
        }
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

    public int getAttackInterval() {
        return 20 * (isDancing() ? 2 : 1);
    }

    @Override
    public int getMeleeAttackInterval() {
        return super.getMeleeAttackInterval() * (isDancing() ? 6 : 1);
    }

    @Override
    public float getMeleeAttackDamage() {
        return (float) (super.getMeleeAttackDamage() * (isDancing() ? 1.5 : 1));
    }

    public int getAttackInaccuracy() {
        return 10;
    }

    @Override
    public boolean hasSuperArmor() {
        return isDancing();
    }

    @Override
    public float getReducedStartDamage(float damage, float originalDamage) {
        if (isDancing()) {
            return damage - 5;
        }
        return super.getReducedStartDamage(damage, originalDamage);
    }
}
