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

import com.mojang.blaze3d.vertex.PoseStack;
import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.client.ClientUtils;
import illusnow.tjchase.client.RenderStateAdditions;
import illusnow.tjchase.util.DancingHelper;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void extractAdditionalRenderState(T entity, S reusedState, float partialTick, CallbackInfo ci) {
        DancingHelper.DanceEffectType danceEffectType = entity.getData(ModAttachments.DANCE_EFFECT_TYPE.get());
        ((RenderStateAdditions) reusedState).tjChase$setDanceEffectType(danceEffectType);
    }

    @Inject(method = "submit", at = @At("TAIL"))
    private void renderDancingIcon(S renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        ClientUtils.renderDancingIcon(poseStack, nodeCollector, renderState);
    }
}
