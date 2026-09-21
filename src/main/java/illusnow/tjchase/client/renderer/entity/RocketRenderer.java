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
import illusnow.tjchase.client.ClientUtils;
import illusnow.tjchase.client.renderer.renderstate.RocketRenderState;
import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.entity.gameplay.Rocket;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

public class RocketRenderer<R extends RocketRenderState & GeoRenderState> extends GeoEntityRenderer<Rocket, R> {
    private final Font font;

    public RocketRenderer(EntityRendererProvider.Context context) {
        this(context, ModEntities.ROCKET.get());
    }

    public RocketRenderer(EntityRendererProvider.Context context, EntityType<? extends Rocket> entityType) {
        super(context, entityType);
        this.font = context.getFont();
    }

    @Override
    public void addRenderData(Rocket rocket, @Nullable Void relatedObject, R renderState, float partialTick) {
        renderState.setFusePositionOffset(rocket.getFuseDisplayDirection().apply(rocket.getLookAngle(), rocket.getFuseDisplayDistance()).add(0, rocket.getFuseDisplayHeightOffset(), 0));
        renderState.setRenderFuseCountdown(!rocket.isFlying());
        renderState.setFuseCountdownSecondsText(Component.literal(String.valueOf(rocket.getFuseSeconds())));
        renderState.setFontScale(rocket.getFuseDisplayFontScale());
    }

    @Override
    public void postRenderPass(RenderPassInfo<R> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.postRenderPass(renderPassInfo, renderTasks);
        R renderState = renderPassInfo.renderState();
        if (renderState.shouldRenderFuseCountdown()) {
            PoseStack poseStack = renderPassInfo.poseStack();
            poseStack.pushPose();
            poseStack.translate(0, renderState.eyeHeight, 0);
            poseStack.translate(renderState.getFusePositionOffset());
            ClientUtils.facePlayerHorizontally(poseStack);
            float fontSize = (float) (0.04F * renderState.getFontScale());
            poseStack.scale(fontSize, -fontSize, fontSize);
            Component countdownComponent = renderState.getFuseCountdownSecondsText();
            FormattedCharSequence countdownText = countdownComponent.getVisualOrderText();

            float textX = -font.width(countdownText) / 2.0f;
            float textY = -4.5f;

            renderTasks.submitText(poseStack, textX, textY, countdownText, false, Font.DisplayMode.NORMAL, renderPassInfo.packedLight(), 0xFFFFFFFF, 0, 0);
            poseStack.popPose();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public R createRenderState(Rocket animatable, @Nullable Void relatedObject) {
        return (R) new RocketRenderState();
    }

    @Override
    protected float calculateYRot(Rocket rocket, float yHeadRot, float partialTick) {
        return Mth.rotLerp(partialTick, rocket.yRotO, rocket.getYRot());
    }
}
