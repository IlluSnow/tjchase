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
import illusnow.tjchase.entity.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@EventBusSubscriber(modid = TJChase.MODID, value = Dist.CLIENT)
public final class ModEntityRenderers {
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BLUEPRINT.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.BLUEPRINT_MANAGER.get(), BlueprintManagerRenderer::new);
        event.registerEntityRenderer(ModEntities.EVILINIA.get(), context -> new GeoEntityRenderer<>(context, ModEntities.EVILINIA.get()));
        event.registerEntityRenderer(ModEntities.HARP_TESTER.get(), DummyEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.LINIA.get(), context -> new GeoEntityRenderer<>(context, ModEntities.LINIA.get()));
        event.registerEntityRenderer(ModEntities.ORBITING_BLOCK.get(), OrbitingBlockRenderer::new);
        event.registerEntityRenderer(ModEntities.VINE_MANAGER.get(), DummyEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.VINE_SEED.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.YOGA_BALL.get(), context -> new YogaBallRenderer<>(context).withScale(0.5F));
        event.registerEntityRenderer(ModEntities.ZURI.get(), context -> new ZuriRenderer<>(context).withScale(0.75F));
    }

    private ModEntityRenderers() {}
}
