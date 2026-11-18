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

import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.entity.projectile.BlueRascal;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.specialty.DirectionalProjectileRenderer;

public class BlueRascalRenderer<R extends EntityRenderState & GeoRenderState> extends DirectionalProjectileRenderer<BlueRascal, R> {
    public BlueRascalRenderer(EntityRendererProvider.Context context) {
        super(context, ModEntities.BLUE_RASCAL.get());
    }

    @Override
    protected float calculateYRot(BlueRascal animatable, float yHeadRot, float partialTick) {
        return Mth.rotLerp(partialTick, animatable.yRotO, animatable.getYRot()) + 180f;
    }
}
