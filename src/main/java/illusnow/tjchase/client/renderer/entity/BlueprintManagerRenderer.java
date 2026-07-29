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

package illusnow.tjchase.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import illusnow.tjchase.TJChase;
import illusnow.tjchase.client.renderer.ModRenderTypes;
import illusnow.tjchase.entity.BlueprintManager;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

public class BlueprintManagerRenderer extends EntityRenderer<BlueprintManager, BlueprintManagerRenderState> {
    private static final Identifier EDGE_TEXTURE = TJChase.prefix("textures/entity/blueprint/blueprint_edge.png");
    private static final Identifier TEXTURE = TJChase.prefix("textures/entity/blueprint/blueprint.png");
    private static final int TEXTURE_LENGTH = 160;
    private static final int TEXTURE_WIDTH = 2;

    public BlueprintManagerRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(BlueprintManagerRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        AABB blueprintAABB = renderState.getBlueprintAABB();
        double minX = (renderState.x - cameraRenderState.pos.x) + (blueprintAABB.minX - renderState.x);
        double minY = (renderState.y - cameraRenderState.pos.y) + (blueprintAABB.minY - renderState.y);
        double minZ = (renderState.z - cameraRenderState.pos.z) + (blueprintAABB.minZ - renderState.z);
        double maxX = (renderState.x - cameraRenderState.pos.x) + (blueprintAABB.maxX - renderState.x);
        double maxY = (renderState.y - cameraRenderState.pos.y) + (blueprintAABB.maxY - renderState.y);
        double maxZ = (renderState.z - cameraRenderState.pos.z) + (blueprintAABB.maxZ - renderState.z);
        double xLen = maxX - minX;
        double yLen = maxY - minY;
        double zLen = maxZ - minZ;

        poseStack.pushPose();
        nodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.lines(),
                (pose, consumer) -> {
                    float d = 0;

                    drawEdge(pose, consumer, renderState, minX - d, minY - d, minZ - d, minX - d, minY - d, maxZ + d);
                    drawEdge(pose, consumer, renderState, minX - d, minY - d, minZ - d, maxX + d, minY - d, minZ - d);
                    drawEdge(pose, consumer, renderState, minX - d, minY - d, maxZ + d, maxX + d, minY - d, maxZ + d);
                    drawEdge(pose, consumer, renderState, maxX + d, minY - d, minZ - d, maxX + d, minY - d, maxZ + d);

                    drawEdge(pose, consumer, renderState, minX - d, minY - d, minZ - d, minX - d, maxY + d, minZ - d);
                    drawEdge(pose, consumer, renderState, minX - d, minY - d, maxZ + d, minX - d, maxY + d, maxZ + d);
                    drawEdge(pose, consumer, renderState, maxX + d, minY - d, minZ - d, maxX + d, maxY + d, minZ - d);
                    drawEdge(pose, consumer, renderState, maxX + d, minY - d, maxZ + d, maxX + d, maxY + d, maxZ + d);

                    drawEdge(pose, consumer, renderState, minX - d, maxY + d, minZ - d, minX - d, maxY + d, maxZ + d);
                    drawEdge(pose, consumer, renderState, minX - d, maxY + d, minZ - d, maxX + d, maxY + d, minZ - d);
                    drawEdge(pose, consumer, renderState, minX - d, maxY + d, maxZ + d, maxX + d, maxY + d, maxZ + d);
                    drawEdge(pose, consumer, renderState, maxX + d, maxY + d, minZ - d, maxX + d, maxY + d, maxZ + d);
                }
        );
        nodeCollector.submitCustomGeometry(
                poseStack,
                ModRenderTypes.blueprint(TEXTURE),
                (pose, consumer) -> {
                    float x1 = (float) minX, y1 = (float) minY, z1 = (float) minZ;
                    float x2 = (float) minX, y2 = (float) minY, z2 = (float) maxZ;
                    float x3 = (float) maxX, y3 = (float) minY, z3 = (float) maxZ;
                    float x4 = (float) maxX, y4 = (float) minY, z4 = (float) minZ;
                    float x5 = (float) minX, y5 = (float) maxY, z5 = (float) minZ;
                    float x6 = (float) minX, y6 = (float) maxY, z6 = (float) maxZ;
                    float x7 = (float) maxX, y7 = (float) maxY, z7 = (float) maxZ;
                    float x8 = (float) maxX, y8 = (float) maxY, z8 = (float) minZ;
                    drawQuad(consumer, renderState, x2, y2, z2, x1, y1, z1, x4, y4, z4, x3, y3, z3, (float) xLen, (float) zLen);
                    drawQuad(consumer, renderState, x5, y5, z5, x6, y6, z6, x7, y7, z7, x8, y8, z8, (float) xLen, (float) zLen);
                    drawQuad(consumer, renderState, x5, y5, z5, x1, y1, z1, x2, y2, z2, x6, y6, z6, (float) zLen, (float) yLen);
                    drawQuad(consumer, renderState, x7, y7, z7, x3, y3, z3, x4, y4, z4, x8, y8, z8, (float) zLen, (float) yLen);
                    drawQuad(consumer, renderState, x8, y8, z8, x4, y4, z4, x1, y1, z1, x5, y5, z5, (float) xLen, (float) yLen);
                    drawQuad(consumer, renderState, x6, y6, z6, x2, y2, z2, x3, y3, z3, x7, y7, z7, (float) xLen, (float) yLen);
                }
        );
        poseStack.popPose();
        super.submit(renderState, poseStack, nodeCollector, cameraRenderState);
    }

    private void drawEdge(PoseStack.Pose pose, VertexConsumer consumer, BlueprintManagerRenderState renderState, double x1, double y1, double z1, double x2, double y2, double z2) {
        Vector3f normal = new Vector3f((float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1)).normalize();
        double distance1 = Math.sqrt(x1 * x1 + y1 * y1 + z1 * z1);
        double distance2 = Math.sqrt(x2 * x2 + y2 * y2 + z2 * z2);
        double referenceDistance = 10;
        float calculatedWidth1 = (float) (4.0 * (referenceDistance / distance1));
        float calculatedWidth2 = (float) (4.0 * (referenceDistance / distance2));
        float finalLineWidth1 = Math.max(1.0f, Math.min(12.0f, calculatedWidth1)) * 1.5F;
        float finalLineWidth2 = Math.max(1.0f, Math.min(12.0f, calculatedWidth2)) * 1.5F;
        consumer.addVertex((float) x1, (float) y1, (float) z1).setColor(1, 1, 1, renderState.getLineAlpha()).setLineWidth(finalLineWidth1).setNormal(pose, normal);
        consumer.addVertex((float) x2, (float) y2, (float) z2).setColor(1, 1, 1, renderState.getLineAlpha()).setLineWidth(finalLineWidth2).setNormal(pose, normal);
    }

    private void drawQuad(VertexConsumer consumer, BlueprintManagerRenderState renderState,
                          float x1, float y1, float z1,
                          float x2, float y2, float z2,
                          float x3, float y3, float z3,
                          float x4, float y4, float z4,
                          float u2, float v2) {
        float nx = Direction.UP.getStepX();
        float ny = Direction.UP.getStepY();
        float nz = Direction.UP.getStepZ();
        consumer.addVertex(x1, y1, z1).setColor(1, 1, 1, renderState.getMainAlpha()).setUv(0, v2).setLight(15728880).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(nx, ny, nz);
        consumer.addVertex(x2, y2, z2).setColor(1, 1, 1, renderState.getMainAlpha()).setUv(0, 0).setLight(15728880).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(nx, ny, nz);
        consumer.addVertex(x3, y3, z3).setColor(1, 1, 1, renderState.getMainAlpha()).setUv(u2, 0).setLight(15728880).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(nx, ny, nz);
        consumer.addVertex(x4, y4, z4).setColor(1, 1, 1, renderState.getMainAlpha()).setUv(u2, v2).setLight(15728880).setOverlay(OverlayTexture.NO_OVERLAY).setNormal(nx, ny, nz);
    }

    @Override
    public void extractRenderState(BlueprintManager entity, BlueprintManagerRenderState reusedState, float partialTick) {
        super.extractRenderState(entity, reusedState, partialTick);
        reusedState.setBlueprintAABB(entity.getBlueprintAABB());
        int lineTime = BlueprintManager.CONSTRUCT_TIME_LINE;
        int mainTime = BlueprintManager.CONSTRUCT_TIME_MAIN;
        if (entity.getDisappearTime() > 0) {
            reusedState.setMainAlpha(Mth.clampedLerp((entity.getDisappearTime() + partialTick) / mainTime, 1, 0));
            reusedState.setLineAlpha(Mth.clampedLerp((entity.getDisappearTime() + partialTick - mainTime) / lineTime, 1, 0));
        } else if (entity.getConstructTime() > 0) {
            reusedState.setLineAlpha(Mth.clampedLerp((entity.getConstructTime() + partialTick) / lineTime, 0, 1));
            reusedState.setMainAlpha(Mth.clampedLerp((entity.getConstructTime() + partialTick - lineTime) / mainTime, 0, 1));
        }
    }

    @Override
    protected AABB getBoundingBoxForCulling(BlueprintManager blueprintManager) {
        return blueprintManager.getBlueprintAABB();
    }

    @Override
    public BlueprintManagerRenderState createRenderState() {
        return new BlueprintManagerRenderState();
    }
}
