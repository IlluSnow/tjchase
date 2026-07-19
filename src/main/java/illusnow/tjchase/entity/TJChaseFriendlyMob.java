package illusnow.tjchase.entity;

import com.mojang.logging.LogUtils;
import illusnow.tjchase.entity.proficency.ProficiencyLevel;
import illusnow.tjchase.world.ModDamageSources;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class TJChaseFriendlyMob extends PathfinderMob implements OwnableEntity, TJChaseCharacter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PROFICIENCY_TAG = "Proficiency";
    protected static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> DATA_OWNER_UUID = SynchedEntityData.defineId(
            TJChaseFriendlyMob.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE
    );
    protected static final EntityDataAccessor<Integer> DATA_PROFICIENCY_POINTS = SynchedEntityData.defineId(
            TJChaseFriendlyMob.class, EntityDataSerializers.INT
    );

    protected TJChaseFriendlyMob(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(DATA_PROFICIENCY_POINTS, 0);
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
        return hurt;
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
    protected abstract void playAttackSound();

    @Nullable
    @Override
    protected abstract SoundEvent getHurtSound(DamageSource damageSource);

    @Nullable
    @Override
    protected abstract SoundEvent getDeathSound();

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    public boolean canActivelyAttack(@Nullable LivingEntity target) {
        if (target == null || target == this) {
            return false;
        }
        if (target == getTarget()) {
            return true;
        }
        if (target instanceof Enemy) {
            return canAttack(target);
        }
        LivingEntity owner = getOwner();
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

    public boolean isOwnedBy(LivingEntity entity) {
        return entity == getOwner();
    }

    public float getMeleeAttackDamage() {
        return (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    public double getMaxMeleeAttackAngle() {
        return Math.PI / 4;
    }

    public int getMeleeAttackInterval() {
        return 12;
    }

    public double getMeleeAttackRange() {
        return 5;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        EntityReference<LivingEntity> ownerRef = this.getOwnerReference();
        EntityReference.store(ownerRef, output, "Owner");
        output.putInt(PROFICIENCY_TAG, getProficiencyPoints());
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
    }
}
