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

import illusnow.tjchase.client.renderer.ModDataTickets;
import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.entity.projectile.YogaBall;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.RenderPassInfo;
import software.bernie.geckolib.renderer.specialty.DirectionalProjectileRenderer;

import java.util.Objects;

public class YogaBallRenderer<R extends EntityRenderState & GeoRenderState> extends DirectionalProjectileRenderer<YogaBall, R> {
    public YogaBallRenderer(EntityRendererProvider.Context context) {
        super(context, ModEntities.YOGA_BALL.get());
    }

    @Override
    public void addRenderData(YogaBall yogaBall, @Nullable Void relatedObject, R renderState, float partialTick) {
        renderState.addGeckolibData(ModDataTickets.YOGA_BALL_RENDER_SCALE, getRenderScaleWhenDeflating(yogaBall, partialTick));
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<R> renderPassInfo, float widthScale, float heightScale) {
        float renderScale = Objects.requireNonNull(renderPassInfo.renderState().getGeckolibData(ModDataTickets.YOGA_BALL_RENDER_SCALE));
        super.scaleModelForRender(renderPassInfo, widthScale * renderScale, heightScale * renderScale);
    }

    private static float getRenderScaleWhenDeflating(YogaBall yogaBall, float partialTick) {
        if (yogaBall.getDeflateTime() == -1) {
            return 1;
        }
        float deflationProgress = Mth.lerp(Math.abs(yogaBall.getDeflateTime() + partialTick) / YogaBall.DEFLATE_TICKS, 1, 0);
        if (yogaBall.getDeflateTime() < 0) {
            deflationProgress = Mth.sqrt(deflationProgress);
        }
        return deflationProgress;
    }
}
