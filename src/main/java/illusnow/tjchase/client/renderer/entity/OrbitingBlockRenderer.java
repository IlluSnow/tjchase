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
import com.mojang.math.Axis;
import illusnow.tjchase.entity.projectile.OrbitingBlockEntity;
import illusnow.tjchase.mixin.client.BlockRenderDispatcherAccessor;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class OrbitingBlockRenderer extends EntityRenderer<OrbitingBlockEntity, OrbitingBlockRenderer.RenderState> {
    private final BlockRenderDispatcher blockRenderer;

    public OrbitingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        OrbitingBlockEntity.Properties properties = renderState.getProperties();
        float size = properties.size();
        poseStack.scale(size, size, size);
//        poseStack.mulPose(cameraRenderState.orientation);
        float rotateSpeed = 0;
        if (renderState.canSeek()) {
            rotateSpeed += 5;
        }
        if (renderState.isSeeking()) {
            rotateSpeed *= 5F;
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(renderState.getYRot() + renderState.ageInTicks * rotateSpeed));
        poseStack.translate(renderState.getXOffset(), renderState.getYOffset(), renderState.getZOffset());
        poseStack.translate(-0.5F, 0, -0.5F);

        BlockState blockState = renderState.getBlockState();
        BlockStateModel blockStateModel = blockRenderer.getBlockModel(blockState);
        @SuppressWarnings("deprecation")
        RenderType renderType = ItemBlockRenderTypes.getRenderType(blockState);
        int color = ((BlockRenderDispatcherAccessor) blockRenderer).getBlockColors().getColor(blockState, null, null, 0);
        float r = color == -1 ? 0 : (color >> 16 & 0xFF) / 255.0F;
        float g = color == -1 ? 0 : (color >> 8 & 0xFF) / 255.0F;
        float b = color == -1 ? 0 : (color & 0xFF) / 255.0F;
        nodeCollector.submitBlockModel(poseStack, renderType, blockStateModel, r, g, b, renderState.lightCoords, OverlayTexture.NO_OVERLAY, renderState.outlineColor);
        poseStack.popPose();
        super.submit(renderState, poseStack, nodeCollector, cameraRenderState);
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(OrbitingBlockEntity entity, RenderState reusedState, float partialTick) {
        super.extractRenderState(entity, reusedState, partialTick);
        reusedState.setBlockState(entity.getBlockState());
        reusedState.setProperties(entity.getProperties());
        reusedState.setYRot(180F + entity.getYRot(partialTick));
        reusedState.setXRot(entity.getXRot());
        AABB blockBounds = entity.getBlockBounds();
        reusedState.setHeight((float) (blockBounds.maxY - blockBounds.minY));
        reusedState.setYOffset(-(float) blockBounds.minY);
        float dz = (float) ((blockBounds.maxZ - 0.5F) + (blockBounds.minZ - 0.5F));
        float dx = (float) ((blockBounds.maxX - 0.5F) + (blockBounds.minX - 0.5F));
        reusedState.setZOffset(-Mth.sqrt(dz * dz + dx * dx) / 2);
        reusedState.setCanSeek(entity.canSeek());
        reusedState.setSeeking(entity.getTarget() != null);
    }

    public static class RenderState extends EntityRenderState {
        private BlockState blockState = Blocks.AIR.defaultBlockState();
        private OrbitingBlockEntity.Properties properties = OrbitingBlockEntity.Properties.DEFAULT;
        private float height;
        private float xOffset;
        private float yOffset;
        private float zOffset;
        private float yRot;
        private float xRot;
        private boolean canSeek;
        private boolean seeking;

        public BlockState getBlockState() {
            return blockState;
        }

        public void setBlockState(BlockState blockState) {
            this.blockState = blockState;
        }

        public OrbitingBlockEntity.Properties getProperties() {
            return properties;
        }

        public void setProperties(OrbitingBlockEntity.Properties properties) {
            this.properties = properties;
        }

        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }

        public float getYRot() {
            return yRot;
        }

        public void setYRot(float yRot) {
            this.yRot = yRot;
        }

        public float getXRot() {
            return xRot;
        }

        public void setXRot(float xRot) {
            this.xRot = xRot;
        }

        public float getXOffset() {
            return xOffset;
        }

        public void setXOffset(float xOffset) {
            this.xOffset = xOffset;
        }

        public float getYOffset() {
            return yOffset;
        }

        public void setYOffset(float yOffset) {
            this.yOffset = yOffset;
        }

        public float getZOffset() {
            return zOffset;
        }

        public void setZOffset(float zOffset) {
            this.zOffset = zOffset;
        }

        public boolean canSeek() {
            return canSeek;
        }

        public void setCanSeek(boolean canSeek) {
            this.canSeek = canSeek;
        }

        public boolean isSeeking() {
            return seeking;
        }

        public void setSeeking(boolean seeking) {
            this.seeking = seeking;
        }
    }
}
