package illusnow.tjchase.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import illusnow.tjchase.item.HarpItem;
import illusnow.tjchase.mixin.client.ItemInHandRendererAccessor;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import org.apache.commons.lang3.math.Fraction;

public final class HarpAnimation {
    public static final Fraction HOLD_ARM_MAX_SWING_PROGRESS = Fraction.ONE_FIFTH;
    public static final Fraction HOLD_ARM_RESET_PROGRESS = HOLD_ARM_MAX_SWING_PROGRESS.add(Fraction.ONE);
    public static final Fraction PLAY_ARM_MAX_SWING_PROGRESS = Fraction.ONE_HALF;
    public static final Fraction PLAY_ARM_RESET_PROGRESS = Fraction.getFraction(4, 3);
    public static final int PLAY_HARP_TIMES = 2;

    // For third person renderer only
    private static final float HOLD_ARM_Y_ROT = -Mth.PI / 8;
    private static final float HOLD_ARM_X_ROT = -Mth.PI / 3;
    private static final float PLAY_ARM_HORIZONTAL_SWING_SCALE = Mth.PI / 4;
    private static final float PLAY_ARM_VERTICAL_SWING_SCALE = Mth.PI / 6;
    private static final float PLAY_ARM_VERTICAL_PLAY_SCALE = Mth.PI / 6;
    private static final float HOLD_ARM_INITIAL_SWING = 0.6F;

    private HarpAnimation() {}

    public static void renderPlayHarpAnimation(float ticksUsingItem, float maxPlayDuration, ModelPart holdArm, ModelPart playArm, boolean leftHanded) {
        int sgn = leftHanded ? -1 : 1;
        float progress = ticksUsingItem / maxPlayDuration;
        progress = progress > 1 ? progress : smooth(progress);

        float holdArmSwingScale = getHoldArmSwingScale(progress);
        holdArm.xRot = HOLD_ARM_X_ROT * holdArmSwingScale;
        holdArm.yRot = sgn * HOLD_ARM_Y_ROT * holdArmSwingScale;

        if (progress <= PLAY_ARM_MAX_SWING_PROGRESS.floatValue()) {
            // Swing arm
            float playArmSwingProgress = progress * PLAY_ARM_MAX_SWING_PROGRESS.getDenominator();
            playArm.xRot = calculateHarpPlayingSwing(playArmSwingProgress);
            playArm.yRot = sgn * PLAY_ARM_HORIZONTAL_SWING_SCALE * (-1 + 2 * playArmSwingProgress);
        } else if (progress <= 1) {
            // Play harp
            float playHarpProgress = (progress - PLAY_ARM_MAX_SWING_PROGRESS.floatValue()) * PLAY_ARM_MAX_SWING_PROGRESS.getDenominator();
            playArm.xRot = calculateHarpPlayingSwing(1) - PLAY_ARM_VERTICAL_PLAY_SCALE * (Mth.cos(playHarpProgress * 2 * PLAY_HARP_TIMES * Mth.PI) - 1);
            playArm.yRot = sgn * PLAY_ARM_HORIZONTAL_SWING_SCALE;
        } else if (progress <= PLAY_ARM_RESET_PROGRESS.floatValue()) {
            // Reset arm
            float xRotProgress1 = calculateHarpPlayingSwing(1);
            float yRotProgress1 = PLAY_ARM_HORIZONTAL_SWING_SCALE;
            float playArmSwingScale = smooth(PLAY_ARM_RESET_PROGRESS.getNumerator() - PLAY_ARM_RESET_PROGRESS.getDenominator() * progress);
            playArm.xRot = xRotProgress1 * playArmSwingScale;
            playArm.yRot = sgn * yRotProgress1 * playArmSwingScale;
        }
    }

    public static float calculateHarpPlayingSwing(float swingProgress) {
        return -(Mth.PI / 2 + PLAY_ARM_VERTICAL_SWING_SCALE * Mth.cos(swingProgress * 2 * Mth.PI));
    }

    public static float smooth(float progress) {
        if (progress > 1) {
            return 1;
        }
        if (progress < 0) {
            return 0;
        }
        return progress * progress;
    }

    public static float getHoldArmSwingScale(float progress) {
        float holdArmSwingScale = 0;
        if (progress <= HOLD_ARM_MAX_SWING_PROGRESS.floatValue()) {
            holdArmSwingScale = HOLD_ARM_INITIAL_SWING + (1 - HOLD_ARM_INITIAL_SWING) * smooth(progress * HOLD_ARM_MAX_SWING_PROGRESS.getDenominator());
        } else if (progress <= 1) {
            holdArmSwingScale = 1;
        } else if (progress <= HOLD_ARM_RESET_PROGRESS.floatValue()) {
            holdArmSwingScale = smooth(HOLD_ARM_RESET_PROGRESS.getNumerator() - progress * HOLD_ARM_RESET_PROGRESS.getDenominator());
        }
        return holdArmSwingScale;
    }

    public static void renderFirstPersonPlayHarpAnimation(RenderHandEvent event, Player player, ItemStack stack, ItemInHandRenderer renderer) {
        HumanoidArm holdArm = (event.getHand() == InteractionHand.MAIN_HAND) ? player.getMainArm() : player.getMainArm().getOpposite();
        HumanoidArm playArm = holdArm.getOpposite();

        int sgn = playArm == HumanoidArm.LEFT ? -1 : 1;
        float progress = calculateUseProgress(stack, player, event.getPartialTick());

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        // Initial translation: the arm is now identical to the arm pos when holding the map
        poseStack.translate(sgn * 0.125F, -0.125F, 0.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(sgn * 10));

        float swingProgress = Math.min(1, progress * PLAY_ARM_MAX_SWING_PROGRESS.getDenominator());
        // Slightly raise and move left/right the left/right playArm
        poseStack.translate(sgn * 0.5F, 0.125F, 0.35F);
        // Rotate the arm
        poseStack.rotateAround(Axis.YP.rotationDegrees(-sgn * (75 - swingProgress * 125)), sgn * 0.625F, -0.5F, -1);
        // Move and correct the arm's position
        poseStack.translate(-sgn * (swingProgress * 0.5F), swingProgress * 0.125F, -swingProgress * 0.75F);

        if (progress >= PLAY_ARM_MAX_SWING_PROGRESS.floatValue() && progress <= 1) {
            final float playHarpVerticalMovementCoefficient = 0.15F; // Determines how much will the arm move up and down during the harp-playing progress.
            float playHarpProgress = (progress - PLAY_ARM_MAX_SWING_PROGRESS.floatValue()) * PLAY_ARM_MAX_SWING_PROGRESS.getDenominator();
            poseStack.translate(0, playHarpVerticalMovementCoefficient * Mth.sin(playHarpProgress * HarpAnimation.PLAY_HARP_TIMES * 2 * Mth.PI), 0);
        }
        if (progress > 1) {
            final float resettingMovementCoefficient = 1F; // Determines how much will the arm move down during the resetting progress.
            final float resettingScaleCoefficient = 50; // Determines how large will the resetting-arm angle be.
            float resetScale = smooth(PLAY_ARM_RESET_PROGRESS.getNumerator() - PLAY_ARM_RESET_PROGRESS.getDenominator() * progress);
            float resetProgress = 1 - resetScale;
            poseStack.translate(-sgn * 0.125F * resetProgress,
                    -resettingMovementCoefficient * resetProgress,
                    0);
            poseStack.mulPose(Axis.YP.rotationDegrees(-sgn * resettingScaleCoefficient * resetProgress));
        }

        ((ItemInHandRendererAccessor) renderer).callRenderPlayerArm(poseStack, event.getSubmitNodeCollector(), event.getPackedLight(), 0, 0, playArm);
        poseStack.popPose();
    }

    public static void renderFirstPersonThrowBlockAnimation(RenderHandEvent event, AbstractClientPlayer player, ItemInHandRenderer itemInHandRenderer) {
        HumanoidArm holdArm = (event.getHand() == InteractionHand.MAIN_HAND) ? player.getMainArm() : player.getMainArm().getOpposite();
        ((ItemInHandRendererAccessor) itemInHandRenderer)
                .callRenderPlayerArm(event.getPoseStack(), event.getSubmitNodeCollector(), event.getPackedLight(), 0, event.getSwingProgress(), holdArm);
    }

    public static void applyHarpTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick) {
        if (player.isUsingItem() && player.getUseItem() == itemInHand) {
            float progress = calculateUseProgress(itemInHand, player, partialTick);
            float swingProgress = Math.min(1, progress * PLAY_ARM_MAX_SWING_PROGRESS.getDenominator());

            int sgn = arm == HumanoidArm.RIGHT ? 1 : -1;
            float translationX = -sgn * (0.1F + 0.25F * swingProgress);
            float translationY = 0.1F + 0.1F * swingProgress;
            float translationZ = -0.02F * swingProgress;
            float resetScale = 1;
            if (progress > 1) {
                resetScale = smooth(PLAY_ARM_RESET_PROGRESS.getNumerator() - PLAY_ARM_RESET_PROGRESS.getDenominator() * progress);
            }
            poseStack.translate(translationX * resetScale, translationY * resetScale, translationZ * resetScale);
        }
    }

    private static float calculateUseProgress(ItemStack stack, Player player, float partialTick) {
        float maxPlayDuration = HarpItem.getPlayDuration(stack, player);
        float ticksUsingItem = player.getTicksUsingItem() + partialTick;
        float linearProgress = ticksUsingItem / maxPlayDuration;
        return linearProgress > 1 ? linearProgress : smooth(linearProgress);
    }
}
