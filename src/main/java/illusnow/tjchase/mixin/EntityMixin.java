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
import illusnow.tjchase.attachment.ModAttachments;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixin extends AttachmentHolder {
    @Shadow public abstract boolean isSpectator();

    @ModifyReturnValue(method = "isInvisible", at = @At("RETURN"))
    private boolean forceVisibleInsideBlueprint(boolean original) {
        if (!isSpectator()) {
            if (getData(ModAttachments.NEGATIVE_EFFECT_BLUEPRINT)) {
                return false;
            }
        }
        return original;
    }
}
