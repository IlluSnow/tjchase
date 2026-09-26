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

package illusnow.tjchase.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import illusnow.tjchase.entity.gameplay.TyingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Nullable
    public LocalPlayer player;

    @ModifyExpressionValue(method = "handleKeybinds",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z"),
            slice = @Slice(
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;cycle()Lnet/minecraft/client/CameraType;")
            ))
    private boolean forceFirstPerson(boolean original) {
        if (player != null && TyingHelper.isCameraRestricted(player)) {
            return false;
        }
        return original;
    }
}
