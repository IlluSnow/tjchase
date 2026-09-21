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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import illusnow.tjchase.entity.controllable.Controllable;
import illusnow.tjchase.network.c2s.UpdateControlledEntityRotationPayload;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @WrapOperation(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void modifyControlledEntityTurn(LocalPlayer player, double yRot, double xRot, Operation<Void> original) {
        Controllable controllingMob = Controllable.getControllingMob(player);
        if (controllingMob != null) {
            LivingEntity living = controllingMob.getSelfAsEntity();
            tjChase$applySmoothTurn(living, yRot, xRot);
            ClientPacketDistributor.sendToServer(new UpdateControlledEntityRotationPayload(living.getId(), living.getYRot(), living.yHeadRot, living.yBodyRot, living.getXRot()));
        } else {
            original.call(player, yRot, xRot);
        }
    }

    @Unique
    private static void tjChase$applySmoothTurn(LivingEntity living, double mouseX, double mouseY) {
        float fYaw = (float) mouseX * 0.15F;
        float fPitch = (float) mouseY * 0.15F;

        float oldXRot = living.getXRot();
        float newXRot = Mth.clamp(oldXRot + fPitch, -90.0F, 90.0F);
        float deltaXRot = newXRot - oldXRot;

        living.setXRot(newXRot);
        living.xRotO += deltaXRot;

        float oldHeadYaw = living.yHeadRot;
        float newHeadYaw = oldHeadYaw + fYaw;

        living.setYRot(newHeadYaw);
        living.yRotO += fYaw;
        living.setYHeadRot(newHeadYaw);
        living.yHeadRotO += fYaw;

        float oldBodyYaw = living.yBodyRot;
        float maxAngleDiff = 50.0F;
        float angleDiff = Mth.wrapDegrees(newHeadYaw - oldBodyYaw);

        float newBodyYaw = oldBodyYaw;
        if (angleDiff < -maxAngleDiff) {
            newBodyYaw = newHeadYaw + maxAngleDiff;
        } else if (angleDiff > maxAngleDiff) {
            newBodyYaw = newHeadYaw - maxAngleDiff;
        }

        float deltaBodyYaw = newBodyYaw - oldBodyYaw;
        living.yBodyRot = newBodyYaw;
        living.yBodyRotO += deltaBodyYaw;
    }
}
