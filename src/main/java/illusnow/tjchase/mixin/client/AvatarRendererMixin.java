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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import illusnow.tjchase.client.renderer.ModDataTickets;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import software.bernie.geckolib.renderer.base.GeoRenderState;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
    @ModifyReturnValue(method = "shouldRenderLayers(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Z", at = @At("RETURN"))
    private boolean mayDisableLayerRendering(boolean original, @Local(argsOnly = true) AvatarRenderState state) {
        if (state instanceof GeoRenderState geoRenderState && !geoRenderState.getOrDefaultGeckolibData(ModDataTickets.ALLOW_LAYER_RENDERING, true)) {
            return false;
        }
        return original;
    }
}
