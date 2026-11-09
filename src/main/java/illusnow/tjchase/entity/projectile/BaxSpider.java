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

package illusnow.tjchase.entity.projectile;

import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.entity.Cocoon;
import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.network.s2c.BaxSpiderDestroyBlockPayload;
import illusnow.tjchase.sound.ModSoundEvents;
import illusnow.tjchase.util.ChainHarvesting;
import illusnow.tjchase.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Comparator;
import java.util.function.Predicate;

public class BaxSpider extends ThrowableProjectile implements GeoEntity, Seekable {
    public static final RawAnimation AMBIENT = RawAnimation.begin().thenLoop("ambient");
    private static final EntityDataAccessor<Boolean> RETURNING = SynchedEntityData.defineId(BaxSpider.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> PREFERRED_SLOT = SynchedEntityData.defineId(BaxSpider.class, EntityDataSerializers.INT);
    private static final String RETURNING_TAG = "Returning";
    private static final String ITEM_FORM_TAG = "ItemForm";
    private static final String PREFERRED_SLOT_TAG = "PreferredSlot";
    private static final String LIFE_TAG = "Life";
    private static final int MAX_LIFE = 600;
    private static final int FORCE_BACK_LIFE = 200;
    private static final double FORCE_BACK_DISTANCE = 48;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private ItemStack itemForm = ItemStack.EMPTY;
    private int life;

    public BaxSpider(EntityType<? extends BaxSpider> type, Level level) {
        super(type, level);
    }

    public BaxSpider(Entity owner, Level level) {
        super(ModEntities.BAX_SPIDER.get(), owner.getX(), owner.getEyeY() - 0.1F, owner.getZ(), level);
        setOwner(owner);
        if (owner instanceof Player) {
            owner.getData(ModAttachments.FORWARDING_BAX_SPIDERS).add(EntityReference.of(this));
            owner.syncData(ModAttachments.FORWARDING_BAX_SPIDERS);
        }
        setRot(owner.getYRot(), owner.getXRot());
    }

    public BaxSpider(Level level, double x, double y, double z) {
        super(ModEntities.BAX_SPIDER.get(), x, y, z, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RETURNING, false);
        builder.define(PREFERRED_SLOT, -1);
    }

    @Override
    protected double getDefaultGravity() {
        return isReturning() ? 0 : 0.01;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("Ambient", state -> state.setAndContinue(AMBIENT)));
    }

    @Override
    public void tick() {
        super.tick();
        life++;
        if (!(getOwner() instanceof Player player) || !player.isAlive() || life > MAX_LIFE) {
            returned(getOwner());
            discard();
            return;
        }
        if (canSeek()) {
            Entity target = getTarget();
            if (target != null) {
                trySeek(target);
            }
        }
        if (isReturning()) {
            double dx = getX() - player.getX();
            double dy = getY() - player.getY(0.5);
            double dz = getZ() - player.getZ();
            if (dx * dx + dz * dz < 0.64 && Math.abs(dy) < player.getBbHeight() / 2) {
                returned(player);
                discard();
            }
        } else if (life > FORCE_BACK_LIFE || distanceToSqr(player) > FORCE_BACK_DISTANCE * FORCE_BACK_DISTANCE) {
            startReturning(null);
            setDeltaMovement(getDeltaMovement().scale(-1));
        }
    }

    @Override
    protected void updateRotation() {
        if (isReturning()) {
            Vec3 deltaMovement = getDeltaMovement().scale(-1);
            double horizontalDistance = deltaMovement.horizontalDistance();
            setXRot(lerpRotation(xRotO, (float) (Mth.atan2(deltaMovement.y, horizontalDistance) * 180.0F / (float)Math.PI)));
            setYRot(lerpRotation(yRotO, (float) (Mth.atan2(deltaMovement.x, deltaMovement.z) * 180.0F / (float)Math.PI)));
        } else {
            super.updateRotation();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (isReturning()) {
            return;
        }
        super.onHit(result);
    }

    @Override
    protected void applyInertia() {
        Vec3 movement = getDeltaMovement();
        Vec3 position = position();
        if (isInWater()) {
            for (int i = 0; i < 4; i++) {
                float offset = 0.25F;
                level().addParticle(ParticleTypes.BUBBLE, position.x - movement.x * offset, position.y - movement.y * offset, position.z - movement.z * offset, movement.x, movement.y, movement.z);
            }
        }
    }

    @Override
    protected ProjectileDeflection hitTargetOrDeflectSelf(HitResult result) {
        ProjectileDeflection projectileDeflection0 = super.hitTargetOrDeflectSelf(result);
        if (projectileDeflection0 != ProjectileDeflection.NONE) {
            return projectileDeflection0;
        }
        if (!isReturning() && deflect(ModProjectileDeflections.BAX_SPIDER_BOUNCE_BACK, null, owner, false)) {
            startReturning(result);
            return ModProjectileDeflections.BAX_SPIDER_BOUNCE_BACK;
        }
        return ProjectileDeflection.NONE;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean(RETURNING_TAG, isReturning());
        output.store(ITEM_FORM_TAG, ItemStack.CODEC, itemForm);
        output.putInt(PREFERRED_SLOT_TAG, getPreferredSlot());
        output.putInt(LIFE_TAG, life);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setReturning(input.getBooleanOr(RETURNING_TAG, false));
        noPhysics = isReturning();
        setItemForm(input.read(ITEM_FORM_TAG, ItemStack.CODEC).orElse(ItemStack.EMPTY));
        setPreferredSlot(input.getIntOr(PREFERRED_SLOT_TAG, -1));
        life = input.getIntOr(LIFE_TAG, 0);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    @Override
    public double getSeekPower() {
        return isReturning() ? 0.9 : 0;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 64 * 64;
    }

    @Nullable
    @Override
    public Entity getTarget() {
        return isReturning() ? getOwner() : null;
    }

    public void startReturning(@Nullable HitResult result) {
        if (level().isClientSide()) {
            return;
        }
        if (getOwner() instanceof ServerPlayer playerOwner) {
            if (result != null) {
                switch (result.getType()) {
                    case ENTITY -> {
                        EntityHitResult entityResult = (EntityHitResult) result;
                        Entity target = entityResult.getEntity();
                        if (!(target instanceof Mob mob)) {
                            break;
                        }
                        if (Cocoon.isConvertible(mob)) {
                            Cocoon cocoon = Cocoon.convert(mob);
                            if (cocoon != null) {
                                cocoon.spawnCobwebParticlesAroundSelf();
                                cocoon.playSound(ModSoundEvents.COCOON_CREATED.get(), 1, random.triangle(1, 0.2F));
                            }
                        }
                    }
                    case BLOCK -> {
                        BlockHitResult blockResult = (BlockHitResult) result;
                        BlockState blockState = level().getBlockState(blockResult.getBlockPos());
                        ChainHarvesting.AdjacencyType adjacencyType = null;
                        Predicate<BlockState> blockPredicate = state -> false;
                        if (blockState.is(Tags.Blocks.ORES)) {
                            adjacencyType = ChainHarvesting.AdjacencyType.PLANE;
                            blockPredicate = state -> state.is(blockState.getBlock());
                        } else if (blockState.is(Tags.Blocks.NATURAL_LOGS)) {
                            adjacencyType = ChainHarvesting.AdjacencyType.VERTEX;
                            blockPredicate = state -> state.is(blockState.getBlock());
                        }
                        if (adjacencyType != null) {
                            ChainHarvesting chainHarvesting = new ChainHarvesting(adjacencyType, level(), blockResult.getBlockPos(), blockPredicate);
                            ItemStack tool = findSuitableTool(playerOwner, level(), blockResult.getBlockPos(), blockState);
                            if (!tool.isEmpty()) {
                                for (BlockPos pos : chainHarvesting.getResult(Comparator.comparing(playerOwner.blockPosition()::distSqr))) {
                                    if (tool.isEmpty()) {
                                        break;
                                    }
                                    destroyBlock(pos, playerOwner, tool);
                                }
                            }
                        }
                    }
                }
            }
            if (playerOwner.getData(ModAttachments.FORWARDING_BAX_SPIDERS).remove(EntityReference.of(this))) {
                playerOwner.syncData(ModAttachments.FORWARDING_BAX_SPIDERS);
            }
        }
        setReturning(true);
        noPhysics = true;
        needsSync = true;
    }

    private static ItemStack findSuitableTool(Player player, Level level, BlockPos pos, BlockState state) {
        Inventory inventory = player.getInventory();
        if (isItemSuitableForBlock(player, level, inventory.getSelectedItem(), pos, state)) {
            return inventory.getSelectedItem();
        }
        if (isItemSuitableForBlock(player, level, inventory.getItem(Inventory.SLOT_OFFHAND), pos, state)) {
            return inventory.getItem(Inventory.SLOT_OFFHAND);
        }
        for (int i = 0; i < Inventory.SELECTION_SIZE; i++) {
            if (isItemSuitableForBlock(player, level, inventory.getItem(i), pos, state)) {
                return inventory.getItem(i);
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean isItemSuitableForBlock(Player player, Level level, ItemStack stack, BlockPos pos, BlockState state) {
        boolean hasCorrectTool = stack.isCorrectToolForDrops(state);
        PlayerEvent.HarvestCheck event = NeoForge.EVENT_BUS.post(new PlayerEvent.HarvestCheck(player, state, level, pos, hasCorrectTool));
        return event.canHarvest();
    }

    private static boolean spiderCausedDrop;

    /**
     * [VanillaCopy] With a custom tool
     * {@link net.minecraft.server.level.ServerPlayerGameMode#destroyBlock(BlockPos)}
     */
    private static boolean destroyBlock(BlockPos pos, ServerPlayer player, ItemStack tool) {
        ServerLevel level = player.level();
        BlockState state = level.getBlockState(pos);
        BlockEvent.BreakEvent event = CommonHooks.fireBlockBreak(level, player.gameMode.getGameModeForPlayer(), player, pos, state);
        if (event.isCanceled()) {
            return false;
        } else {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            Block block = state.getBlock();
            if (block instanceof GameMasterBlock && !player.canUseGameMasterBlocks()) {
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                return false;
            } else if (player.blockActionRestricted(level, pos, player.gameMode.getGameModeForPlayer())) {
                return false;
            } else {
                BlockState state1 = block.playerWillDestroy(level, pos, state, player);
                PacketDistributor.sendToPlayer(player, new BaxSpiderDestroyBlockPayload(pos, Block.getId(state1)));
                if (player.preventsBlockDrops()) {
                    removeBlock(pos, state1, player, false, player.getMainHandItem().copy());
                } else {
                    ItemStack toolCopy = tool.copy();
                    boolean canHarvest = isItemSuitableForBlock(player, level, toolCopy.copy(), pos, state1);
                    tool.mineBlock(level, state1, pos, player);
                    boolean flag = removeBlock(pos, state1, player, canHarvest, toolCopy.copy());
                    if (canHarvest && flag) {
                        spiderCausedDrop = true;
                        block.playerDestroy(level, player, pos, state1, blockEntity, toolCopy);
                        spiderCausedDrop = false;
                    }
                    if (tool.isEmpty() && !toolCopy.isEmpty()) {
                        EventHooks.onPlayerDestroyItem(player, toolCopy, InteractionHand.MAIN_HAND);
                    }
                }
                return true;
            }
        }
    }

    public static boolean isSpiderCausedDrop() {
        return spiderCausedDrop;
    }

    private static boolean removeBlock(BlockPos pos, BlockState state, Player player, boolean canHarvest, ItemStack toolStack) {
        boolean removed = state.onDestroyedByPlayer(player.level(), pos, player, toolStack, canHarvest, player.level().getFluidState(pos));
        if (removed) {
            state.getBlock().destroy(player.level(), pos, state);
        }
        return removed;
    }

    private void returned(@Nullable Entity owner) {
        if (!(owner instanceof Player player)) {
            return;
        }
        Utils.giveItem(player, getItemForm().copy(), getPreferredSlot());
        setItemForm(ItemStack.EMPTY);
        setPreferredSlot(-1);
    }

    public boolean isReturning() {
        return entityData.get(RETURNING);
    }

    public void setReturning(boolean returning) {
        entityData.set(RETURNING, returning);
    }

    public ItemStack getItemForm() {
        return itemForm;
    }

    public void setItemForm(ItemStack itemForm) {
        this.itemForm = itemForm;
    }

    public boolean isOffhandThrown() {
        return getPreferredSlot() == Inventory.SLOT_OFFHAND;
    }

    public int getPreferredSlot() {
        return entityData.get(PREFERRED_SLOT);
    }

    public void setPreferredSlot(int preferredSlot) {
        entityData.set(PREFERRED_SLOT, preferredSlot);
    }
}
