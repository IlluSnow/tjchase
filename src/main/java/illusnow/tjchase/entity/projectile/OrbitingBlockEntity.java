package illusnow.tjchase.entity.projectile;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.entity.ModEntityDataSerializers;
import illusnow.tjchase.item.enchantment.ModEnchantmentEffectComponents;
import illusnow.tjchase.tag.ModBlockTags;
import illusnow.tjchase.util.AngelTomPassive2Owner;
import illusnow.tjchase.util.HarpConstants;
import illusnow.tjchase.util.OrbitingBlock;
import illusnow.tjchase.world.ModDamageSources;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.event.EventHooks;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public class OrbitingBlockEntity extends Projectile implements ItemSupplier {
    private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE = SynchedEntityData.defineId(
            OrbitingBlockEntity.class, EntityDataSerializers.BLOCK_STATE
    );
    private static final EntityDataAccessor<Properties> DATA_PROPERTIES = SynchedEntityData.defineId(
            OrbitingBlockEntity.class, ModEntityDataSerializers.ORBITING_BLOCK_ENTITY_PROPERTIES.get()
    );
    private static final EntityDataAccessor<Optional<EntityReference<Entity>>> DATA_CURRENT_TARGET = SynchedEntityData.defineId(
            OrbitingBlockEntity.class, ModEntityDataSerializers.OPTIONAL_ENTITY_REFERENCE.get()
    );
    private static final String BLOCK_STATE_TAG = "BlockState";
    private static final String PROPERTIES_TAG = "Properties";
    private static final String PRESET_TARGET_TAG = "PresetTarget";
    private static final String TARGET_TAG = "Target";
    private static final String LIFE_TAG = "Life";
    private static final ExplosionDamageCalculator TNT_USED_PORTAL_DAMAGE_CALCULATOR = new ExplosionDamageCalculator() {
        @Override
        public boolean shouldBlockExplode(Explosion explosion, BlockGetter reader, BlockPos pos, BlockState state, float power) {
            return !state.is(Blocks.NETHER_PORTAL) && super.shouldBlockExplode(explosion, reader, pos, state, power);
        }

        @Override
        public Optional<Float> getBlockExplosionResistance(
                Explosion explosion, BlockGetter reader, BlockPos pos, BlockState state, FluidState fluid
        )
        {
            return state.is(Blocks.NETHER_PORTAL)
                    ? Optional.empty()
                    : super.getBlockExplosionResistance(explosion, reader, pos, state, fluid);
        }
    };
    private boolean usedPortal;
    @Nullable
    private EntityReference<Entity> presetTarget;
    private int life;

    public OrbitingBlockEntity(EntityType<? extends OrbitingBlockEntity> type, Level level) {
        super(type, level);
    }

    public OrbitingBlockEntity(Entity owner, OrbitingBlock block) {
        super(ModEntities.ORBITING_BLOCK.get(), owner.level());
        setOwner(owner);
        setPos(block.calculateBottomCenterWorldPos(owner, owner.getYRot()));
        setYRot(owner.getYHeadRot());
        setBlockState(block.getBlockState());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        if (tickCount < 2 && distance < 12.25) {
            return false;
        } else {
            double maxDistance = getBoundingBox().getSize() * 4.0;
            if (Double.isNaN(maxDistance)) {
                maxDistance = 4.0;
            }
            maxDistance *= 64.0;
            return distance < maxDistance * maxDistance;
        }
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, velocity, inaccuracy);
    }

    @Override
    public boolean canUsePortal(boolean allowPassengers) {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_BLOCK_STATE, Blocks.AIR.defaultBlockState());
        builder.define(DATA_PROPERTIES, Properties.DEFAULT);
        builder.define(DATA_CURRENT_TARGET, Optional.empty());
    }

    @Override
    public void tick() {
        if (life < Integer.MAX_VALUE) {
            life++;
        }
        handleFirstTickBubbleColumn();
        applyGravity();
        applyInertia();
        if (canSeek()) {
            Entity target = findTarget(tickCount % 10 == 0);
            if (target != null) {
                trySeek(target);
            }
        }
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        Vec3 nextPosition;
        if (hitResult.getType() != HitResult.Type.MISS && !EventHooks.onProjectileImpact(this, hitResult)) {
            nextPosition = hitResult.getLocation();
        } else {
            nextPosition = position().add(getDeltaMovement());
        }
        setPos(nextPosition);
        updateRotation();
        applyEffectsFromBlocks();
        super.tick();
        if (hitResult.getType() != HitResult.Type.MISS && isAlive()) {
            hitTargetOrDeflectSelf(hitResult);
        }
    }

    public boolean canSeek() {
        return getPresetTarget() != null || getProperties().canActivelySeek();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if (!getBlockState().isAir()) {
            AABB bounds = getBlockBounds();
            float width = (float) ((bounds.maxX - bounds.minX) + (bounds.maxZ - bounds.minZ)) / 2;
            float height = (float) (bounds.maxY - bounds.minY);
            return EntityDimensions.scalable(width, height).scale(getProperties().size());
        }
        return super.getDimensions(pose);
    }

    public AABB getBlockBounds() {
        return getBlockState().getShape(level(), blockPosition()).bounds();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!(result instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof EnderMan) && level() instanceof ServerLevel level) {
            double x = getX();
            double y = getY();
            double z = getZ();
            BlockPos pos = BlockPos.containing(result.getLocation());
            double fallPower = Math.max(0, getDeltaMovement().scale(5).length());

            onBreak(level, pos, fallPower, this, x, y, z, HarpConstants.TNT_EXPLOSION_POWER, Level.ExplosionInteraction.TNT);
            discard();
        }
    }

    private static void onBreak(ServerLevel level, BlockPos pos, double fallPower, OrbitingBlockEntity entity, double x, double y, double z, float explosionPower, Level.ExplosionInteraction explosionInteraction) {
        if (entity.getProperties().type() == BlockType.TNT) {
            applyExplosion(level, entity.getOwner() == null ? entity : entity.getOwner(), entity.getX(), entity.getY(0.0625), entity.getZ(), entity.usedPortal, explosionPower, entity.isOnFire(), explosionInteraction);
        } else {
            addBreakEffects(level, entity.getBlockState(), pos, null, fallPower, x, y, z);
        }
    }

    public static void applyExplosion(ServerLevel level, @Nullable Entity owner, double x, double y, double z, boolean usedPortal, float explosionPower, boolean fire, Level.ExplosionInteraction explosionInteraction) {
        if (!(owner instanceof Player) && !EventHooks.canEntityGrief(level, owner)) {
            return;
        }
        level.explode(
                        null,
                        Explosion.getDefaultDamageSource(level, owner),
                        usedPortal ? TNT_USED_PORTAL_DAMAGE_CALCULATOR : null,
                        x,
                        y,
                        z,
                        explosionPower,
                        fire,
                        explosionInteraction
                );
    }

    public static void addBreakEffects(ServerLevel level, BlockState state, BlockPos hitPos, @Nullable Entity fallingOn, double fallPower, double x, double y, double z) {
        double particleFactor = Math.min(0.2F + fallPower / 15.0, 2.5);
        int numberOfParticles = (int) (150 * particleFactor);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state, hitPos), x, y, z, numberOfParticles, 0, 0, 0, 0.15);
        SoundType type = state.getBlock().getSoundType(state, level, hitPos, fallingOn);
        if (!state.isAir() && !IClientBlockExtensions.of(state).playBreakSound(state, level, hitPos)) {
            SoundEvent breakSound = type.getBreakSound();
            level.playSound(null, x, y, z, breakSound, SoundSource.BLOCKS, (type.getVolume() + 1) / 2F, type.getPitch() * 0.8F);
        }
    }

    @Nullable
    @Override
    public Entity teleport(TeleportTransition teleportTransition) {
        Entity entity = super.teleport(teleportTransition);
        if (entity instanceof OrbitingBlockEntity orbitingBlockEntity && orbitingBlockEntity.getProperties().type() == BlockType.TNT) {
            orbitingBlockEntity.usedPortal = true;
        }
        return entity;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level() instanceof ServerLevel level) {
            hurtEntity(result.getEntity(), level, 1);
            applyAOE(result.getLocation(), result.getEntity(), level);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level() instanceof ServerLevel level) {
            applyAOE(result.getLocation(), null, level);
        }
    }

    protected void addExplosionParticle(ServerLevel level, Vec3 position) {
        level.sendParticles(ParticleTypes.EXPLOSION, position.x, position.y, position.z, 1, getProperties().aoeRadius() * 0.075, 0, 0, 1);
    }

    protected void applyAOE(Vec3 position, @Nullable Entity resultEntity, ServerLevel level) {
        double aoeRadius = getProperties().aoeRadius();
        if (aoeRadius > 0) {
            AABB aoeBox = new AABB(
                    position.x - aoeRadius, position.y - aoeRadius, position.z - aoeRadius,
                    position.x + aoeRadius, position.y + aoeRadius, position.z + aoeRadius
            ).inflate(1);
            List<Entity> entities = level.getEntities(this, aoeBox, entity -> {
                if (entity == resultEntity || entity == getOwner()) {
                    return false;
                }
                if (entity.distanceToSqr(position) > aoeRadius * aoeRadius) {
                    return false;
                }
                return canAttack(entity);
            });
            for (Entity entity : entities) {
                hurtEntity(entity, level, getProperties().aoeDamageMul() * (float) Math.max(0, (aoeRadius - Math.sqrt(entity.distanceToSqr(position))) / aoeRadius));
            }
            addExplosionParticle(level, position);
        }
    }

    protected void hurtEntity(Entity entity, ServerLevel level, float damageMul) {
        DamageSource damageSource = getOwner() == null ? ModDamageSources.orbitingBlock(this) : ModDamageSources.indirectOrbitingBlock(this, getOwner());
        float amount = getProperties().baseDamage() + getProperties().type().getDamageBonus();
        if (!getProperties().firedFromWeapon().isEmpty()) {
            amount = EnchantmentHelper.modifyDamage(level, getProperties().firedFromWeapon(), entity, damageSource, amount);
            applyAngelTomPassive2(level, getProperties().firedFromWeapon(), entity, damageSource);
        }
        entity.hurtServer(level, damageSource, amount * damageMul);
    }

    protected void applyAngelTomPassive2(ServerLevel level, ItemStack stack, Entity entity, DamageSource damageSource) {
        long p2Interval = (long) getEnchantmentValue(ModEnchantmentEffectComponents.ANGEL_TOM_PASSIVE2_INTERVAL.get(), level, stack, entity, damageSource);
        int p2healCount = (int) getEnchantmentValue(ModEnchantmentEffectComponents.ANGEL_TOM_PASSIVE2_HEAL_COUNT.get(), level, stack, entity, damageSource);
        float p2healAmount = getEnchantmentValue(ModEnchantmentEffectComponents.ANGEL_TOM_PASSIVE2_HEAL_AMOUNT.get(), level, stack, entity, damageSource);
        if (p2Interval > 0 && p2healCount > 0 && p2healAmount > 0) {
            Entity owner = getOwner() == null ? damageSource.getEntity() : getOwner();
            if (owner instanceof LivingEntity livingOwner) {
                AngelTomPassive2Owner.resetAttributes(livingOwner, p2Interval, p2healCount, p2healAmount);
                AngelTomPassive2Owner.mayTriggerPassive2(livingOwner);
            }
        }
    }

    private static float getEnchantmentValue(DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type, ServerLevel level, ItemStack stack, Entity entity, DamageSource damageSource) {
        MutableFloat value = new MutableFloat(0);
        EnchantmentHelper.runIterationOnItem(
                stack, (enchantment, enchantmentLevel) -> enchantment.value().modifyDamageFilteredValue(type,
                        level,
                        enchantmentLevel,
                        stack,
                        entity,
                        damageSource,
                        value)
        );
        return value.floatValue();
    }

    @Override
    protected void updateRotation() {
        Vec3 deltaMovement = getDeltaMovement();
        double horizontalDistance = deltaMovement.horizontalDistance();
        setXRot(lerpRotation(xRotO, (float) (Mth.atan2(deltaMovement.y, horizontalDistance) * 180 / Math.PI)));
        setYRot(lerpRotation(yRotO, (float) (Mth.atan2(deltaMovement.x, deltaMovement.z) * 180 / Math.PI)));
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key == DATA_BLOCK_STATE) {
            refreshDimensions();
        }
    }

    protected void applyInertia() {
        Vec3 deltaMovement = getDeltaMovement();
        Vec3 position = position();
        double movementScale;
        if (isInWater()) {
            for (int i = 0; i < 4; i++) {
                level().addParticle(ParticleTypes.BUBBLE, position.x - deltaMovement.x * 0.25, position.y - deltaMovement.y * 0.25, position.z - deltaMovement.z * 0.25, deltaMovement.x, deltaMovement.y, deltaMovement.z);
            }
            movementScale = HarpConstants.ORBITING_BLOCK_INERTIA_WATER;
        } else {
            movementScale = HarpConstants.ORBITING_BLOCK_INERTIA;
        }
        if (getProperties().noGravity()) {
            return;
        }
        movementScale += getProperties().type().getInertiaBonus();
        setDeltaMovement(deltaMovement.scale(movementScale));
    }

    @Nullable
    protected Entity findTarget(boolean refresh) {
        Entity target = getPresetTarget();
        if (target != null) {
            return invalidTarget(target) ? null : target;
        }
        target = getTarget(); // Actively found target
        if (!refresh && target != null && !invalidTarget(target)) {
            return target;
        }
        if (!getProperties().canActivelySeek()) {
            return null;
        }
        double k = life < 5 ? 0.6 : (life < 10 ? 0.85 : 1);
        double seekingRange = getProperties().seekingRange() * Math.min(1, getDeltaMovement().length() * k * Math.min(life, 20) / 20.0);
        List<Entity> possibleEntities = level().getEntities(this, getBoundingBox().inflate(seekingRange), entity -> canAttack(entity) && distanceToSqr(entity) <= seekingRange * seekingRange);
        if (possibleEntities.isEmpty()) {
            setCurrentTarget(null);
            return null;
        }
        List<Entity> possibleMonsters = possibleEntities.stream()
                .filter(entity -> entity instanceof LivingEntity)
                .filter(entity -> entity instanceof Enemy)
                .sorted(Comparator.comparingDouble(this::getSeekingPriority))
                .toList();
        if (!possibleMonsters.isEmpty()) {
            target = possibleMonsters.getFirst();
            setCurrentTarget(target);
            return target;
        }
        possibleEntities.removeAll(possibleMonsters);
        List<Entity> crystals = possibleEntities.stream()
                .filter(entity -> entity instanceof EndCrystal)
                .sorted(Comparator.comparingDouble(this::getSeekingPriority))
                .toList();
        if (!crystals.isEmpty()) {
            target = crystals.getFirst();
            setCurrentTarget(target);
            return target;
        }
        possibleEntities.removeAll(crystals);
        possibleEntities.sort(Comparator.comparingDouble(this::getSeekingPriority));
        target = possibleEntities.isEmpty() ? null : possibleEntities.getFirst();
        setCurrentTarget(target);
        return target;
    }

    protected double getSeekingPriority(Entity entity) {
        double dx = entity.getX() - getX();
        double dy = entity.getY(0.5) - getY(0.5);
        double dz = entity.getZ() - getZ();
        Vec3 vecToTarget = new Vec3(dx, dy, dz);
        double v = vecToTarget.dot(getDeltaMovement()) / vecToTarget.length() / getDeltaMovement().length();
        double k = 1, b = 0;
        if (v < 0) {
            v *= 3;
            b += 2;
        } else if (v <= 0.5) {
            v = 2 * v * v;
            b += 1;
        }
        if (entity instanceof OwnableEntity ownableEntity && getOwner() != null && ownableEntity.getOwner() != null && ownableEntity.getOwner().is(getOwner())) {
            k = 10;
            b += 7;
        }
        return b + distanceTo(entity) * (1 - v) * k;
    }

    protected static boolean invalidTarget(Entity entity) {
        return !EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity) || !EntitySelector.ENTITY_STILL_ALIVE.test(entity);
    }

    protected boolean canAttack(Entity entity) {
        if (invalidTarget(entity)) {
            return false;
        }
        if (entity == getOwner()) {
            return false;
        }
        if (entity instanceof EnderMan) {
            return false;
        }
        return entity instanceof EndCrystal || entity instanceof LivingEntity;
    }

    protected void trySeek(Entity target) {
        Vec3 deltaMovement = getDeltaMovement();
        double dx = target.getX() - getX();
        double dy = target.getY(0.5) - getY(0.5);
        double dz = target.getZ() - getZ();
        double movementLen = deltaMovement.length();
        Vec3 vecToTarget = new Vec3(dx, dy, dz).normalize().scale(movementLen);
        double seekPower = Mth.clamp(getProperties().seekPower() * movementLen / 2, 0, 1);
        Vec3 newDirection = deltaMovement.scale(1 - seekPower).add(vecToTarget.scale(seekPower));
        setDeltaMovement(newDirection.normalize().scale(movementLen));
    }

    private void handleFirstTickBubbleColumn() {
        if (firstTick) {
            for (BlockPos pos : BlockPos.betweenClosed(getBoundingBox())) {
                BlockState state = level().getBlockState(pos);
                if (state.is(Blocks.BUBBLE_COLUMN)) {
                    state.entityInside(level(), pos, this, InsideBlockEffectApplier.NOOP, true);
                }
            }
        }
    }

    @Override
    protected double getDefaultGravity() {
        if (getProperties().noGravity()) {
            return 0;
        }
        double gravity = HarpConstants.ORBITING_BLOCK_GRAVITY + getProperties().type().getGravityBonus();
        if (canSeek()) {
            gravity *= HarpConstants.SEEK_VELOCITY_MULTIPLIER;
        }
        return gravity;
    }

    public void setBlockState(BlockState state) {
        getEntityData().set(DATA_BLOCK_STATE, state);
        refreshDimensions();
    }

    public BlockState getBlockState() {
        return getEntityData().get(DATA_BLOCK_STATE);
    }

    @Nullable
    public Entity getTarget() {
        if (level().isClientSide() || presetTarget == null) {
            return entityData.get(DATA_CURRENT_TARGET).map(ref -> EntityReference.getEntity(ref, level())).orElse(null);
        }
        return getPresetTarget();
    }

    @Nullable
    public Entity getPresetTarget() {
        return EntityReference.getEntity(presetTarget, level());
    }

    public void setPresetTarget(@Nullable Entity target) {
        presetTarget = EntityReference.of(target);
        setCurrentTarget(target);
    }

    public void setCurrentTarget(@Nullable Entity target) {
        entityData.set(DATA_CURRENT_TARGET, Optional.ofNullable(EntityReference.of(target)));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(LIFE_TAG, life);
        output.store(BLOCK_STATE_TAG, BlockState.CODEC, getBlockState());
        output.store(PROPERTIES_TAG, Properties.CODEC, getProperties());
        if (presetTarget != null) {
            output.store(PRESET_TARGET_TAG, UUIDUtil.CODEC, presetTarget.getUUID());
        }
        entityData.get(DATA_CURRENT_TARGET).ifPresent(ref -> output.store(TARGET_TAG, UUIDUtil.CODEC, ref.getUUID()));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        life = input.getIntOr(LIFE_TAG, 0);
        setBlockState(input.read(BLOCK_STATE_TAG, BlockState.CODEC).orElseGet(Blocks.AIR::defaultBlockState));
        setProperties(input.read(PROPERTIES_TAG, Properties.CODEC).orElse(Properties.DEFAULT));
        presetTarget = EntityReference.read(input, PRESET_TARGET_TAG);
        entityData.set(DATA_CURRENT_TARGET, Optional.ofNullable(EntityReference.read(input, TARGET_TAG)));
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(getBlockState().getBlock().asItem());
    }

    public Properties getProperties() {
        return entityData.get(DATA_PROPERTIES);
    }

    public void setProperties(Properties properties) {
        entityData.set(DATA_PROPERTIES, properties);
    }

    public record Properties(float baseDamage, float size, boolean noGravity, double seekingRange, double seekPower, float aoeDamageMul, double aoeRadius, BlockType type, ItemStack firedFromWeapon) {
        public static final Codec<Properties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("base_damage").forGetter(Properties::baseDamage),
                Codec.FLOAT.fieldOf("size").forGetter(Properties::size),
                Codec.BOOL.fieldOf("no_gravity").forGetter(Properties::noGravity),
                Codec.DOUBLE.fieldOf("seeking_range").forGetter(Properties::seekingRange),
                Codec.DOUBLE.fieldOf("seek_power").forGetter(Properties::seekPower),
                Codec.FLOAT.fieldOf("aoe_damage").forGetter(Properties::aoeDamageMul),
                Codec.DOUBLE.fieldOf("aoe_radius").forGetter(Properties::aoeRadius),
                BlockType.CODEC.fieldOf("type").forGetter(Properties::type),
                ItemStack.OPTIONAL_CODEC.fieldOf("fired_from_weapon").forGetter(Properties::firedFromWeapon))
                .apply(instance, Properties::new)
        );

        public static final Properties DEFAULT = new Properties(HarpConstants.BASE_ORBITING_BLOCK_DAMAGE,
                HarpConstants.DEFAULT_ORBITING_BLOCK_SIZE,
                false,
                0,
                0,
                0,
                0,
                BlockType.DEFAULT,
                ItemStack.EMPTY);
        public static final StreamCodec<RegistryFriendlyByteBuf, Properties> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT, Properties::baseDamage,
                ByteBufCodecs.FLOAT, Properties::size,
                ByteBufCodecs.BOOL, Properties::noGravity,
                ByteBufCodecs.DOUBLE, Properties::seekingRange,
                ByteBufCodecs.DOUBLE, Properties::seekPower,
                ByteBufCodecs.FLOAT, Properties::aoeDamageMul,
                ByteBufCodecs.DOUBLE, Properties::aoeRadius,
                BlockType.STREAM_CODEC, Properties::type,
                ItemStack.STREAM_CODEC, Properties::firedFromWeapon,
                Properties::new
        );

        public boolean canActivelySeek() {
            return seekingRange > 0;
        }

        @Override
        public double seekPower() {
            return Mth.clamp(seekPower, 0, 1);
        }
    }

    public enum BlockType implements Predicate<BlockState>, StringRepresentable {
        TNT(1, "tnt",
                10,
                0,
                0,
                List.of(ModBlockTags.EXPLOSIVE_ORBITING_BLOCK_TAG_SPECIAL),
                p -> p.or(state -> state.getBlock() instanceof TntBlock)),
        HEAVY(2, "heavy",
                6,
                0.07,
                0,
                List.of(ModBlockTags.HEAVY_ORBITING_BLOCK_TAG_SPECIAL, BlockTags.MINEABLE_WITH_PICKAXE),
                p -> p.and(state -> state.getBlock().defaultDestroyTime() >= HarpConstants.HEAVY_BLOCK_MIN_HARDNESS)),
        HARD(3, "hard",
                2,
                0.01,
                0,
                List.of(ModBlockTags.HARD_ORBITING_BLOCK_TAG_SPECIAL, BlockTags.MINEABLE_WITH_PICKAXE)),
        SOFT(4, "soft",
                -1,
                -0.005,
                -0.05,
                List.of(ModBlockTags.SOFT_ORBITING_BLOCK_TAG_SPECIAL, BlockTags.MINEABLE_WITH_SHOVEL, BlockTags.MINEABLE_WITH_HOE,
                        BlockTags.LEAVES, BlockTags.WOOL, BlockTags.WOOL_CARPETS),
                p -> p.or(state -> state.getBlock().defaultDestroyTime() == 0)),
        DEFAULT(0, "default", 0, 0, 0, List.of(), p -> Predicates.alwaysTrue());

        public static final StringRepresentable.EnumCodec<BlockType> CODEC = StringRepresentable.fromEnum(BlockType::values);
        private static final List<BlockType> TEST_ORDER = List.of(TNT, HEAVY, HARD, SOFT);
        private static final IntFunction<BlockType> BY_ID = ByIdMap.continuous(BlockType::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, BlockType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, BlockType::getId);
        private final int id;
        private final String name;
        private final float damageBonus;
        private final double gravityBonus;
        private final double inertiaBonus;
        private final Predicate<? super BlockState> predicate;

        BlockType(int id, String name, float damageBonus, double gravityBonus, double inertiaBonus, List<TagKey<Block>> tags) {
            this(id, name, damageBonus, inertiaBonus, gravityBonus, tags, Function.identity());
        }

        BlockType(int id, String name, float damageBonus, double gravityBonus, double inertiaBonus, List<TagKey<Block>> tags, Function<? super Predicate<BlockState>, ? extends Predicate<? super BlockState>> additionalPredicate) {
            this.id = id;
            this.name = name;
            this.damageBonus = damageBonus;
            this.gravityBonus = gravityBonus;
            this.inertiaBonus = inertiaBonus;
            Predicate<BlockState> tagPredicate = state -> tags.stream().anyMatch(state::is);
            this.predicate = additionalPredicate.apply(tagPredicate);
        }

        public static BlockType getTypeFor(BlockState state) {
            for (BlockType type : TEST_ORDER) {
                if (type.test(state)) {
                    return type;
                }
            }
            return DEFAULT;
        }

        public int getId() {
            return id;
        }

        public static BlockType byId(int id) {
            return BY_ID.apply(id);
        }

        public float getDamageBonus() {
            return damageBonus;
        }

        public double getGravityBonus() {
            return gravityBonus;
        }

        public double getInertiaBonus() {
            return inertiaBonus;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        @Override
        public boolean test(BlockState state) {
            return predicate.test(state);
        }
    }
}
