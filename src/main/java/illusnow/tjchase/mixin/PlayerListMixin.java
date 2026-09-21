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

package illusnow.tjchase.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import illusnow.tjchase.entity.controllable.Controllable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Definition(id = "radius", local = @Local(type = double.class, ordinal = 3, argsOnly = true))
    @Expression("? * ? + ? * ? + ? * ? < radius * radius")
    @ModifyExpressionValue(method = "broadcast", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private boolean broadcastIfNearControlledMob(boolean original, @Local(type = ServerPlayer.class) ServerPlayer player,
                                                 @Local(type = double.class, ordinal = 0, argsOnly = true) double x,
                                                 @Local(type = double.class, ordinal = 1, argsOnly = true) double y,
                                                 @Local(type = double.class, ordinal = 2, argsOnly = true) double z,
                                                 @Local(type = double.class, ordinal = 3, argsOnly = true) double radius
    ) {
        Controllable controllable = Controllable.getControllingMob(player);
        if (controllable != null) {
            Vec3 pos = controllable.getSelfAsEntity().position();
            return original || pos.distanceToSqr(new Vec3(x, y, z)) < radius * radius;
        }
        return original;
    }
}
