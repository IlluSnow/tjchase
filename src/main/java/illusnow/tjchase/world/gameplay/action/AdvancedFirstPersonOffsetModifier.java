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

package illusnow.tjchase.world.gameplay.action;

import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractModifier;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;

public class AdvancedFirstPersonOffsetModifier extends AbstractModifier {
    private boolean isFirstPersonPass;
    public float xOffset;
    public float yOffset;
    public float zOffset;

    public AdvancedFirstPersonOffsetModifier(float xOffset, float yOffset, float zOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }

    @Override
    public void setupAnim(AnimationData state) {
        super.setupAnim(state);
        this.isFirstPersonPass = state.isFirstPersonPass();
    }

    @Override
    public PlayerAnimBone get3DTransform(PlayerAnimBone bone) {
        bone = super.get3DTransform(bone);
        if (isFirstPersonPass && "body".equals(bone.getName())) {
            bone.positionX += xOffset;
            bone.positionY += yOffset;
            bone.positionZ += zOffset;
        }
        return bone;
    }
}
