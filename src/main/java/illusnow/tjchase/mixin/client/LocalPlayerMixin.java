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

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import illusnow.tjchase.entity.controllable.Controllable;
import illusnow.tjchase.entity.controllable.ControllableMovementHandler;
import illusnow.tjchase.mixin.EntityAccessor;
import illusnow.tjchase.util.OriginalInputAccessor;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Shadow public abstract boolean isAutoJumpEnabled();

    @Shadow private int autoJumpTime;

    @Shadow protected abstract boolean isMoving();

    @Shadow public ClientInput input;

    @Shadow protected abstract boolean canAutoJump();

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/ClientInput;makeJump()V"))
    private void makeControlledMobJump(ClientInput instance, Operation<Void> original) {
        Controllable controllingMob = Controllable.getControllingMob((LocalPlayer) (Object) this);
        if (controllingMob != null) {
            Vec2 moveVector = ControllableMovementHandler.calculateMoveVector(((OriginalInputAccessor) instance).tjChase$getOriginalInput());
            ControllableMovementHandler.makeControlledMobJump(controllingMob.getSelfAsEntity(), moveVector.lengthSquared() > 0);
        } else {
            original.call(instance);
        }
    }

    @WrapOperation(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;updateAutoJump(FF)V"))
    private void redirectAutoJumper(LocalPlayer instance, float movementX, float movementZ, Operation<Void> original) {
        Controllable controllingMob = Controllable.getControllingMob((LocalPlayer) (Object) this);
        if (controllingMob != null && canAutoJump()) {
            Input originalInput = ((OriginalInputAccessor) input).tjChase$getOriginalInput();
            ControllableMovementHandler.updateAutoJumpIfPossible(controllingMob.getSelfAsEntity(), movementX, movementZ, originalInput, () -> autoJumpTime = 1);
        } else {
            original.call(instance, movementX, movementZ);
        }
    }

    @WrapMethod(method = "canAutoJump")
    private boolean canControllingMobAutoJump(Operation<Boolean> original) {
        Controllable controllingMob = Controllable.getControllingMob((LocalPlayer) (Object) this);
        if (controllingMob != null) {
            Input originalInput = ((OriginalInputAccessor) input).tjChase$getOriginalInput();
            return tjChase$canControlledMobAutoJump(controllingMob.getSelfAsEntity(), ControllableMovementHandler.calculateMoveVector(originalInput).lengthSquared() > 0);
        }
        return original.call();
    }

    @Unique
    private boolean tjChase$canControlledMobAutoJump(LivingEntity entity, boolean moving) {
        return ControllableMovementHandler.ENABLE_AUTO_JUMP && isAutoJumpEnabled()
                && autoJumpTime <= 0
                && entity.onGround()
                && !entity.isShiftKeyDown() // Replaced isStayingOnGroundSurface()
                && !entity.isPassenger()
                && moving
                && ((EntityAccessor) entity).callGetBlockJumpFactor() >= 1.0;
    }
}
