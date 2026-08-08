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

package illusnow.tjchase.mixin.client;

import illusnow.tjchase.client.RenderStateAdditions;
import illusnow.tjchase.util.DancingHelper;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements RenderStateAdditions {
    @Unique
    private DancingHelper.DanceEffectType tjChase$danceEffectType = DancingHelper.DanceEffectType.NONE;
    @Unique
    private boolean tjChase$insideBlueprint;

    @Unique
    @Override
    public DancingHelper.DanceEffectType tjChase$getDanceEffectType() {
        return tjChase$danceEffectType;
    }

    @Unique
    @Override
    public void tjChase$setDanceEffectType(DancingHelper.DanceEffectType tjChase$danceEffectType) {
        this.tjChase$danceEffectType = tjChase$danceEffectType;
    }

}
