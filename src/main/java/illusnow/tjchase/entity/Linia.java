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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.sound.ModSoundEvents;
import illusnow.tjchase.tag.ModEntityTypeTags;
import illusnow.tjchase.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Objects;
import java.util.Optional;

public class Linia extends PathfinderMob implements GeoEntity, HealthLockable {
    private static final String STORED_ENTITY_TYPE_TAG = "StoredEntityType";
    private static final String ENTITY_DATA_TAG = "AdditionalEntityData";
    private static final String LIFE_TAG = "Life";
    public static final float MAX_ATTACK_DAMAGE = 1;
    public static final float DAMAGE_ADDITION = 1F;
    private static final int MAX_LIFE = 160;
    private static final int IMMUNE_TICKS = 80;
    protected static final Logger LOGGER = LogUtils.getLogger();
    protected static final ImmutableList<SensorType<? extends Sensor<? super Linia>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.HURT_BY
    );
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.PATH,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.IS_PANICKING
    );
    @Nullable
    protected EntityType<? extends Mob> storedEntityType;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    protected int life = -1;
    protected CompoundTag tag = new CompoundTag();

    public Linia(EntityType<? extends Linia> entityType, Level level) {
        super(entityType, level);
        moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.FLYING_SPEED, 0.1)
                .add(Attributes.FOLLOW_RANGE, 16)
                .add(Attributes.MOVEMENT_SPEED, 0.1)
                .add(Attributes.ATTACK_DAMAGE, 1.5);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && life > 0) {
            life--;
            ServerLevel level = (ServerLevel) level();
            if (life == 0) {
                if (storedEntityType != null && EventHooks.canLivingConvert(this, storedEntityType, timer -> {})) {
                    try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(LOGGER)) {
                        LivingEntity target = getTarget();
                        Mob mob = (Mob) EntityType.loadEntityRecursive(storedEntityType, TagValueInput.create(reporter, level.registryAccess(), tag), level(), EntitySpawnReason.CONVERSION, EntityProcessor.NOP);
                        if (mob != null) {
                            mob.copyPosition(this);
                            Utils.setTarget(mob, target);
                            mob.setHealth(getHealth());
                            mob.setAbsorptionAmount(getAbsorptionAmount());
                            mob.setData(ModAttachments.BLUEPRINT_CONVERSION_IMMUNE_TICKS.get(), level().getGameTime() + IMMUNE_TICKS);
                            BlueprintManager.updateBlueprintData(mob);
                            EventHooks.onLivingConvert(this, mob);
                            if (!Utils.canFly(mob)) {
                                mob.snapTo(Utils.tryMoveDownToGround(mob.level(), mob.position(), 12));
                            }
                            if (level().addFreshEntity(mob)) {
                                discard();
                            }
                        }
                    }
                } else {
                    discard();
                }
            }
        }
    }

    public static boolean isConvertible(Mob mob) {
        if (mob instanceof Linia) {
            return false;
        }
        if (mob.getType().is(Tags.EntityTypes.BOSSES)) {
            return false;
        }
        if (mob.isInvulnerable() || !mob.attackable()) {
            return false;
        }
        if (mob instanceof OwnableEntity ownable && BlueprintManager.getBlueprintOf(ownable.getOwner(), blueprintManager -> blueprintManager.getOwner() == ownable.getOwner()) != null) {
            return false;
        }
        return mob.isAlive() && mob.getData(ModAttachments.BLUEPRINT_CONVERSION_IMMUNE_TICKS) < mob.level().getGameTime();
    }

    @Override
    public void checkDespawn() {
        super.checkDespawn();
        if (level().getDifficulty() == Difficulty.PEACEFUL && storedEntityType != null && !storedEntityType.isAllowedInPeaceful()) {
            discard();
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static Linia convert(Mob mob) {
        EntityType<? extends Linia> liniaType = mob instanceof Enemy ? ModEntities.EVILINIA.get() : ModEntities.LINIA.get();
        if (mob.level().isClientSide() || !EventHooks.canLivingConvert(mob, liniaType, timer -> {})) {
            return null;
        }
        ServerLevel level = (ServerLevel) mob.level();
        double maxHealth = mob.getAttributeBaseValue(Attributes.MAX_HEALTH);
        double attackDamage = mob.getAttribute(Attributes.ATTACK_DAMAGE) == null ? 1 : mob.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
        double armor = mob.getAttributeBaseValue(Attributes.ARMOR);
        double armorToughness = mob.getAttributeBaseValue(Attributes.ARMOR_TOUGHNESS);
        LivingEntity target = mob.getTarget();
        mob.getPassengers().forEach(Entity::stopRiding);
        mob.stopRiding();

        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, level.registryAccess());
            mob.save(output);

            Linia linia = mob.convertTo(liniaType, ConversionParams.single(mob, true, true), converted -> {
                converted.tag = output.buildResult();
                converted.copyPosition(mob);
                converted.snapTo(mob.getBoundingBox().getCenter());
                EventHooks.finalizeMobSpawn(converted, level, level.getCurrentDifficultyAt(converted.blockPosition()), EntitySpawnReason.CONVERSION, null);
                converted.getNonnullAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth);
                converted.getNonnullAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(Math.min(attackDamage, MAX_ATTACK_DAMAGE));
                converted.getNonnullAttribute(Attributes.ARMOR).setBaseValue(armor);
                converted.getNonnullAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(armorToughness);
                converted.setHealth(mob.getHealth());
                converted.storedEntityType = (EntityType<? extends Mob>) mob.getType();
                converted.life = MAX_LIFE;
                EventHooks.onLivingConvert(mob, converted);
            });
            if (linia != null) {
                if (!linia.isPassive()) {
                    Utils.setTarget(linia, target);
                }
                linia.spawnAnim();
            }
            return linia;
        }
    }

    protected AttributeInstance getNonnullAttribute(Holder<Attribute> attribute) {
        return Objects.requireNonNull(getAttribute(attribute));
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        return getTargetFromBrain();
    }

    @Override
    protected Brain.Provider<Linia> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        Brain<Linia> brain = brainProvider().makeBrain(dynamic);
        initCoreActivity(brain);
        initIdleActivity(brain);
        initFightActivity(this, brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<? extends Linia> brain) {
        brain.addActivity(
                Activity.CORE,
                0,
                ImmutableList.of(
                        new Swim<>(0.8F),
                        new AnimalPanic<Linia>(1.5F) {
                            @Override
                            protected boolean checkExtraStartConditions(ServerLevel level, Linia linia) {
                                return linia.isPassive() && super.checkExtraStartConditions(level, linia);
                            }
                        },
                        new LookAtTargetSink(45, 90),
                        new MoveToTargetSink(),
                        new CountDownCooldownTicks(MemoryModuleType.ATTACK_TARGET_COOLDOWN)
                )
        );
    }

    @SuppressWarnings("deprecation")
    private static void initIdleActivity(Brain<? extends Linia> brain) {
        brain.addActivityWithConditions(
                Activity.IDLE,
                ImmutableList.of(
                        Pair.of(0, StartAttacking.create((level, linia) -> linia.findNearestPlayer())),
                        Pair.of(1, StartAttacking.create((level, linia) -> linia.getHurtByIfNotPassive())),
                        Pair.of(2, SetEntityLookTargetSometimes.create(6, UniformInt.of(30, 60))),
                        Pair.of(
                                3,
                                new RunOne<>(
                                        ImmutableList.of(
                                                Pair.of(RandomStroll.fly(1), 2), Pair.of(SetWalkTargetFromLookTarget.create(1, 3), 2), Pair.of(new DoNothing(10, 20), 1)
                                        )
                                )
                        )
                ),
                ImmutableSet.of()
        );
    }

    private static void initFightActivity(Linia linia, Brain<? extends Linia> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(
                Activity.FIGHT,
                10,
                ImmutableList.of(
                        StopAttackingIfTargetInvalid.create(Sensor.wasEntityAttackableLastNTicks(linia, 100).negate()::test),
                        SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.2F),
                        MeleeAttack.create(20)
                ),
                MemoryModuleType.ATTACK_TARGET
        );
    }

    Optional<? extends Player> findNearestPlayer() {
        if (isPassive()) {
            return Optional.empty();
        }
        return getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
    }

    @Override
    public void travel(Vec3 travelVector) {
        travelFlying(travelVector, getSpeed());
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {}

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {}

    public boolean isPassive() {
        return storedEntityType == null || storedEntityType.getCategory() != MobCategory.MONSTER;
    }

    Optional<LivingEntity> getHurtByIfNotPassive() {
        return getBrain()
                .getMemory(MemoryModuleType.HURT_BY)
                .filter(source -> !isPassive())
                .map(DamageSource::getEntity)
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setRequiredPathLength(48);
        return navigation;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return super.hurtServer(level, source, amount * (1 + DAMAGE_ADDITION));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("liniaBrain");
        getBrain().tick(level, this);
        profilerFiller.pop();
        profilerFiller.push("liniaActivityUpdate");
        getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
        profilerFiller.pop();
        super.customServerAiStep(level);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<Linia> getBrain() {
        return (Brain<@NotNull Linia>) super.getBrain();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(LIFE_TAG, life);
        output.store(ENTITY_DATA_TAG, CompoundTag.CODEC, tag);
        if (storedEntityType != null) {
            Identifier key = entityTypes().getKey(storedEntityType);
            Objects.requireNonNull(key);
            output.putString(STORED_ENTITY_TYPE_TAG, key.toString());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        life = input.getIntOr(LIFE_TAG, -1);
        tag = input.read(ENTITY_DATA_TAG, CompoundTag.CODEC).orElseGet(CompoundTag::new);
        try {
            storedEntityType = (EntityType<? extends Mob>) input.getString(STORED_ENTITY_TYPE_TAG).map(Identifier::tryParse).map(entityTypes()::getValue).orElse(null);
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to load storedEntityType", e);
            storedEntityType = null;
        }
    }

    @Override
    protected void dropFromLootTable(ServerLevel level, DamageSource damageSource, boolean playerKill) {
        if (storedEntityType == null) {
            super.dropFromLootTable(level, damageSource, playerKill);
            return;
        }
        Optional<ResourceKey<LootTable>> lootTable = storedEntityType.getDefaultLootTable();
        lootTable.ifPresent(lootTableKey -> dropFromLootTable(level, damageSource, playerKill, lootTableKey));
    }

    private Registry<EntityType<?>> entityTypes() {
        return level().registryAccess().lookupOrThrow(Registries.ENTITY_TYPE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public EntityType<? extends Linia> getType() {
        return (EntityType<? extends Linia>) super.getType();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericFlyIdleController());
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSoundEvents.LINIA_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSoundEvents.LINIA_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSoundEvents.LINIA_DEATH.get();
    }

    @Override
    public float getLockedHealth() {
        if (storedEntityType != null && storedEntityType.is(ModEntityTypeTags.TJCHASE_FRIENDLY_MOBS)) {
            return TJChaseFriendlyMob.MINIMUM_HEALTH;
        }
        return 0;
    }
}