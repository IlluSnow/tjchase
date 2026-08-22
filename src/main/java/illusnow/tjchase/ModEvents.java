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

package illusnow.tjchase;

import illusnow.tjchase.item.ModCreativeModeTabs;
import illusnow.tjchase.item.ModItems;
import illusnow.tjchase.network.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = TJChase.MODID)
public class ModEvents {
    @SubscribeEvent // on the mod event bus
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                PlayDanceTimePayload.TYPE,
                PlayDanceTimePayload.STREAM_CODEC
        );
        registrar.playToClient(
                UpdateControlledEntityPayload.TYPE,
                UpdateControlledEntityPayload.STREAM_CODEC
        );
        registrar.playToServer(
                UpdateInputPayload.TYPE,
                UpdateInputPayload.STREAM_CODEC,
                ModServerPayloadHandlers::handleUpdateInput
        );
        registrar.playToServer(
                UpdateControlledEntityRotationPayload.TYPE,
                UpdateControlledEntityRotationPayload.STREAM_CODEC,
                ModServerPayloadHandlers::handleUpdateControlledEntityRotation
        );
    }

    @SubscribeEvent
    public static void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == ModCreativeModeTabs.MAIN_TAB.get()) {
            event.accept(ModItems.BLUEPRINT.get());
            event.accept(ModItems.HARP.get());
            event.accept(ModItems.NETHERITE_HARP.get());
            event.accept(ModItems.VINE_SEED.get());
            event.accept(ModItems.REMOTE_CONTROL.get());
        }
    }
}
