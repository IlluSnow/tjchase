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

import illusnow.tjchase.TJChase;
import illusnow.tjchase.client.renderer.ModDataTickets;
import illusnow.tjchase.entity.Cocoon;
import illusnow.tjchase.entity.ModEntities;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

import java.util.Objects;

public class CocoonRenderer<R extends EntityRenderState & GeoRenderState> extends GeoEntityRenderer<Cocoon, R> {
    private static final Identifier TEXTURE = TJChase.prefixTex("entity/cocoon/cocoon.png");
    private static final Identifier TEXTURE_DAMAGED = TJChase.prefixTex("entity/cocoon/cocoon_damaged.png");

    public CocoonRenderer(EntityRendererProvider.Context context) {
        super(context, ModEntities.COCOON.get());
    }

    @Override
    public void addRenderData(Cocoon cocoon, @Nullable Void relatedObject, R renderState, float partialTick) {
        renderState.addGeckolibData(ModDataTickets.RENDER_SCALE, cocoon.getSize());
        renderState.addGeckolibData(ModDataTickets.COCOON_DAMAGED, cocoon.isDamaged());
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<R> renderPassInfo, float widthScale, float heightScale) {
        float renderScale = Objects.requireNonNull(renderPassInfo.renderState().getGeckolibData(ModDataTickets.RENDER_SCALE));
        super.scaleModelForRender(renderPassInfo, widthScale * renderScale, heightScale * renderScale);
    }

    @Override
    public Identifier getTextureLocation(R renderState) {
        return renderState.getOrDefaultGeckolibData(ModDataTickets.COCOON_DAMAGED, false) ? TEXTURE_DAMAGED : TEXTURE;
    }
}
