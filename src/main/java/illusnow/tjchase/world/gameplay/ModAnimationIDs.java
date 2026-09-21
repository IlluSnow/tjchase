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

package illusnow.tjchase.world.gameplay;

import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import illusnow.tjchase.TJChase;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public class ModAnimationIDs {
    public static final Identifier STRUGGLE_LAYER = TJChase.prefix("struggle");
    public static final Identifier TIED_STRUGGLE = TJChase.prefix("tied_struggle");

    public static Animation getSimpleAnimation(Identifier animation) {
        return Objects.requireNonNull(PlayerAnimResources.getAnimation(animation), "Animation not found");
    }

    public static RawAnimation getSimpleRawAnimation(Identifier animation) {
        return RawAnimation.begin().then(getSimpleAnimation(animation), Animation.LoopType.DEFAULT);
    }
}
