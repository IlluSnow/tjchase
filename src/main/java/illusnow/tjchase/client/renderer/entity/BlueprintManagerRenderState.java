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

import illusnow.tjchase.util.Utils;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.AABB;

public class BlueprintManagerRenderState extends EntityRenderState {
    private AABB blueprintAABB = Utils.ZERO_AABB;
    private float lineAlpha;
    private float mainAlpha;

    public AABB getBlueprintAABB() {
        return blueprintAABB;
    }

    public void setBlueprintAABB(AABB blueprintAABB) {
        this.blueprintAABB = blueprintAABB;
    }

    public float getLineAlpha() {
        return lineAlpha;
    }

    public void setLineAlpha(float lineAlpha) {
        this.lineAlpha = lineAlpha;
    }

    public float getMainAlpha() {
        return mainAlpha;
    }

    public void setMainAlpha(float mainAlpha) {
        this.mainAlpha = mainAlpha;
    }
}
