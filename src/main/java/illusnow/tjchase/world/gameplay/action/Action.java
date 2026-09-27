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

import com.mojang.serialization.Codec;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import illusnow.tjchase.util.ModRegistries;
import illusnow.tjchase.util.PlayerAnimationUtils;
import illusnow.tjchase.world.gameplay.ModPlayerAnimationIDs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public abstract sealed class Action permits ContinuousAction, OneTimeAction {
    public static final Codec<Action> CODEC = ModRegistries.ACTIONS.byNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, Action> STREAM_CODEC = ByteBufCodecs.registry(ModRegistries.ACTIONS_KEY);
    public static final int LOW_PRIORITY = 10;
    public static final int MEDIUM_PRIORITY = 20;
    public static final int HIGH_PRIORITY = 30;

    private final Identifier id;
    private final Identifier animId;
    private final int priority;

    protected Action(Identifier id, Identifier animId, int priority) {
        this.id = id;
        this.animId = animId;
        this.priority = priority;
    }

    public void start(Player player, ActionHolder holder) {
        triggerRelatedAnim(player);
    }

    public void triggerRelatedAnim(Player player) {
        if (!player.level().isClientSide()) {
            return;
        }
        PlayerAnimationController controller = PlayerAnimationUtils.getController(player, ModPlayerAnimationIDs.ACTION_LAYER);
        controller.triggerAnimation(animId);
        applyFirstPersonAdjustments(controller);
    }

    protected void applyFirstPersonAdjustments(PlayerAnimationController controller) {
        controller.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
    }

    public void reload(Player player, ActionHolder holder) {
        triggerRelatedAnim(player);
    }

    public void stop(Player player, ActionHolder holder) {
        stopRelatedAnim(player);
    }

    public void stopRelatedAnim(Player player) {
        if (!player.level().isClientSide()) {
            return;
        }
        PlayerAnimationController controller = PlayerAnimationUtils.getController(player, ModPlayerAnimationIDs.ACTION_LAYER);
        controller.setFirstPersonMode(FirstPersonMode.NONE);
        controller.setFirstPersonConfiguration(new FirstPersonConfiguration());
//        controller.setFirstPersonFollowsCamera(false);
        controller.removeAllModifiers();
        controller.stopTriggeredAnimation();
    }

    public abstract void onComplete(Player player, ActionHolder holder);

    public abstract void onInterrupt(Player player, ActionHolder holder);

    public abstract void update(Player player, ActionHolder holder);

    public boolean canInterrupt(@Nullable Action action) {
        return action == null || priority >= action.priority;
    }

    public Identifier getId() {
        return id;
    }

    public Identifier getAnimId() {
        return animId;
    }

    @Override
    public String toString() {
        return "Action[" + id + "]";
    }
}
