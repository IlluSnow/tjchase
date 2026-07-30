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

import com.mojang.logging.LogUtils;
import illusnow.tjchase.entity.proficiency.ProficiencyLevel;
import illusnow.tjchase.entity.proficiency.ProficiencyMainLevel;
import illusnow.tjchase.entity.proficiency.ProficiencyRelatedValue;
import illusnow.tjchase.world.ModDamageSources;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class TJChaseFriendlyMob extends PathfinderMob implements OwnableEntity, TJChaseCharacter, HealthLockable {
    protected static final ProficiencyRelatedValue DEFAULT_PROFICIENCY_ATTACK_DAMAGE_MODIFIER = ProficiencyRelatedValue.beginner0(1)
            .whenReached(ProficiencyMainLevel.APPRENTICE, 1.2)
            .whenReached(ProficiencyMainLevel.ELITE, 1.4)
            .whenReached(ProficiencyMainLevel.EXPERT, 1.6)
            .masterCutoff(1.8)
            .whenMaster(2)
            .whenMaster(4, 2.2)
            .whenMaster(3, 2.4)
            .masterCutoff(2, 2.6)
            .whenMaster(2, 3)
            .masterCutoff(1, 3.4)
            .whenMaster(1, 4)
            .build();
    protected static final ProficiencyRelatedValue DEFAULT_DAMAGE_REDUCTION_PRE = ProficiencyRelatedValue.beginner0(0)
            .whenReached(ProficiencyMainLevel.APPRENTICE, 0.1)
            .whenReached(ProficiencyMainLevel.ELITE, 0.2)
            .whenReached(ProficiencyMainLevel.EXPERT, 0.3)
            .masterCutoff(0.4)
            .whenMaster(0.5)
            .masterCutoff(2, 0.625)
            .whenMaster(2, 0.75)
            .masterCutoff(1, 0.825)
            .whenMaster(1, 0.9)
            .build();
    protected static final ProficiencyRelatedValue DEFAULT_MELEE_ATTACK_INTERVAL = ProficiencyRelatedValue.beginner0(50)
            .whenReached(ProficiencyMainLevel.APPRENTICE, 43)
            .whenReached(ProficiencyMainLevel.ELITE, 36)
            .whenReached(ProficiencyMainLevel.EXPERT, 30)
            .whenMaster(25)
            .whenMaster(4, 22)
            .whenMaster(3, 18)
            .whenMaster(2, 15)
            .whenMaster(1, 12)
            .build();
    private static final Map<ProficiencyMainLevel, Integer> PROFICIENCY_POINTS_MELEE = Map.of(
            ProficiencyMainLevel.BEGINNER, 8,
            ProficiencyMainLevel.APPRENTICE, 6,
            ProficiencyMainLevel.ELITE, 4,
            ProficiencyMainLevel.EXPERT, 3,
            ProficiencyMainLevel.MASTER, 0
    );
    private static final Map<ProficiencyMainLevel, Float> PROFICIENCY_POINTS_MUL_DAMAGE = Map.of(
            ProficiencyMainLevel.BEGINNER, 1F,
            ProficiencyMainLevel.APPRENTICE, 0.9F,
            ProficiencyMainLevel.ELITE, 0.8F,
            ProficiencyMainLevel.EXPERT, 0.7F,
            ProficiencyMainLevel.MASTER, 0.5F
    );
    private static final Map<ProficiencyMainLevel, Float> PROFICIENCY_POINTS_MUL_KILL = Map.of(
            ProficiencyMainLevel.BEGINNER, 1F,
            ProficiencyMainLevel.APPRENTICE, 1F,
            ProficiencyMainLevel.ELITE, 0.9F,
            ProficiencyMainLevel.EXPERT, 0.9F,
            ProficiencyMainLevel.MASTER, 0.8F
    );

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PROFICIENCY_TAG = "Proficiency";
    private static final String WEAK_TAG = "Weak";
    private static final String RECOVER_TICKS_TAG = "RecoverTicks";
    private static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> DATA_OWNER_UUID = SynchedEntityData.defineId(
            TJChaseFriendlyMob.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE
    );
    private static final EntityDataAccessor<Integer> DATA_PROFICIENCY_POINTS = SynchedEntityData.defineId(TJChaseFriendlyMob.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_RECOVER_TICKS = SynchedEntityData.defineId(TJChaseFriendlyMob.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_WEAK = SynchedEntityData.defineId(
            TJChaseFriendlyMob.class, EntityDataSerializers.BOOLEAN
    );
    protected static final int DEFAULT_MELEE_ATTACK_RANGE = 5;
    protected static final int DEFAULT_MAX_WEAK_TICKS = 15;
    protected static final float MINIMUM_HEALTH = 0.001F;
    protected final int maxWeakTicks = initMaxWeakTicks();

    protected TJChaseFriendlyMob(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    protected int initMaxWeakTicks() {
        return DEFAULT_MAX_WEAK_TICKS;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
        if (isWeak() && getRecoverTicks() > 0) {
            setRecoverTicks(getRecoverTicks() - 1);
            if (getRecoverTicks() == 0) {
                setWeak(false, false);
                setRecoverTicks(-1);
            }
        }
    }

    @Override
    public void kill(ServerLevel level) {
        if (isWeak()) {
            remove(RemovalReason.KILLED);
            gameEvent(GameEvent.ENTITY_DIE);
        } else {
            super.kill(level);
        }
    }

    protected static AttributeSupplier.Builder createTJChaseFriendlyMobAttributes(double maxHealth, double attackDamage) {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.MAX_HEALTH, maxHealth)
                .add(Attributes.ATTACK_DAMAGE, attackDamage)
                .add(Attributes.FOLLOW_RANGE, 24);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(DATA_PROFICIENCY_POINTS, 0);
        builder.define(DATA_WEAK, false);
        builder.define(DATA_RECOVER_TICKS, -1);
    }

    protected int getRecoverTicks() {
        return entityData.get(DATA_RECOVER_TICKS);
    }

    protected void setRecoverTicks(int recoverTicks) {
        entityData.set(DATA_RECOVER_TICKS, recoverTicks);
    }

    @Override
    public int getProficiencyPoints() {
        return entityData.get(DATA_PROFICIENCY_POINTS);
    }

    @Override
    public void setProficiencyPoints(int points) {
        entityData.set(DATA_PROFICIENCY_POINTS, Mth.clamp(points, 0, ProficiencyLevel.UPPER_LIMIT_TOTAL));
    }

    @Nullable
    @Override
    public EntityReference<LivingEntity> getOwnerReference() {
        return entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(owner).map(EntityReference::of));
    }

    public void setOwnerReference(@Nullable EntityReference<LivingEntity> ownerReference) {
        entityData.set(DATA_OWNER_UUID, Optional.ofNullable(ownerReference));
    }

    @Override
    public boolean isWeak() {
        return entityData.get(DATA_WEAK);
    }

    @Override
    public void setWeak() {
        setWeak(true, false);
    }

    @Override
    public void recoverFromWeak() {
        setRecoverTicks(maxWeakTicks);
        onWeakStateStartedToChange(false);
    }

    private void setWeak(boolean weak, boolean fromEntityLoading) {
        entityData.set(DATA_WEAK, weak);
        setInvulnerable(weak);
        updateControlFlags();
        setRecoverTicks(-1);
        if (!fromEntityLoading) {
            if (weak) {
                onWeakStateStartedToChange(true);
            } else {
                onRecovered();
            }
        }
    }

    @Override
    protected void updateControlFlags() {
        super.updateControlFlags();
        goalSelector.setControlFlag(Goal.Flag.MOVE, !isWeak());
        goalSelector.setControlFlag(Goal.Flag.JUMP, !isWeak());
        goalSelector.setControlFlag(Goal.Flag.LOOK, !isWeak());
    }

    @Override
    public boolean isInvulnerable() {
        return super.isInvulnerable() || isWeak();
    }

    @Override
    public boolean attackable() {
        return super.attackable() && !isWeak();
    }

    @Override
    public boolean fireImmune() {
        return super.fireImmune() || hasSuperArmor();
    }

    public boolean meleeAttack(LivingEntity attackTarget, double attackRange) {
        List<LivingEntity> targets = findNearbyTargets(attackTarget, attackRange);
        if (!targets.isEmpty()) {
            prepareMelee(targets);
            return meleeAttack(targets);
        }
        return false;
    }

    protected @NotNull List<LivingEntity> findNearbyTargets(LivingEntity attackTarget, double attackRange) {
        AABB attackBox = getAttackBoundingBox(attackRange);
        double dx = attackTarget.getX() - getX();
        double dy = attackTarget.getEyeY() - getY();
        double dz = attackTarget.getZ() - getZ();
        Vec3 direction = new Vec3(dx, dy, dz).normalize();
        List<LivingEntity> targets = new ArrayList<>();
        targets.add(attackTarget);
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, attackBox)) {
            if (target == this || target == attackTarget) {
                continue;
            }
            Vec3 targetDirection = new Vec3(target.getX() - getX(), target.getEyeY() - getY(), target.getZ() - getZ()).normalize();
            double dotProduct = direction.dot(targetDirection);
            if (dotProduct >= Mth.cos(getMaxMeleeAttackAngle()) && canActivelyAttack(target)) {
                targets.add(target);
            }
        }
        return targets;
    }

    protected void prepareMelee(List<LivingEntity> targets) {}

    public final boolean meleeAttack(List<LivingEntity> targets) {
        boolean hurt = false;
        for (LivingEntity target : targets) {
            hurt |= doHurtTarget((ServerLevel) level(), target);
        }
        awardMeleeProficiencyPoints(targets);
        return hurt;
    }

    protected void awardMeleeProficiencyPoints(List<LivingEntity> targets) {
        addProficiencyPoints(PROFICIENCY_POINTS_MELEE.get(getProficiencyLevel().mainLevel()));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity entity) {
        float damage = getMeleeAttackDamage();
        ItemStack weapon = getWeaponItem();
        // Modified DamageSource to prevent scaling with difficulty
        DamageSource damagesource = weapon.getDamageSource(this, () -> ModDamageSources.mobAttackNoScaling(this));
        damage = EnchantmentHelper.modifyDamage(level, weapon, entity, damagesource, damage);
        damage += weapon.getItem().getAttackDamageBonus(entity, damage, damagesource);
        Vec3 deltaMovement = entity.getDeltaMovement();
        boolean hurt = entity.hurtServer(level, damagesource, damage);
        if (hurt) {
            causeExtraKnockback(entity, this.getKnockback(entity, damagesource), deltaMovement);
            if (entity instanceof LivingEntity livingEntity) {
                weapon.hurtEnemy(livingEntity, this);
            }
            EnchantmentHelper.doPostAttackEffects(level, entity, damagesource);
            setLastHurtMob(entity);
            playAttackSound();
        }
        lungeForwardMaybe();
        return hurt;
    }

    @Override
    public boolean canBeSeenAsEnemy() {
        if (isWeak()) {
            return false;
        }
        return super.canBeSeenAsEnemy();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (isWeak()) {
            return false;
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    protected abstract void playAttackSound();

    @Nullable
    @Override
    protected abstract SoundEvent getHurtSound(DamageSource damageSource);

    @Nullable
    @Override
    protected abstract SoundEvent getDeathSound();

    @Nullable
    @Override
    protected abstract SoundEvent getAmbientSound();

    @Nullable
    protected abstract SoundEvent getWeakSound();

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    public void playAmbientSound() {
        if (!isWeak()) {
            super.playAmbientSound();
        }
    }

    public boolean canActivelyAttack(@Nullable LivingEntity target) {
        if (target == null || target == this) {
            return false;
        }
        if (target == getTarget()) {
            return canAttack(target);
        }
        if (target instanceof Enemy) {
            return canAttack(target);
        }
        LivingEntity owner = getOwner();
        if (target != owner && target == getLastHurtByMob()) {
            return super.canAttack(target);
        }
        if (owner != null && (target == owner.getLastHurtByMob() || target == owner.getLastHurtMob())) {
            return canAttack(target);
        }
        if (getTarget() != null && target.getType() == getTarget().getType() && target.getType() != EntityType.PLAYER) {
            return canAttack(target) && !(target instanceof OwnableEntity && ((OwnableEntity) target).getOwner() == owner);
        }
        return false;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !isOwnedBy(target) && super.canAttack(target);
    }

    @Override
    protected boolean considersEntityAsAlly(Entity entity) {
        if (entity == this || entity instanceof LivingEntity livingEntity && isOwnedBy(livingEntity)) {
            return true;
        }
        return super.considersEntityAsAlly(entity);
    }

    @Override
    public void awardDamageProficiencyPoints(LivingEntity entity, float damageDealt) {
        if (!isOwnedBy(entity)) {
            TJChaseCharacter.super.awardDamageProficiencyPoints(entity, damageDealt * PROFICIENCY_POINTS_MUL_DAMAGE.get(getProficiencyLevel().mainLevel()));
        }
    }

    @Override
    public void awardKillProficiencyPoints(LivingEntity entity, float maxHealth) {
        if (!isOwnedBy(entity)) {
            TJChaseCharacter.super.awardKillProficiencyPoints(entity, maxHealth * PROFICIENCY_POINTS_MUL_KILL.get(getProficiencyLevel().mainLevel()));
        }
    }

    public boolean isOwnedBy(LivingEntity entity) {
        return entity == getOwner();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        EntityReference<LivingEntity> ownerRef = this.getOwnerReference();
        EntityReference.store(ownerRef, output, "Owner");
        output.putInt(PROFICIENCY_TAG, getProficiencyPoints());
        output.putBoolean(WEAK_TAG, isWeak());
        output.putInt(RECOVER_TICKS_TAG, getRecoverTicks());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        EntityReference<LivingEntity> ownerRef = EntityReference.readWithOldOwnerConversion(input, "Owner", level());
        if (ownerRef != null) {
            try {
                entityData.set(DATA_OWNER_UUID, Optional.of(ownerRef));
            } catch (Throwable throwable) {
                LOGGER.error("Failed to read owner of {}:", getDisplayName(), throwable);
            }
        } else {
            entityData.set(DATA_OWNER_UUID, Optional.empty());
        }
        entityData.set(DATA_PROFICIENCY_POINTS, input.getIntOr(PROFICIENCY_TAG, 0));
        setWeak(input.getBooleanOr(WEAK_TAG, false), true);
        setRecoverTicks(input.getIntOr(RECOVER_TICKS_TAG, -1));
    }

    public float getMeleeAttackDamage() {
        return (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    public double getMaxMeleeAttackAngle() {
        return Math.PI / 4;
    }

    public int getMeleeAttackInterval() {
        return DEFAULT_MELEE_ATTACK_INTERVAL.intValue(getProficiencyPoints());
    }

    public double getMeleeAttackRange() {
        return DEFAULT_MELEE_ATTACK_RANGE;
    }

    @Override
    public float getReducedStartDamage(DamageSource source, float damage, float originalDamage) {
        return damage * (1 - DEFAULT_DAMAGE_REDUCTION_PRE.floatValue(getProficiencyPoints()));
    }

    @Override
    public float getReducedFinalDamage(DamageSource source, float damage, float originalDamage) {
        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (damage >= getHealth() - MINIMUM_HEALTH) {
                damage = getHealth() - MINIMUM_HEALTH;
                setWeak();
            }
        }
        return damage;
    }

    protected void onWeakStateStartedToChange(boolean weak) {
        if (weak) {
            clearTargetsNearby(64);
            SoundEvent weakSound = getWeakSound();
            if (weakSound != null) {
                playSound(weakSound);
            }
            clearFire();
            removeAllEffects();
        }
    }

    protected void clearTargetsNearby(double distance) {
        for (Mob mob : level().getEntitiesOfClass(Mob.class, getBoundingBox().inflate(distance), mob -> mob.getTarget() == this)) {
            mob.setTarget(null);
        }
    }

    protected abstract void onRecovered();

    @Override
    public float getLockedHealth() {
        return MINIMUM_HEALTH;
    }

    @Override
    public boolean processDamageInEventListeners() {
        return false;
    }
}
