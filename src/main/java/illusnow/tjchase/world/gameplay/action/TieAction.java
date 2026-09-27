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

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration;
import illusnow.tjchase.entity.gameplay.Rocket;
import illusnow.tjchase.entity.gameplay.TyingHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public class TieAction extends OneTimeAction<TieAction.Data> {
    private static final double TIE_DISTANCE = 2;

    public TieAction(Identifier id, Identifier animId, int priority) {
        super(id, animId, priority, Rocket.DEFAULT_TIE_DURATION);
    }

    @Override
    protected boolean shouldInterrupt(Player player, ActionHolder holder, Data data) {
        return shouldInterrupt(player, holder, data, true);
    }

    @Override
    public boolean canTrigger(Player player, ActionHolder holder, @Nullable ActionData data) {
        return !shouldInterrupt(player, holder, (Data) data, false);
    }

    private boolean shouldInterrupt(Player player, ActionHolder holder, Data data, boolean allowOccupied) {
        if (super.shouldInterrupt(player, holder, data)) {
            return true;
        }
        return !isRocketValid(player, data, allowOccupied) || TyingHelper.getTying(player) == null;
    }

    @Override
    public void start(Player player, ActionHolder holder) {
        super.start(player, holder);
        if (!player.level().isClientSide()) {
            Rocket rocket = getRocket(player, holder);
            if (rocket != null) {
                rocket.setOccupied(true);
            }
        }
    }

    @Override
    public void onComplete(Player player, ActionHolder holder) {
        if (!player.level().isClientSide()) {
            Rocket rocket = getRocket(player, holder);
            Player tying = TyingHelper.getTying(player);
            if (rocket != null && tying != null && rocket.tryTiePlayerToSelf(tying, true)) {
                rocket.prime();
            }
        }
    }

    @Override
    public void stop(Player player, ActionHolder holder) {
        super.stop(player, holder);
        if (!player.level().isClientSide()) {
            Rocket rocket = getRocket(player, holder);
            if (rocket != null) {
                rocket.setOccupied(false);
            }
        }
    }

    @Nullable
    private Rocket getRocket(Player player, ActionHolder holder) {
        Data data = getDataFrom(holder);
        return EntityReference.get(data.rocket, player.level(), Rocket.class);
    }

    @Override
    protected void applyFirstPersonAdjustments(PlayerAnimationController controller) {
        super.applyFirstPersonAdjustments(controller);
        controller.setFirstPersonConfiguration(new FirstPersonConfiguration(true, true, false, false));
        controller.addModifierLast(new AdvancedFirstPersonOffsetModifier(0, 0, 2));
    }

    private boolean isRocketValid(Player player, Data data, boolean allowOccupied) {
        Rocket rocket = EntityReference.get(data.rocket, player.level(), Rocket.class);
        return rocket != null && rocket.canTiePlayerToSelf(allowOccupied) && player.distanceToSqr(rocket) <= TIE_DISTANCE * TIE_DISTANCE;
    }

    public record Data(EntityReference<Rocket> rocket, int duration) implements ActionData {}
}
