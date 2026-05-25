package illusnow.tjchase.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.util.HarpConstants;
import illusnow.tjchase.util.OrbitingBlock;
import illusnow.tjchase.util.OrbitingBlockHolder;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class OrbitingBlocksRenderHelper {
    private OrbitingBlocksRenderHelper() {}

    public static void renderOrbitingBlocks(PoseStack poseStack, LevelRenderState renderState, BlockRenderDispatcher blockRenderer, RenderBuffers renderBuffers, LocalPlayer player, float partialTicks) {
        OrbitingBlockHolder orbitingBlocks = player.getData(ModAttachments.ORBITING_BLOCKS.get());
        int count = orbitingBlocks.getOrbitingBlocks().size();

        for (int i = 0; i < count; i++)  {
            OrbitingBlock block = orbitingBlocks.getOrbitingBlocks().get(i);
            float yRotDelta = block.getYRot(partialTicks);
            Vec3 targetWorldPos = calculateWorldPos(player, partialTicks, yRotDelta, block.getOrbitRadius());
            Vec3 cameraPos = renderState.cameraRenderState.pos;
            Vec3 renderOffset = targetWorldPos.subtract(cameraPos);

            poseStack.pushPose();
            poseStack.translate(renderOffset.x, renderOffset.y, renderOffset.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(180F - yRotDelta));
            poseStack.scale(HarpConstants.ORBITING_BLOCK_SIZE, HarpConstants.ORBITING_BLOCK_SIZE, HarpConstants.ORBITING_BLOCK_SIZE);
            poseStack.translate(-0.5F, -0.5F, -0.5F);

            BlockState blockState = block.getBlockState();
            BlockPos lightPos = BlockPos.containing(targetWorldPos);
            int packedLight = LevelRenderer.getLightColor(player.level(), lightPos);
            blockRenderer.renderSingleBlock(
                    blockState,
                    poseStack,
                    renderBuffers.bufferSource(),
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    player.level(),
                    lightPos
            );

            poseStack.popPose();
        }
    }

    private static Vec3 calculateWorldPos(Entity entity, float partialTicks, float yRot, double orbitRadius) {
        return OrbitingBlock.calculateWorldPos(entity, entity.getPosition(partialTicks), yRot, orbitRadius);
    }
}
