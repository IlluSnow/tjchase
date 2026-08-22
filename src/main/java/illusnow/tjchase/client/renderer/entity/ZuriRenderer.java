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
import illusnow.tjchase.entity.Zuri;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class ZuriRenderer<R extends EntityRenderState & GeoRenderState> extends GeoEntityRenderer<Zuri, R> {
    public ZuriRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(BuiltInRegistries.ENTITY_TYPE.getKey(ModEntities.ZURI.get()), "head"));
    }

    @Override
    public void addRenderData(Zuri zuri, @Nullable Void relatedObject, R renderState, float partialTick) {
        renderState.addGeckolibData(ModDataTickets.DANCING, zuri.isDancing());
    }
}
