package illusnow.tjchase.client.model;

import com.mojang.logging.LogUtils;
import illusnow.tjchase.client.ModRenderStateContextKeys;
import illusnow.tjchase.client.util.HarpAnimation;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;
import org.slf4j.Logger;

public final class ModArmPoses {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final EnumProxy<HumanoidModel.ArmPose> HARP_PLAY = new EnumProxy<>(HumanoidModel.ArmPose.class,
            true,
            true,
            (IArmPoseTransformer) (model, state, arm) -> {
                Float maxPlayHarpDuration = state.getRenderData(ModRenderStateContextKeys.MAX_PLAY_HARP_DURATION);
                if (maxPlayHarpDuration == null || maxPlayHarpDuration == 0 || !Float.isFinite(maxPlayHarpDuration)) {
                    LOGGER.warn("Unable to render play harp animation because of invalid maxPlayHarpDuration: {}", maxPlayHarpDuration);
                    return;
                }
                if (arm == HumanoidArm.RIGHT) {
                    HarpAnimation.renderPlayHarpAnimation(state.ticksUsingItem, maxPlayHarpDuration, model.rightArm, model.leftArm, false);
                } else {
                    HarpAnimation.renderPlayHarpAnimation(state.ticksUsingItem, maxPlayHarpDuration, model.leftArm, model.rightArm, true);
                }
            });

    private ModArmPoses() {}
}
