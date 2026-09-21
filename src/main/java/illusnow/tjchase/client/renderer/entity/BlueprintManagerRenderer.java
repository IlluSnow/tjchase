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
import illusnow.tjchase.client.ClientUtils;
import illusnow.tjchase.client.renderer.renderstate.BlueprintManagerRenderState;
import illusnow.tjchase.entity.dataentity.BlueprintManager;
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
        if (ClientUtils.isRenderingShadowPass()) {
            return;
        }

        AABB blueprintAABB = renderState.getBlueprintAABB();
        double minX = blueprintAABB.minX - cameraRenderState.pos.x;
        double minY = blueprintAABB.minY - cameraRenderState.pos.y;
        double minZ = blueprintAABB.minZ - cameraRenderState.pos.z;
        double maxX = blueprintAABB.maxX - cameraRenderState.pos.x;
        double maxY = blueprintAABB.maxY - cameraRenderState.pos.y;
        double maxZ = blueprintAABB.maxZ - cameraRenderState.pos.z;
        double xLen = maxX - minX;
        double yLen = maxY - minY;
        double zLen = maxZ - minZ;

        poseStack.pushPose();
        drawSurfaces(renderState, poseStack, nodeCollector, (float) minX, (float) minY, (float) minZ, (float) maxZ, (float) maxX, (float) maxY, (float) xLen, (float) zLen, (float) yLen);
        drawEdges(renderState, poseStack, nodeCollector, minX, minY, minZ, maxZ, maxX, maxY);
        poseStack.popPose();
        super.submit(renderState, poseStack, nodeCollector, cameraRenderState);
    }

    private void drawSurfaces(BlueprintManagerRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, float minX, float minY, float minZ, float maxZ, float maxX, float maxY, float xLen, float zLen, float yLen) {
        nodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.entityNoOutline(TEXTURE),
                (pose, consumer) -> {
                    float x1 = minX, y1 = minY, z1 = minZ;
                    float x2 = minX, y2 = minY, z2 = maxZ;
                    float x3 = maxX, y3 = minY, z3 = maxZ;
                    float x4 = maxX, y4 = minY, z4 = minZ;
                    float x5 = minX, y5 = maxY, z5 = minZ;
                    float x6 = minX, y6 = maxY, z6 = maxZ;
                    float x7 = maxX, y7 = maxY, z7 = maxZ;
                    float x8 = maxX, y8 = maxY, z8 = minZ;
                    drawQuad(consumer, renderState, x2, y2, z2, x1, y1, z1, x4, y4, z4, x3, y3, z3, xLen, zLen);
                    drawQuad(consumer, renderState, x5, y5, z5, x6, y6, z6, x7, y7, z7, x8, y8, z8, xLen, zLen);
                    drawQuad(consumer, renderState, x5, y5, z5, x1, y1, z1, x2, y2, z2, x6, y6, z6, zLen, yLen);
                    drawQuad(consumer, renderState, x7, y7, z7, x3, y3, z3, x4, y4, z4, x8, y8, z8, zLen, yLen);
                    drawQuad(consumer, renderState, x8, y8, z8, x4, y4, z4, x1, y1, z1, x5, y5, z5, xLen, yLen);
                    drawQuad(consumer, renderState, x6, y6, z6, x2, y2, z2, x3, y3, z3, x7, y7, z7, xLen, yLen);
                }
        );
    }

    private void drawEdges(BlueprintManagerRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, double minX, double minY, double minZ, double maxZ, double maxX, double maxY) {
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
    }

    private void drawEdge(PoseStack.Pose pose, VertexConsumer consumer, BlueprintManagerRenderState renderState, double x1, double y1, double z1, double x2, double y2, double z2) {
        Vector3f normal = new Vector3f((float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1)).normalize();
        double avgX = (x1 + x2) / 2;
        double avgY = (y1 + y2) / 2;
        double avgZ = (z1 + z2) / 2;
        double distance1 = Math.sqrt(avgX * avgX + avgY * avgY + avgZ * avgZ);
        double distance2 = Math.sqrt(avgX * avgX + avgY * avgY + avgZ * avgZ);
        double referenceDistance = 8;
        float calculatedWidth1 = (float) (14 * (referenceDistance / distance1));
        float calculatedWidth2 = (float) (14 * (referenceDistance / distance2));
        float finalLineWidth1 = Math.max(1.5F, Math.min(14, calculatedWidth1));
        float finalLineWidth2 = Math.max(1.5F, Math.min(14, calculatedWidth2));
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
