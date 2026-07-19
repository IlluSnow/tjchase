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
