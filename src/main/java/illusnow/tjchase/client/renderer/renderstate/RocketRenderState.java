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

package illusnow.tjchase.client.renderer.renderstate;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class RocketRenderState extends EntityRenderState {
    private Vec3 fusePositionOffset = Vec3.ZERO;
    private double fontScale;
    private boolean renderFuseCountdown;
    private Component fuseCountdownSecondsText = Component.empty();

    public boolean shouldRenderFuseCountdown() {
        return renderFuseCountdown;
    }

    public void setRenderFuseCountdown(boolean renderFuseCountdown) {
        this.renderFuseCountdown = renderFuseCountdown;
    }

    public Vec3 getFusePositionOffset() {
        return fusePositionOffset;
    }

    public void setFusePositionOffset(Vec3 fusePositionOffset) {
        this.fusePositionOffset = fusePositionOffset;
    }

    public Component getFuseCountdownSecondsText() {
        return fuseCountdownSecondsText;
    }

    public void setFuseCountdownSecondsText(Component fuseCountdownSecondsText) {
        this.fuseCountdownSecondsText = fuseCountdownSecondsText;
    }

    public double getFontScale() {
        return fontScale;
    }

    public void setFontScale(double fontScale) {
        this.fontScale = fontScale;
    }
}
