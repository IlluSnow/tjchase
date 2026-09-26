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

package illusnow.tjchase.client;

import com.mojang.logging.LogUtils;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import com.zigythebird.playeranimcore.animation.layered.modifier.SpeedModifier;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranimcore.enums.PlayState;
import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.util.PlayerAnimationUtils;
import illusnow.tjchase.world.gameplay.ModPlayerAnimationIDs;
import illusnow.tjchase.world.gameplay.WeakState;
import illusnow.tjchase.world.gameplay.action.AdvancedFirstPersonOffsetModifier;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.slf4j.Logger;

public final class ModPlayerAnimationRegistry {
    public static final int WEAK_PRIORITY = 8;
    public static final int TJCHASE_ACTION_LOW_PRIORITY = 1500;
    public static final int TJCHASE_ACTION_MEDIUM_PRIORITY = 2000;
    public static final int TJCHASE_ACTION_HIGH_PRIORITY = 2500;
    private static final Logger LOGGER = LogUtils.getLogger();

    private ModPlayerAnimationRegistry() {}

    public static void registerPlayerAnimations() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ModPlayerAnimationIDs.ACTION_LAYER, TJCHASE_ACTION_MEDIUM_PRIORITY,
                avatar -> new PlayerAnimationController(avatar,
                        (controller, state, animSetter) -> PlayState.STOP
                )
        );
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ModPlayerAnimationIDs.WEAK_LAYER, WEAK_PRIORITY,
                ModPlayerAnimationRegistry::getWeakAnim
        );
    }

    private static IAnimation getWeakAnim(Avatar avatar) {
        PlayerAnimationController weakAnimController = new PlayerAnimationController(avatar,
                (controller, state, animSetter) -> {
                    WeakState weak = avatar.getData(ModAttachments.WEAK_STATE);
                    if (weak.isWeak()) {
                        boolean moving = state.isMoving();
                        if (avatar instanceof RemotePlayer player) {
                            moving = player.walkAnimation.speed() > 0.015F;
                        }
                        // When the player is moving the animation may not be correctly played, so I added this
                        if (!controller.isActive()) {
                            controller.forceAnimationReset();
                        }
                        RawAnimation animation = createWeakRawAnimation(avatar, controller, moving ? weak.getWeakAnimMoving() : weak.getWeakAnimStill());
                        return animSetter.setAnimation(animation);
                    }
                    return PlayState.STOP;
                });
        weakAnimController.addModifierLast(new SpeedModifier(1));
        weakAnimController.addModifierLast(new AdvancedFirstPersonOffsetModifier(0, 0, 8));
        weakAnimController.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
        weakAnimController.setFirstPersonFollowsCamera(true);
        weakAnimController.setFirstPersonConfiguration(new FirstPersonConfiguration(true, true, false, false));
        return weakAnimController;
    }

    public static RawAnimation createWeakRawAnimation(Avatar avatar, AnimationController controller, Identifier animId) {
        double baseSpeed = avatar.getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
        double speed = avatar.getSpeed();
        RawAnimation animation = RawAnimation.begin()
                .thenLoop(PlayerAnimationUtils.getSimpleAnimation(animId));
        if (!(controller.getModifier(0) instanceof SpeedModifier speedMod)) {
            LOGGER.warn("SpeedModifier not found for WeakAnimStateHandler");
        } else {
            speedMod.speed = (float) (speed / baseSpeed);
        }
        return animation;
    }
}
