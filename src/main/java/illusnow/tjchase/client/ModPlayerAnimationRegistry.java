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

package illusnow.tjchase.client;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.enums.PlayState;
import illusnow.tjchase.entity.gameplay.TyingHelper;
import illusnow.tjchase.world.gameplay.ModAnimationIDs;
import net.minecraft.world.entity.player.Player;

public final class ModPlayerAnimationRegistry {
    public static final int TJCHASE_IMPORTANT_ACTION_PRIORITY = 2000;

    private ModPlayerAnimationRegistry() {}

    public static void registerPlayerAnimations() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ModAnimationIDs.STRUGGLE_LAYER, TJCHASE_IMPORTANT_ACTION_PRIORITY,
                avatar -> new PlayerAnimationController(avatar,
                        (controller, state, animSetter) ->
                                avatar instanceof Player player && TyingHelper.getTiedTo(player) != null ?
                                        animSetter.setAnimation(ModAnimationIDs.getSimpleRawAnimation(ModAnimationIDs.TIED_STRUGGLE)) : PlayState.STOP
                )
        );
    }
}
