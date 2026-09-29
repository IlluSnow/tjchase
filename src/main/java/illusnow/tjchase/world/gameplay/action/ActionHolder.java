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
    private ContinuousAction prevAction; // Synced manually
    private long tickCount;
    private boolean firstUpdate = true;
    // Serverside non-persistent data for OneTimeActions
    @Nullable
    private Progress currentProgress;
    @Nullable
    private ActionData data;

    public static ActionHolder get(Player player) {
        return player.getData(ModAttachments.ACTION_HOLDER);
    }

    public static void sync(Player player, NetworkOp op, @Nullable Action newAction, @Nullable Action oldAction, float speed) {
        player.syncData(ModAttachments.ACTION_HOLDER);
        PacketDistributor.sendToAllPlayers(new UpdateActionPayload(op, player.nameAndId(), Optional.ofNullable(newAction), Optional.ofNullable(oldAction), speed));
    }

    public static void updateBidirectionally(Player player) {
        get(player).update(player);
    }

    @Nullable
    public static Action getAction(Player player) {
        return get(player).getCurrentAction();
    }

    public static boolean setAction(Player player, @Nullable Action action) {
        return setAction(player, action, null);
    }

    public static boolean setAction(Player player, @Nullable Action action, @Nullable ActionData data) {
        ActionHolder holder = get(player);
        if (action instanceof OneTimeAction<?> oneTimeAction) {
            if (!oneTimeAction.isValidData(data)) {
                LOGGER.warn("Invalid action data {} for {}", data, oneTimeAction);
                return false;
            } else if (!oneTimeAction.canTrigger(player, holder, data)) {
                return false;
            }
        }
        Action prevAction = holder.getCurrentAction();
        NetworkOp op = null;
        if (action != null) {
            if (holder.replaceCurrentAction(player, action, data)) {
                if (action instanceof OneTimeAction<?> && prevAction instanceof ContinuousAction continuousPrevAction) {
                    holder.setPrevAction(continuousPrevAction);
                    op = NetworkOp.SET_NEW_SAVE_OLD;
                } else {
                    op = NetworkOp.SET_NEW;
                }
            }
        } else {
            boolean readStored = false;
            if (prevAction instanceof OneTimeAction<?> oneTimeAction) {
                readStored = oneTimeAction.readPreviousContinuousAction(player, holder, false);
            }
            if ((holder.prevAction == null || !readStored) ? holder.interruptCurrentAction(player) : holder.replaceCurrentAction(player, holder.prevAction, data)) {
                op = (holder.prevAction == null || !readStored) ? NetworkOp.INTERRUPT : NetworkOp.INTERRUPT_BACKTRACK;
            }
            if (readStored) {
                action = holder.prevAction;
            }
            holder.setPrevAction(null);
        }
        if (op != null) {
            float speed = 1;
            if (action instanceof OneTimeAction<?> oneTimeAction) {
                checkActionDataNonnull(data, oneTimeAction);
                speed = (float) data.duration() / oneTimeAction.defaultDuration();
            }
            sync(player, op, action, prevAction, speed);
        }
        return op != null;
    }

    @Contract("_, null -> false")
    public static boolean stopAction(Player player, @Nullable Action action) {
        return stopActionIf(player, currentAction -> currentAction == action);
    }

    @SuppressWarnings("ConstantValue")
    public static boolean stopAllActions(Player player) {
        return stopActionIf(player, Objects::nonNull);
    }

    @SuppressWarnings("DataFlowIssue")
    public static boolean stopActionIf(Player player, Predicate<? super Action> currentActionPredicate) {
        Action action = getAction(player);
        if (action != null && currentActionPredicate.test(action)) {
            int attempts = 0;
            do {
                setAction(player, null);
                action = getAction(player);
                attempts++;
                if (attempts >= 5) {
                    LOGGER.warn("Failed to actually stop the player's action, the condition may be invalid");
                    return false;
                }
            } while (currentActionPredicate.test(action));
            return true;
        }
        return false;
    }

    @Nullable
    public Action getCurrentAction() {
        return currentAction;
    }

    @Nullable
    public ContinuousAction getPrevAction() {
        return prevAction;
    }

    public void setPrevAction(@Nullable ContinuousAction prevAction) {
        this.prevAction = prevAction;
    }

    @Nullable
    public ActionData getActionData() {
        return data;
    }

    private void setActionDataDirectly(@Nullable ActionData data) {
        this.data = data;
    }

    private boolean replaceCurrentAction(Player player, Action newAction, @Nullable ActionData data) {
        if (newAction == currentAction || !newAction.canInterrupt(currentAction)) {
            return false;
        }
        interruptAndStop(player);
        currentAction = newAction;
        setActionDataDirectly(data);
        if (newAction instanceof OneTimeAction<?> oneTimeAction) {
            checkActionDataNonnull(data, oneTimeAction);
            currentProgress = Progress.createWithDuration(tickCount, data.duration());
        }
        currentAction.start(player, this);
        return true;
    }

    private static void checkActionDataNonnull(@Nullable ActionData data, OneTimeAction<?> oneTimeAction) {
        Objects.requireNonNull(data, "ActionData is null for OneTimeAction " + oneTimeAction);
    }

    private boolean interruptCurrentAction(Player player) {
        if (currentAction != null) {
            interruptAndStop(player, currentAction, this);
            currentAction = null;
            currentProgress = null;
            setActionDataDirectly(null);
            return true;
        }
        return false;
    }

    public boolean completeCurrentAction(Player player) {
        if (currentAction != null) {
            completeAndStop(player, currentAction, this);
            currentAction = null;
            currentProgress = null;
            setActionDataDirectly(null);
            return true;
        }
        return false;
    }

    private void update(Player player) {
        tickCount++;
        if (firstUpdate) {
            if (currentAction != null) {
                currentAction.reload(player, this);
            }
            firstUpdate = false;
        }
        if (currentAction != null) {
            currentAction.update(player, this);
        }
    }

    public Progress getProgress() {
        Objects.requireNonNull(currentProgress, "Attempting to get action progress but found null");
        return currentProgress;
    }

    public long getTickCount() {
        return tickCount;
    }

    private void interruptAndStop(Player player) {
        if (currentAction != null) {
            interruptAndStop(player, currentAction, this);
        }
    }

    private static void interruptAndStop(Player player, Action action, ActionHolder holder) {
        action.onInterrupt(player, holder);
        action.stop(player, holder);
    }

    private static void completeAndStop(Player player, Action action, ActionHolder holder) {
        action.onComplete(player, holder);
        action.stop(player, holder);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("currentAction", currentAction)
                .add("prevAction", prevAction)
                .add("tickCount", tickCount)
                .toString();
    }

    private static void checkAndSetPrevActionForSyncing(ActionHolder holder, @Nullable Action prevActionRead) {
        if (prevActionRead instanceof ContinuousAction prevContinuousActionRead) {
            holder.setPrevAction(prevContinuousActionRead);
        } else {
            LOGGER.warn("Failed to sync prevAction, {} is not a ContinuousAction", prevActionRead);
        }
    }

    public enum NetworkOp {
        SET_NEW(0) {
            @Override
            public void handle(Player player, @Nullable Action newAction, @Nullable Action oldAction) {
                Objects.requireNonNull(newAction, "newAction cannot be null");
                ActionHolder holder = getActionHolder(player);
                if (oldAction != null) {
                    interruptAndStop(player, oldAction, holder);
                }
                newAction.start(player, holder);
            }
        },
        INTERRUPT(1) {
            @Override
            public void handle(Player player, @Nullable Action newAction, @Nullable Action oldAction) {
                Objects.requireNonNull(oldAction, "oldAction cannot be null");
                ActionHolder holder = getActionHolder(player);
                interruptAndStop(player, oldAction, holder);
            }
        },
        COMPLETE(2) {
            @Override
            public void handle(Player player, @Nullable Action newAction, @Nullable Action oldAction) {
                Objects.requireNonNull(oldAction, "oldAction cannot be null");
                ActionHolder holder = getActionHolder(player);
                completeAndStop(player, oldAction, holder);
            }
        },
        SET_NEW_SAVE_OLD(3) {
            @Override
            public void handle(Player player, @Nullable Action newAction, @Nullable Action oldAction) {
                Objects.requireNonNull(newAction, "newAction cannot be null");
                Objects.requireNonNull(oldAction, "oldAction cannot be null");
                ActionHolder holder = getActionHolder(player);
                interruptAndStop(player, oldAction, holder);
                newAction.start(player, holder);
                checkAndSetPrevActionForSyncing(holder, oldAction);
            }
        },
        INTERRUPT_BACKTRACK(4) {
            @Override
            public void handle(Player player, @Nullable Action newAction, @Nullable Action oldAction) {
                Objects.requireNonNull(newAction, "newAction cannot be null");
                Objects.requireNonNull(oldAction, "oldAction cannot be null");
                ActionHolder holder = getActionHolder(player);
                interruptAndStop(player, oldAction, holder);
                newAction.start(player, holder);
                holder.setPrevAction(null);
            }
        },
        COMPLETE_BACKTRACK(5) {
            @Override
            public void handle(Player player, @Nullable Action newAction, @Nullable Action oldAction) {
                Objects.requireNonNull(oldAction, "oldAction cannot be null");
                ActionHolder holder = getActionHolder(player);
                completeAndStop(player, oldAction, holder);
                holder.setPrevAction(null);
            }
        };

        private final int id;

        private static final IntFunction<NetworkOp> BY_ID = ByIdMap.continuous(NetworkOp::getId, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, NetworkOp> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, NetworkOp::getId);

        NetworkOp(int id) {
            this.id = id;
        }

        public abstract void handle(Player player, @Nullable Action newAction, @Nullable Action oldAction);

        private static ActionHolder getActionHolder(Player player) {
            return player.getData(ModAttachments.ACTION_HOLDER);
        }

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
            Action prevActionRead = input.read("PrevAction", Action.CODEC).orElse(null);
            if (prevActionRead instanceof ContinuousAction prevContinuousActionRead){
                actionHolder.setPrevAction(prevContinuousActionRead);
            } else {
                LOGGER.warn("Failed to load prevAction, {} is not a ContinuousAction", prevActionRead);
            }
            actionHolder.tickCount = input.getIntOr("TickCount", 0);
            return actionHolder;
        }

        @Override
        public boolean write(ActionHolder attachment, ValueOutput output) {
            if (attachment.currentAction != null) {
                output.store("CurrentAction", Action.CODEC, attachment.currentAction);
            }
            if (attachment.prevAction != null) {
                output.store("PrevAction", Action.CODEC, attachment.prevAction);
            }
            output.putLong("TickCount", attachment.tickCount);
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
            buf.writeVarLong(attachment.tickCount);
        }

        @Override
        public ActionHolder read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable ActionHolder previousValue) {
            ActionHolder actionHolder = new ActionHolder();
            if (buf.readBoolean()) {
                int id = buf.readVarInt();
                actionHolder.currentAction = buf.registryAccess().lookupOrThrow(ModRegistries.ACTIONS_KEY).byId(id);
            }
            actionHolder.tickCount = buf.readVarLong();
            return actionHolder;
        }
    }
}
