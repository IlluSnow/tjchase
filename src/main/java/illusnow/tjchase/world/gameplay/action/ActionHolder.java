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

package illusnow.tjchase.world.gameplay.action;

import com.google.common.base.MoreObjects;
import com.mojang.logging.LogUtils;
import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.network.s2c.UpdateActionPayload;
import illusnow.tjchase.util.ModRegistries;
import illusnow.tjchase.util.Progress;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public class ActionHolder {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private Action currentAction;
    @Nullable
    private Progress currentProgress;
    private int tickCount;
    private boolean firstUpdate = true;

    public static ActionHolder get(Player player) {
        return player.getData(ModAttachments.ACTION_HOLDER);
    }

    public static void sync(Player player, NetworkOp op, @Nullable Action newAction, @Nullable Action oldAction) {
        player.syncData(ModAttachments.ACTION_HOLDER);
        PacketDistributor.sendToAllPlayers(new UpdateActionPayload(op, player.nameAndId(), Optional.ofNullable(newAction), Optional.ofNullable(oldAction)));
    }

    public static void updateBidirectionally(Player player) {
        get(player).update(player);
    }

    @Nullable
    public static Action getAction(Player player) {
        return get(player).getCurrentAction();
    }

    public static boolean setAction(Player player, @Nullable Action action) {
        ActionHolder holder = get(player);
        Action prevAction = holder.getCurrentAction();
        NetworkOp op = null;
        if (action != null) {
            if (holder.replaceCurrentAction(player, action)) {
                op = NetworkOp.SET_NEW;
            }
        } else {
            if (holder.interruptCurrentAction(player)) {
                op = NetworkOp.INTERRUPT;
            }
        }
        if (op != null) {
            sync(player, op, action, prevAction);
        }
        return op != null;
    }

    @Contract("_, null -> false")
    public static boolean stopAction(Player player, @Nullable Action action) {
        return stopActionIf(player, currentAction -> currentAction == action);
    }

    public static boolean stopActionIf(Player player, Predicate<? super Action> currentActionPredicate) {
        Action action = getAction(player);
        if (action != null && currentActionPredicate.test(action)) {
            return setAction(player, null);
        }
        return false;
    }

    @Nullable
    public Action getCurrentAction() {
        return currentAction;
    }

    public boolean replaceCurrentAction(Player player, Action newAction) {
        if (!newAction.canInterrupt(currentAction)) {
            return false;
        }
        interruptAndStop(player);
        currentAction = newAction;
        currentAction.start(player);
        return true;
    }

    public boolean interruptCurrentAction(Player player) {
        if (currentAction != null) {
            interruptAndStop(player, currentAction);
            currentAction = null;
            return true;
        }
        return false;
    }

    public boolean completeCurrentAction(Player player) {
        if (currentAction != null) {
            completeAndStop(player, currentAction);
            currentAction = null;
            return true;
        }
        return false;
    }

    public void update(Player player) {
        tickCount++;
        if (firstUpdate) {
            if (currentAction != null) {
                currentAction.reload(player);
            }
            firstUpdate = false;
        }
        if (currentAction != null) {
            currentAction.update(player, this);
        }
    }

    private void interruptAndStop(Player player) {
        if (currentAction != null) {
            interruptAndStop(player, currentAction);
        }
    }

    private static void interruptAndStop(Player player, Action action) {
        action.onInterrupt(player);
        action.stop(player);
    }

    private static void completeAndStop(Player player, Action action) {
        action.onComplete(player);
        action.stop(player);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("currentAction", currentAction)
                .add("tickCount", tickCount)
                .toString();
    }

    public enum NetworkOp {
        SET_NEW(0) {
            @Override
            public void handle(Player player, @Nullable Action newAction, @Nullable Action oldAction) {
                Objects.requireNonNull(newAction, "newAction cannot be null");
                if (oldAction != null) {
                    interruptAndStop(player, oldAction);
                }
                newAction.start(player);
            }
        },
        INTERRUPT(1) {
            @Override
            public void handle(Player player, @Nullable Action newAction, @Nullable Action oldAction) {
                Objects.requireNonNull(oldAction, "oldAction cannot be null");
                interruptAndStop(player, oldAction);
            }
        },
        COMPLETE(2) {
            @Override
            public void handle(Player player, @Nullable Action newAction, @Nullable Action oldAction) {
                Objects.requireNonNull(oldAction, "oldAction cannot be null");
                completeAndStop(player, oldAction);
            }
        };

        private final int id;

        private static final IntFunction<NetworkOp> BY_ID = ByIdMap.continuous(NetworkOp::getId, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, NetworkOp> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, NetworkOp::getId);

        NetworkOp(int id) {
            this.id = id;
        }

        public abstract void handle(Player player, @Nullable Action newAction, @Nullable Action oldAction);

        public int getId() {
            return id;
        }
    }

    public enum Serializer implements IAttachmentSerializer<ActionHolder> {
        INSTANCE;

        @Override
        public ActionHolder read(IAttachmentHolder holder, ValueInput input) {
            ActionHolder actionHolder = new ActionHolder();
            actionHolder.currentAction = input.read("CurrentAction", Action.CODEC).orElse(null);
            actionHolder.tickCount = input.getIntOr("TickCount", 0);
            return actionHolder;
        }

        @Override
        public boolean write(ActionHolder attachment, ValueOutput output) {
            if (attachment.currentAction != null) {
                output.store("CurrentAction", Action.CODEC, attachment.currentAction);
            }
            output.putInt("TickCount", attachment.tickCount);
            return true;
        }
    }

    public enum Syncer implements AttachmentSyncHandler<ActionHolder> {
        INSTANCE;

        @Override
        public void write(RegistryFriendlyByteBuf buf, ActionHolder attachment, boolean initialSync) {
            buf.writeBoolean(attachment.currentAction != null);
            if (attachment.currentAction != null) {
                int id = buf.registryAccess().lookupOrThrow(ModRegistries.ACTIONS_KEY).getId(attachment.currentAction);
                buf.writeVarInt(id);
            }
            buf.writeVarInt(attachment.tickCount);
        }

        @Override
        public ActionHolder read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable ActionHolder previousValue) {
            ActionHolder actionHolder = new ActionHolder();
            if (buf.readBoolean()) {
                int id = buf.readVarInt();
                actionHolder.currentAction = buf.registryAccess().lookupOrThrow(ModRegistries.ACTIONS_KEY).byId(id);
            }
            actionHolder.tickCount = buf.readVarInt();
            return actionHolder;
        }
    }
}
