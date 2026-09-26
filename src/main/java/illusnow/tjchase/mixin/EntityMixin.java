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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.entity.controllable.Controllable;
import illusnow.tjchase.entity.controllable.ControllableMovementHandler;
import illusnow.tjchase.entity.gameplay.TyingHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixin extends AttachmentHolder {
    @Shadow public abstract boolean isSpectator();

    @Shadow public abstract boolean isShiftKeyDown();

    @WrapOperation(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isPassengerOfSameVehicle(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean disablePushForTyingPlayers(Entity self, Entity another, Operation<Boolean> original) {
        if (self instanceof Player thisPlayer && another instanceof Player anotherPlayer) {
            if (TyingHelper.getTiedTo(thisPlayer) == anotherPlayer || TyingHelper.getTiedTo(anotherPlayer) == thisPlayer) {
                return true;
            }
        }
        return original.call(self, another);
    }

    @ModifyReturnValue(method = "isInvisible", at = @At("RETURN"))
    private boolean forceVisibleInsideBlueprint(boolean original) {
        if (!isSpectator()) {
            if (getData(ModAttachments.NEGATIVE_EFFECT_BLUEPRINT)) {
                return false;
            }
        }
        return original;
    }

    @ModifyReturnValue(method = "maybeBackOffFromEdge", at = @At("RETURN"))
    private Vec3 preventFallingDownWhenControlledSneaking(Vec3 original) {
        if (this instanceof Controllable controllable && isShiftKeyDown()) {
            return ControllableMovementHandler.preventFallingDownWhenSneaking(controllable.getSelfAsEntity(), original);
        }
        return original;
    }

    @WrapMethod(method = "isLocalInstanceAuthoritative")
    private boolean setLocalInstanceAuthoritativeIfBeingControlled(Operation<Boolean> original) {
        if (this instanceof Controllable controllable && !controllable.canMoveFreely()) {
            return true;
        }
        return original.call();
    }
}
