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

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import illusnow.tjchase.entity.gameplay.TyingHelper;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void move(float zoom, float dy, float dx);

    @Shadow
    protected abstract void setRotation(float yRot, float xRot, float roll);

    @WrapWithCondition(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FFF)V", ordinal = 0))
    private boolean disableRotation(Camera instance, float yRot, float xRot, float roll, @Local(argsOnly = true) Entity entity) {
        return true;
    }

    @Inject(method = "setup", at = @At("TAIL"))
    private void moveCameraIfTied(Level level, Entity entity, boolean detached, boolean mirror, float partialTickTime, CallbackInfo ci) {
        if (entity instanceof Player player && TyingHelper.isCameraRestricted(player) && !(TyingHelper.getTiedTo(player) instanceof Player)) {
            setRotation(entity.getYRot(partialTickTime), 0, 0);
            move(1, 0, 0);
        }
    }
}
