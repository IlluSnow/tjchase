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

package illusnow.tjchase.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import illusnow.tjchase.util.DancingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Quaternionf;

import java.util.Objects;

public final class ClientUtils {
    private ClientUtils() {}

    public static void renderDancingIcon(PoseStack poseStack, SubmitNodeCollector collector, EntityRenderState renderState) {
        DancingHelper.DanceEffectType danceEffectType = ((RenderStateAdditions) renderState).tjChase$getDanceEffectType();
        if (renderState.isInvisible || danceEffectType == DancingHelper.DanceEffectType.NONE) {
            return;
        }
        Identifier textureLocation = danceEffectType.getTextureLocation();
        Objects.requireNonNull(textureLocation);
        poseStack.pushPose();
        float yOffset = renderState.boundingBoxHeight * 1.2F + 0.2F;
        poseStack.translate(0, yOffset, 0);
        Quaternionf rotation = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        poseStack.mulPose(new Quaternionf(0, rotation.y, 0, rotation.w));

        float size = 0.5F;
        collector.submitCustomGeometry(poseStack,
                RenderTypes.entityCutoutNoCull(textureLocation, false),
                (pose, consumer) -> {
                    dancingEffectIconVertex(pose, consumer, renderState, -size / 2, -size / 2, 0, 1);
                    dancingEffectIconVertex(pose, consumer, renderState, size / 2, -size / 2, 1, 1);
                    dancingEffectIconVertex(pose, consumer, renderState, size / 2, size / 2, 1, 0);
                    dancingEffectIconVertex(pose, consumer, renderState, -size / 2, size / 2, 0, 0);
                }
        );
        poseStack.popPose();
    }

    private static void dancingEffectIconVertex(PoseStack.Pose pose, VertexConsumer consumer, EntityRenderState renderState, float x, float y, float u, float v) {
        consumer.addVertex(pose, x, y, 0)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(renderState.lightCoords)
                .setNormal(pose, 0, 1, 0);
    }
}
