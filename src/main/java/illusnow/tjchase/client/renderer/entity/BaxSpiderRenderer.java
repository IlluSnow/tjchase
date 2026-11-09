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
import illusnow.tjchase.client.renderer.ModDataTickets;
import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.entity.projectile.BaxSpider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.RenderPassInfo;
import software.bernie.geckolib.renderer.specialty.DirectionalProjectileRenderer;

public class BaxSpiderRenderer<R extends EntityRenderState & GeoRenderState> extends DirectionalProjectileRenderer<BaxSpider, R> {
    public BaxSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, ModEntities.BAX_SPIDER.get());
    }

    @Override
    public boolean shouldRender(BaxSpider baxSpider, Frustum camera, double camX, double camY, double camZ) {
        return super.shouldRender(baxSpider, camera, camX, camY, camZ) && baxSpider.getOwner() != null;
    }

    @Override
    protected boolean affectedByCulling(BaxSpider baxSpider) {
        return false;
    }

    @Override
    public void addRenderData(BaxSpider baxSpider, @Nullable Void relatedObject, R renderState, float partialTick) {
        Vec3 interpolatedPosition = baxSpider.getPosition(partialTick);
        Vec3 backPosition = interpolatedPosition.subtract(baxSpider.getDeltaMovement().normalize().scale(5 / 16.0));
        renderState.addGeckolibData(ModDataTickets.BAX_SPIDER_BACK_POS, backPosition);
        if (baxSpider.getOwner() instanceof Player player) {
            renderState.addGeckolibData(ModDataTickets.BAX_SPIDER_LINKING_POS, getHoldPosition(baxSpider, player, partialTick));
        }
    }

    public static Vec3 getHoldPosition(BaxSpider baxSpider, Player player, float partialTick) {
        boolean right = player.getMainArm() == HumanoidArm.RIGHT && !baxSpider.isOffhandThrown() || player.getMainArm() == HumanoidArm.LEFT && baxSpider.isOffhandThrown();
        if (player.isLocalPlayer() && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            float yRot = Mth.lerp(partialTick * 0.5F, player.getYRot(), player.yRotO) * (float) (Math.PI / 180.0);
            float xRot = Mth.lerp(partialTick * 0.5F, player.getXRot(), player.xRotO) * (float) (Math.PI / 180.0);
            double sgn = right ? -1.0 : 1.0;
            Vec3 vec3 = new Vec3(0.39 * sgn, -0.6, 0.3);
            return vec3.xRot(-xRot).yRot(-yRot).add(player.getEyePosition(partialTick));
        } else {
            double sideOffset = 0.35 * (right ? -1.0 : 1.0);
            float xRot = Mth.lerp(partialTick * 0.5F, player.getXRot(), player.xRotO) * (float) (Math.PI / 180.0);
            float yBodyRot = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot) * (float) (Math.PI / 180.0);
            if (player.isFallFlying() || player.isAutoSpinAttack()) {
                Vec3 viewVector = player.getViewVector(partialTick);
                Vec3 deltaMovement = player.getDeltaMovement();
                double movementHorizontalLenSqr = deltaMovement.horizontalDistanceSqr();
                double viewVectorHorizontalLenSqr = viewVector.horizontalDistanceSqr();
                float zRot;
                if (movementHorizontalLenSqr > 0.0 && viewVectorHorizontalLenSqr > 0.0) {
                    double cosAngle = (deltaMovement.x * viewVector.x + deltaMovement.z * viewVector.z) / Math.sqrt(movementHorizontalLenSqr * viewVectorHorizontalLenSqr);
                    double crossVal = deltaMovement.x * viewVector.z - deltaMovement.z * viewVector.x;
                    zRot = (float) (Math.signum(crossVal) * Math.acos(cosAngle));
                } else {
                    zRot = 0.0F;
                }
                return player.getPosition(partialTick).add(new Vec3(sideOffset, -0.11, 0.85).zRot(-zRot).xRot(-xRot).yRot(-yBodyRot));
            } else if (player.isVisuallySwimming()) {
                return player.getPosition(partialTick).add(new Vec3(sideOffset, 0.2, -0.15).xRot(-xRot).yRot(-yBodyRot));
            } else {
                double d1 = player.getBoundingBox().getYsize() - 1.0;
                double d2 = player.isCrouching() ? -0.2 : 0.07;
                return player.getPosition(partialTick).add(new Vec3(sideOffset, d1, d2).yRot(-yBodyRot));
            }
        }
    }

    @Override
    public void postRenderPass(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.postRenderPass(renderPassInfo, renderTasks);
        Vec3 backPosition = renderPassInfo.renderState().getGeckolibData(ModDataTickets.BAX_SPIDER_BACK_POS);
        Vec3 linkingPosition = renderPassInfo.renderState().getGeckolibData(ModDataTickets.BAX_SPIDER_LINKING_POS);
        if (backPosition != null && linkingPosition != null) {
            renderString(renderPassInfo, renderTasks, linkingPosition, backPosition);
        }
    }

    private void renderString(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks, Vec3 start, Vec3 end) {
        Vec3 delta = end.subtract(start);
        double horizontalLength = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        double sag = Math.min(0.5, horizontalLength * 0.1);
        int segments = Math.clamp((int) Math.ceil(delta.length() * 16), 16, 32);
        Vec3 cameraPos = new Vec3(renderPassInfo.cameraState().pos.x, renderPassInfo.cameraState().pos.y, renderPassInfo.cameraState().pos.z);
        PoseStack cameraPose = new PoseStack();

        renderTasks.submitCustomGeometry(
                cameraPose,
                RenderTypes.lines(),
                (pose, consumer) -> {
                    for (int i = 0; i < segments; i++) {
                        double t0 = (double) i / segments;
                        double t1 = (double) (i + 1) / segments;
                        Vec3 point0 = start.lerp(end, t0).add(0, -4 * sag * t0 * (1 - t0), 0).subtract(cameraPos);
                        Vec3 point1 = start.lerp(end, t1).add(0, -4 * sag * t1 * (1 - t1), 0).subtract(cameraPos);
                        Vec3 tangent = point1.subtract(point0).normalize();
                        Vector3f normal = new Vector3f((float) tangent.x, (float) tangent.y, (float) tangent.z);
                        float appropriateLineWidth = Minecraft.getInstance().getWindow().getAppropriateLineWidth();
                        consumer.addVertex((float) point0.x, (float) point0.y, (float) point0.z)
                                .setColor(0.9F, 0.9F, 0.9F, 1.0F)
                                .setLineWidth(appropriateLineWidth)
                                .setNormal(pose, normal);
                        consumer.addVertex((float) point1.x, (float) point1.y, (float) point1.z)
                                .setColor(0.9F, 0.9F, 0.9F, 1.0F)
                                .setLineWidth(appropriateLineWidth)
                                .setNormal(pose, normal);
                    }
                }
        );
    }
}
