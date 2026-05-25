package illusnow.tjchase.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import illusnow.tjchase.entity.projectile.OrbitingBlockEntity;
import illusnow.tjchase.util.HarpConstants;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class OrbitingBlockRenderer extends EntityRenderer<OrbitingBlockEntity, OrbitingBlockRenderer.RenderState> {
    private final BlockRenderDispatcher blockRenderer;

    public OrbitingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();

    }

    @Override
    public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.scale(HarpConstants.ORBITING_BLOCK_SIZE, HarpConstants.ORBITING_BLOCK_SIZE, HarpConstants.ORBITING_BLOCK_SIZE);
//        poseStack.mulPose(cameraRenderState.orientation);
        poseStack.translate(-0.5F, 0, -0.5F);

        BlockState blockState = renderState.getBlockState();
        BlockStateModel blockStateModel = blockRenderer.getBlockModel(blockState);
        @SuppressWarnings("deprecation")
        RenderType renderType = ItemBlockRenderTypes.getRenderType(blockState);
        nodeCollector.submitBlockModel(poseStack, renderType, blockStateModel, 0.0F, 0.0F, 0.0F, renderState.lightCoords, OverlayTexture.NO_OVERLAY, renderState.outlineColor);
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
    }

    public static class RenderState extends EntityRenderState {
        private BlockState blockState = Blocks.AIR.defaultBlockState();

        public BlockState getBlockState() {
            return blockState;
        }

        public void setBlockState(BlockState blockState) {
            this.blockState = blockState;
        }
    }
}
