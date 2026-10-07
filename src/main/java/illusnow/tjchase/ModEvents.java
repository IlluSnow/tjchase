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
import illusnow.tjchase.network.ModServerPayloadHandlers;
import illusnow.tjchase.network.bidirectional.SyncInGameEntityPayload;
import illusnow.tjchase.network.c2s.*;
import illusnow.tjchase.network.s2c.*;
import illusnow.tjchase.util.ModRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.NewRegistryEvent;

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
                PlayFuseSoundPayload.TYPE,
                PlayFuseSoundPayload.STREAM_CODEC
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
        registrar.playToServer(
                UpdateControlledEntityPositionPayload.TYPE,
                UpdateControlledEntityPositionPayload.STREAM_CODEC,
                ModServerPayloadHandlers::handleUpdateControlledEntityPosition
        );
        registrar.playToClient(
                OpenGameplayObjectEditScreenForInGameEntityPayload.TYPE,
                OpenGameplayObjectEditScreenForInGameEntityPayload.STREAM_CODEC
        );
        registrar.playToServer(
                LoadTemplatePayload.TYPE,
                LoadTemplatePayload.STREAM_CODEC,
                ModServerPayloadHandlers::handleLoadTemplate
        );
        registrar.playToClient(
                TiePlayerPayload.TYPE,
                TiePlayerPayload.STREAM_CODEC
        );
        registrar.playToClient(
                UpdateActionPayload.TYPE,
                UpdateActionPayload.STREAM_CODEC
        );
        registrar.playToClient(
                UpdateWeakStatusPayload.TYPE,
                UpdateWeakStatusPayload.STREAM_CODEC
        );
        registrar.playToServer(
                StrugglePayload.TYPE,
                StrugglePayload.STREAM_CODEC,
                ModServerPayloadHandlers::handleStruggle
        );
        registrar = registrar.executesOn(HandlerThread.NETWORK);
        registrar.playBidirectional(
                SyncInGameEntityPayload.TYPE,
                SyncInGameEntityPayload.STREAM_CODEC,
                ModServerPayloadHandlers::handleSyncInGameEntity,
                ModServerPayloadHandlers::handleSyncInGameEntity
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
        if (event.getTab() == ModCreativeModeTabs.GAMEPLAY_TAB.get()) {
            event.accept(ModItems.ROCKET_EDITOR.get());
            event.accept(ModItems.MOUSE_EARS.get());
            event.accept(ModItems.CAT_EARS.get());
        }
    }

    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event) {
        event.register(ModRegistries.ACTIONS);
        event.register(ModRegistries.EDITABLE_VALUES);
        event.register(ModRegistries.GAMEPLAY_OBJECT_TYPES);
        event.register(ModRegistries.STRUGGLE_TYPES);
    }
}
