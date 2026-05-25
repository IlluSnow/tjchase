package illusnow.tjchase.entity.projectile;

import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.util.HarpConstants;
import illusnow.tjchase.util.OrbitingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

public class OrbitingBlockEntity extends Projectile implements ItemSupplier {
    private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE = SynchedEntityData.defineId(
            OrbitingBlockEntity.class, EntityDataSerializers.BLOCK_STATE
    );
    private static final String BLOCK_STATE_TAG = "BlockState";

    public OrbitingBlockEntity(EntityType<? extends OrbitingBlockEntity> type, Level level) {
        super(type, level);
    }

    public OrbitingBlockEntity(Entity owner, OrbitingBlock block) {
        super(ModEntities.ORBITING_BLOCK.get(), owner.level());
        setOwner(owner);
        setPos(block.calculateWorldPos(owner, owner.getYRot()).add(0, -HarpConstants.ORBITING_BLOCK_HEIGHT_MUL / 2, 0));
        setYRot(block.getYRot());
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
    }

    @Override
    public void tick() {
        handleFirstTickBubbleColumn();
        applyGravity();
        applyInertia();
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

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if (!getBlockState().isAir()) {
            AABB bounds = getBlockState().getShape(level(), blockPosition()).bounds();
            float width = (float) ((bounds.maxX - bounds.minX) + (bounds.maxZ - bounds.minZ)) / 2;
            float height = (float) (bounds.maxY - bounds.minY);
            return EntityDimensions.scalable(width, height).scale(HarpConstants.ORBITING_BLOCK_SIZE);
        }
        return super.getDimensions(pose);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel level) {
            double x = getX();
            double y = getY();
            double z = getZ();
            BlockPos pos = BlockPos.containing(result.getLocation());
            double fallPower = Math.max(0, getDeltaMovement().scale(5).length());
            addBreakEffects(level, getBlockState(), pos, this, fallPower, x, y, z);
            discard();
        }
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

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level() instanceof ServerLevel level) {
            // TODO: Update damage source
            DamageSource damageSource = getOwner() == null ? damageSources().fallingBlock(this) : damageSources().fallingBlock(getOwner());
            result.getEntity().hurtServer(level, damageSource, 5);
        }
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
        setDeltaMovement(deltaMovement.scale(movementScale));
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
        return 0.03;
    }

    public void setBlockState(BlockState state) {
        getEntityData().set(DATA_BLOCK_STATE, state);
        refreshDimensions();
    }

    public BlockState getBlockState() {
        return getEntityData().get(DATA_BLOCK_STATE);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store(BLOCK_STATE_TAG, BlockState.CODEC, getBlockState());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setBlockState(input.read(BLOCK_STATE_TAG, BlockState.CODEC).orElseGet(Blocks.AIR::defaultBlockState));
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(getBlockState().getBlock().asItem());
    }
}
