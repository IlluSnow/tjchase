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

package illusnow.tjchase.util;

import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

public final class PlayerAnimationUtils {
    private PlayerAnimationUtils() {}

    public static PlayerAnimationController getController(Player player, Identifier animLayer) {
        if (!player.level().isClientSide()) {
            throw new IllegalStateException("Wrong side");
        }
        return (PlayerAnimationController) PlayerAnimationAccess.getPlayerAnimationLayer(player, animLayer);
    }

    public static boolean triggerAnim(Player player, Identifier animLayer, Identifier animID) {
        return getController(player, animLayer).triggerAnimation(animID);
    }

    public static Animation getSimpleAnimation(Identifier animation) {
        return Objects.requireNonNull(PlayerAnimResources.getAnimation(animation), "Animation not found");
    }

    public static RawAnimation getSimpleRawAnimation(Identifier animation) {
        return RawAnimation.begin().then(getSimpleAnimation(animation), Animation.LoopType.DEFAULT);
    }
}
