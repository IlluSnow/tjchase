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

package illusnow.tjchase.entity.controllable;

import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.network.UpdateControlledEntityPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public interface Controllable {
    static boolean control(Player player, @Nullable Controllable toControl) {
        if (player.level().isClientSide()) {
            throw new IllegalStateException("Wrong side");
        }
        if (toControl != null && !toControl.canStartToBeControlledBy(player)) {
            return false;
        }
        Controllable controlling = player.getData(ModAttachments.CONTROLLING_ENTITY).asControllable(player.level());
        if (controlling == toControl) {
            return false;
        }
        player.setData(ModAttachments.CONTROLLING_ENTITY, toControl == null ? Holder.EMPTY : of(toControl.getSelfAsEntity()));
        if (toControl != null) {
            toControl.setPlayerController(player);
            toControl.startBeingControlled((ServerPlayer) player);
        }
        if (controlling != null) {
            controlling.stopBeingControlled((ServerPlayer) player);
            controlling.setPlayerController(null);
        }
        return true;
    }

    static Holder of(@Nullable LivingEntity entity) {
        return Holder.create(EntityReference.of(entity));
    }

    @Nullable
    static Controllable getControllingMob(Player player) {
        return player.getData(ModAttachments.CONTROLLING_ENTITY).asControllable(player.level());
    }

    static void checkCanContinueToControl(Player player) {
        Controllable controllingMob = getControllingMob(player);
        if (controllingMob != null) {
            Player controller = controllingMob.getPlayerController();
            if (controller != null && controller != player) {
                player.setData(ModAttachments.CONTROLLING_ENTITY, Holder.EMPTY);
            } else if (!controllingMob.canContinueToBeControlledBy(player)) {
                control(player, null);
            }
        }
    }

    default boolean isControllingPlayerValid() {
        Player player = getPlayerController();
        return player != null && player.isAlive();
    }

    default boolean canMoveFreely() {
        return !isControllingPlayerValid();
    }

    default LivingEntity getSelfAsEntity() {
        return (LivingEntity) this;
    }

    default void handleBodyRotationIfBeingControlled(float yBodyRot, float maxHeadRotationRelativeToBody) {
        LivingEntity self = getSelfAsEntity();
        float oldNewDelta = Mth.wrapDegrees(yBodyRot - self.yBodyRot);
        self.yBodyRot += oldNewDelta * 0.3F;
        float mainBodyDelta = Mth.wrapDegrees(self.getYRot() - self.yBodyRot);
        if (Math.abs(mainBodyDelta) > maxHeadRotationRelativeToBody) {
            self.yBodyRot = self.yBodyRot + (mainBodyDelta - Mth.sign(mainBodyDelta) * maxHeadRotationRelativeToBody);
        }
    }

    @Nullable
    EntityReference<Player> getControllerRef();

    void setControllerRef(@Nullable EntityReference<Player> controllerRef);

    @Nullable
    default Player getPlayerController() {
        return EntityReference.getPlayer(getControllerRef(), getSelfAsEntity().level());
    }

    default void setPlayerController(@Nullable Player player) {
        setControllerRef(EntityReference.of(player));
    }

    default void startBeingControlled(ServerPlayer controller) {
        PacketDistributor.sendToPlayer(controller, new UpdateControlledEntityPayload(getSelfAsEntity().getId(), true));
        getSelfAsEntity().stopRiding();
    }

    default void stopBeingControlled(ServerPlayer controller) {
        PacketDistributor.sendToPlayer(controller, new UpdateControlledEntityPayload(getSelfAsEntity().getId(), false));
    }

    default void reloadControlledEntity(ServerPlayer controller) {}

    default boolean canStartToBeControlledBy(Player player) {
        Player controller = getPlayerController();
        if (controller == null || controller == player) {
            return true;
        }
        return !isControllingPlayerValid();
    }

    default boolean canContinueToBeControlledBy(@Nullable Player player) {
        if (!getSelfAsEntity().isAlive() || player == null || !player.isAlive()) {
            return false;
        }
        if (player.isSpectator()) {
            return false;
        }
        return getSelfAsEntity().level() == player.level();
    }

    class Holder {
        public static final Holder EMPTY = new Holder(null);
        @Nullable
        private final EntityReference<LivingEntity> entityRef;

        private Holder(@Nullable EntityReference<LivingEntity> ref) {
            this.entityRef = ref;
        }

        public static Holder create(@Nullable EntityReference<LivingEntity> ref) {
            return ref == null ? EMPTY : new Holder(ref);
        }

        @Nullable
        public EntityReference<LivingEntity> getEntityRef() {
            return entityRef;
        }

        @Nullable
        public Controllable asControllable(Level level) {
            if (entityRef == null) {
                return null;
            }
            LivingEntity entity = EntityReference.getLivingEntity(entityRef, level);
            if (entity instanceof Controllable controllable) {
                return controllable;
            }
            return null;
        }

        public enum Serializer implements IAttachmentSerializer<Holder> {
            INSTANCE;

            private static final String TAG = "ControllableHolder";

            @Override
            public Holder read(IAttachmentHolder holder, ValueInput input) {
                EntityReference<LivingEntity> ref = EntityReference.read(input, TAG);
                return create(ref);
            }

            @Override
            public boolean write(Holder attachment, ValueOutput output) {
                EntityReference.store(attachment.getEntityRef(), output, TAG);
                return true;
            }
        }

        public enum Syncer implements AttachmentSyncHandler<Holder> {
            INSTANCE;

            @Override
            public void write(RegistryFriendlyByteBuf buf, Holder attachment, boolean initialSync) {
                buf.writeBoolean(attachment.getEntityRef() != null);
                if (attachment.getEntityRef() != null) {
                    buf.writeUUID(attachment.getEntityRef().getUUID());
                }
            }

            @Override
            public Holder read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @org.jspecify.annotations.Nullable Holder previousValue) {
                if (!buf.readBoolean()) {
                    return EMPTY;
                }
                EntityReference<LivingEntity> ref = EntityReference.of(buf.readUUID());
                return create(ref);
            }
        }
    }
}
