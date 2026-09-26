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

import illusnow.tjchase.item.ModDataComponents;
import illusnow.tjchase.network.bidirectional.SyncInGameEntityPayload;
import illusnow.tjchase.network.s2c.OpenGameplayObjectEditScreenForInGameEntityPayload;
import illusnow.tjchase.world.gameplay.object.GameplayObject;
import illusnow.tjchase.world.gameplay.object.GameplayObjectType;
import illusnow.tjchase.world.gameplay.object.Template;
import illusnow.tjchase.world.gameplay.object.editablevalue.EditableValue;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public abstract class InGamePlacedEntity<O extends InGamePlacedEntity<O>> extends Entity implements GameplayObject<GameplayObjectType<O>> {
    private static final String TEMPLATE_TAG = "Template";
    private static final EntityDataAccessor<Boolean> DATA_TEMPLATE = SynchedEntityData.defineId(InGamePlacedEntity.class, EntityDataSerializers.BOOLEAN);
    private final InterpolationHandler interpolation = new InterpolationHandler(this, 3);
    private int cleaningPreparationTicks;

    protected InGamePlacedEntity(EntityType<? extends InGamePlacedEntity> entityType, Level level) {
        super(entityType, level);
        getGameplayObjectType().getEditableValues().forEach(this::setDefault);
    }

    @SuppressWarnings("unchecked")
    private <T> void setDefault(EditableValue<T, O> editableValue) {
        editableValue.setDefault((O) this);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TEMPLATE, false);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        if (isInvulnerableToBase(damageSource)) {
            return false;
        }
        if (damageSource.getEntity() instanceof ServerPlayer player) {
            prepareCleaning(player.getMainHandItem(), player, level);
        }
        markHurt();
        gameEvent(GameEvent.ENTITY_DAMAGE, damageSource.getEntity());
        return true;
    }

    @Override
    public void kill(ServerLevel level) {}

    public void clean(ServerLevel level) {
        super.kill(level);
    }

    public void prepareCleaning(ItemStack stack, ServerPlayer player, ServerLevel level) {
        if (!validItem(stack)) {
            return;
        }
        if (cleaningPreparationTicks > 0) {
            clean(level);
        }
        cleaningPreparationTicks = 7;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        InteractionResult result = super.interact(player, hand);
        if (result != InteractionResult.PASS) {
            return result;
        } else {
            result = createInteractionResult(player, hand);
            if (result.consumesAction()) {
                gameEvent(GameEvent.ENTITY_INTERACT, player);
                return result;
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return super.getPickResult();
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isValid() {
        return !isRemoved();
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }

    @Override
    public CustomPacketPayload createServerboundPayload(Template<?> template, String name) {
        return new SyncInGameEntityPayload(getId(), name, template);
    }

    protected InteractionResult createInteractionResult(Player player, InteractionHand hand) {
        if (!player.level().isClientSide() && hand == InteractionHand.MAIN_HAND) {
            ItemStack stack = player.getItemInHand(hand);
            if (validItem(stack) ){
                if (player.isShiftKeyDown()) {
                    Template<?> template = createTemplate();
                    discard();
                    stack.set(ModDataComponents.GAMEPLAY_OBJECT_NAME.get(), hasCustomName() ? getCustomName() : null);
                    stack.set(ModDataComponents.CARRYING_TEMPLATE.get(), template);
                } else {
                    PacketDistributor.sendToPlayer((ServerPlayer) player,
                            new SyncInGameEntityPayload(getId(), "", createTemplate()),
                            new OpenGameplayObjectEditScreenForInGameEntityPayload(getId()));
                }
                return InteractionResult.SUCCESS_SERVER;
            }
        }
        return InteractionResult.PASS;
    }

    public void onPlaced() {}

    public abstract boolean validItem(ItemStack stack);

    @SuppressWarnings("unchecked")
    public Template<O> createTemplate() {
        return GameplayObject.saveAsTemplate((O) this);
    }

    @SuppressWarnings("unchecked")
    public void loadFromTemplate(Template<?> template) {
        try {
            GameplayObject.loadFromTemplate((O) this, (Template<O>) template);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Template type does not match entity type", e);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            handleMovements();
        }
        interpolation.interpolate();
        if (cleaningPreparationTicks > 0) {
            cleaningPreparationTicks--;
        }
    }

    @Override
    public InterpolationHandler getInterpolation() {
        return interpolation;
    }

    protected void handleMovements() {
        applyGravity();
        applyInertia();
        move(MoverType.SELF, getDeltaMovement());
        applyEffectsFromBlocks();
        handlePortal();
        if (!onGround() || getDeltaMovement().lengthSqr() > 1E-5) {
            needsSync = true;
        }
    }

    protected void applyInertia() {
        setDeltaMovement(getDeltaMovement().scale(0.95));
        if (getDeltaMovement().lengthSqr() <= 1E-6) {
            setDeltaMovement(Vec3.ZERO);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        getGameplayObjectType().deserialize(input, (O) this);
        setTemplate(input.getBooleanOr(TEMPLATE_TAG, false));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        getGameplayObjectType().serialize(output, (O) this);
        output.putBoolean(TEMPLATE_TAG, isTemplate());
    }

    @Override
    protected double getDefaultGravity() {
        return 0.08;
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt lightning) {}

    @Override
    public boolean shouldShowName() {
        return false;
    }

    @Override
    public boolean isTemplate() {
        return entityData.get(DATA_TEMPLATE);
    }

    public void setTemplate(boolean template) {
        entityData.set(DATA_TEMPLATE, template);
    }
}
