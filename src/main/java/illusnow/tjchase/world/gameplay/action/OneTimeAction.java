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

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public abstract non-sealed class OneTimeAction<D extends ActionData> extends Action {
    private final int defaultDuration;
    protected boolean reloads = false;

    protected OneTimeAction(Identifier id, Identifier animId, int priority, int defaultDuration) {
        super(id, animId, priority);
        this.defaultDuration = defaultDuration;
    }

    @Override
    public void onInterrupt(Player player, ActionHolder holder) {}

    @Override
    public void update(Player player, ActionHolder holder) {
        if (!player.level().isClientSide()) {
            if (shouldInterrupt(player, holder, getDataFrom(holder))) {
                interrupt(player, holder);
                return;
            }
            if (holder.getProgress().shouldRemove(holder.getTickCount())) {
                complete(player, holder);
            }
        }
    }

    public boolean canTrigger(Player player, ActionHolder holder, ActionData data) {
        return !shouldInterrupt(player, holder, cast(data));
    }

    protected boolean shouldInterrupt(Player player, ActionHolder holder, D data) {
        Vec3 deltaMovement = player.getDeltaMovement();
        return interruptsActionIfMoving(player) && Math.abs(deltaMovement.x) + Math.abs(deltaMovement.z) > 0.1;
    }

    protected boolean interruptsActionIfMoving(Player player) {
        return true;
    }

    protected void interrupt(Player player, ActionHolder holder) {
        ActionHolder.setAction(player, null);
    }

    protected void complete(Player player, ActionHolder holder) {
        holder.completeCurrentAction(player);
        if (readPreviousContinuousAction(player, holder, true)) {
            ActionHolder.sync(player, ActionHolder.NetworkOp.COMPLETE_BACKTRACK, null, this, 1);
            ActionHolder.setAction(player, holder.getPrevAction());
        } else {
            ActionHolder.sync(player, ActionHolder.NetworkOp.COMPLETE, null, this, 1);
        }
        holder.setPrevAction(null);
    }

    @Override
    public void reload(Player player, ActionHolder holder) {
        if (reloads) {
            super.reload(player, holder);
        } else if (!player.level().isClientSide()) {
            ActionHolder.setAction(player, null);
            holder.setPrevAction(null);
        }
    }

    public D getDataFrom(ActionHolder holder) {
        return cast(holder.getActionData());
    }

    @SuppressWarnings("unchecked")
    public D cast(@Nullable ActionData data) {
        Objects.requireNonNull(data, "Data not found");
        return (D) data;
    }

    @Contract("null -> false")
    public boolean isValidData(@Nullable ActionData data) {
        if (data == null) {
            return false;
        }
        try {
            cast(data);
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public int defaultDuration() {
        return defaultDuration;
    }

    public boolean readPreviousContinuousAction(Player player, ActionHolder holder, boolean complete) {
        return !complete;
    }
}
