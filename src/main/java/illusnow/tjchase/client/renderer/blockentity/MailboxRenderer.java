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

package illusnow.tjchase.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import illusnow.tjchase.block.MailboxBlock;
import illusnow.tjchase.block.entity.MailboxBlockEntity;
import illusnow.tjchase.block.entity.ModBlockEntities;
import illusnow.tjchase.client.ClientUtils;
import illusnow.tjchase.client.renderer.renderstate.MailboxRenderState;
import illusnow.tjchase.world.LuckinessHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

import java.util.Objects;

public class MailboxRenderer<R extends MailboxRenderState & GeoRenderState> extends GeoBlockRenderer<MailboxBlockEntity, R> {
    private final Font font;

    public MailboxRenderer(BlockEntityRendererProvider.Context context) {
        super(ModBlockEntities.MAILBOX.get());
        this.font = context.font();
    }

    @SuppressWarnings({"UnstableApiUsage", "unchecked"})
    @Override
    public R createRenderState() {
        return (R) new MailboxRenderState();
    }

    @Override
    public void addRenderData(MailboxBlockEntity mailbox, @Nullable Void relatedObject, R renderState, float partialTick) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            if (Minecraft.getInstance().hitResult instanceof BlockHitResult blockHitResult) {
                BlockPos pos = blockHitResult.getBlockPos();
                BlockState state = player.level().getBlockState(pos);
                if (state.getBlock() instanceof MailboxBlock) {
                    if (Objects.equals(pos, renderState.blockPos)) {
                        renderState.setRenderLuckiness(true);
                    }
                }
            }
            renderState.setLuckiness(LuckinessHelper.getGoldenFeatherLuckiness(player));
        }
    }

    @Override
    public void postRenderPass(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.postRenderPass(renderPassInfo, renderTasks);
        R renderState = renderPassInfo.renderState();
        if (renderState.shouldRenderLuckiness()) {
            PoseStack poseStack = renderPassInfo.poseStack();
            poseStack.pushPose();
            poseStack.translate(0.5, 1.3, 0.5);
            ClientUtils.facePlayer(poseStack);
            float fontSize = 0.05F;
            poseStack.scale(fontSize, -fontSize, fontSize);
            int luckiness = renderState.getLuckiness();
            Component luckinessText = Component.literal(String.valueOf(luckiness));

            float textX = -font.width(luckinessText) / 2.0f;
            float textY = -4.5f;
            boolean changeColor = luckiness >= LuckinessHelper.GOLDEN_FEATHER_UNLUCKY_THRESHOLD;
            int color = changeColor ? 0xFFFFF2B3 : 0xFFFFFFFF;
            int outlineColor = changeColor ? 0xFF332A0F : 0xFF333333;

            renderTasks.submitText(poseStack, textX, textY, luckinessText.getVisualOrderText(), false, Font.DisplayMode.NORMAL, renderPassInfo.packedLight(), color, 0, outlineColor);
            poseStack.popPose();
        }
    }
}
