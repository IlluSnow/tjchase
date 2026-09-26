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
import illusnow.tjchase.entity.controllable.Controllable;
import illusnow.tjchase.entity.dataentity.VineManager;
import illusnow.tjchase.entity.gameplay.TyingHelper;
import illusnow.tjchase.tag.ModBlockTags;
import illusnow.tjchase.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Shadow private Optional<BlockPos> lastClimbablePos;

    @Shadow public abstract boolean isSuppressingSlidingDownLadder();

    @Shadow protected boolean jumping;

    @Shadow public float zza;

    @Shadow public float xxa;

    private LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @WrapOperation(method = "handleOnClimbable", at = @At(value = "NEW", target = "(DDD)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 handleTemporaryVineClimb(double x, double y, double z, Operation<Vec3> original) {
        if (tjChase$climbingOnSpeedBoostingVine()) {
            boolean hasMovementInput = jumping || zza != 0.0F || xxa != 0.0F;
            if (!hasMovementInput) {
                if (isSuppressingSlidingDownLadder()) {
                    return original.call(x, 0.0D, z);
                }
                return original.call(x, y, z);
            }

            double acceleratedY;
            if (y >= 0) {
                acceleratedY = Math.min(y * VineManager.VINE_CLIMBING_SPEED_MULTIPLIER, VineManager.MAX_CLIMBING_SPEED);
                return original.call(x, Math.max(y, acceleratedY), z);
            }
        }

        return original.call(x, y, z);
    }

    @WrapOperation(method = "handleOnClimbable", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(DD)D", remap = false))
    private double handleTemporaryVineFall(double a, double b, Operation<Double> original) {
        // b = -0.15
        if (tjChase$climbingOnSpeedBoostingVine()) {
            return original.call(a, b * VineManager.VINE_CLIMBING_GRAVITY_MULTIPLIER);
        }
        return original.call(a, b);
    }

    @Unique
    private boolean tjChase$climbingOnSpeedBoostingVine() {
        if (!VineManager.AFFECTED_BY_VINES.test(this)) {
            return false;
        }
        return lastClimbablePos.map(level()::getBlockState).filter(state -> state.is(ModBlockTags.SPEED_BOOSTING_VINE)).isPresent();
    }

    @WrapMethod(method = "lerpHeadTo")
    private void cancelLerp(float yaw, int pitch, Operation<Void> original) {
        if (this instanceof Controllable controllable && !controllable.canMoveFreely()) {
            return;
        }
        original.call(yaw, pitch);
    }

    @ModifyReturnValue(method = "attackable", at = @At("RETURN"))
    private boolean makePassivePlayerUnattackable(boolean original) {
        if ((Object) this instanceof Player self) {
            return !original || !Utils.isPassive(self);
        }
        return original;
    }

    @ModifyReturnValue(method = "isPickable", at = @At("RETURN"))
    private boolean notPickableIfBeingTied(boolean original) {
        if (!original) {
            return false;
        }
        if ((Object) this instanceof Player self) {
            return TyingHelper.getTiedTo(self) == null;
        }
        return true;
    }
}
