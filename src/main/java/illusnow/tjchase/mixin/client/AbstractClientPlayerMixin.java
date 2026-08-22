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
import illusnow.tjchase.entity.controllable.Controllable;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
    @ModifyExpressionValue(method = "getFieldOfViewModifier", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Abilities;flying:Z"))
    private boolean disableFlyingFOVModifier(boolean original) {
        Controllable controllingMob = Controllable.getControllingMob((AbstractClientPlayer) (Object) this);
        if (controllingMob != null) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method = "getFieldOfViewModifier", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Abilities;getWalkingSpeed()F"))
    private float modifyBaseSpeed(float original) {
        Controllable controllingMob = Controllable.getControllingMob((AbstractClientPlayer) (Object) this);
        if (controllingMob != null) {
            return (float) controllingMob.getSelfAsEntity().getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
        }
        return original;
    }

    @ModifyExpressionValue(method = "getFieldOfViewModifier", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getAttributeValue(Lnet/minecraft/core/Holder;)D"))
    private double modifySpeed(double original) {
        Controllable controllingMob = Controllable.getControllingMob((AbstractClientPlayer) (Object) this);
        if (controllingMob != null) {
            return controllingMob.getSelfAsEntity().getAttributeValue(Attributes.MOVEMENT_SPEED);
        }
        return original;
    }
}
