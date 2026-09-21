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

package illusnow.tjchase.network.bidirectional;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.entity.gameplay.InGamePlacedEntity;
import illusnow.tjchase.world.gameplay.object.Template;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public record SyncInGameEntityPayload(int entityId, String name, Template<?> template) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncInGameEntityPayload> TYPE = new CustomPacketPayload.Type<>(TJChase.prefix("sync_in_game_entity"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncInGameEntityPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncInGameEntityPayload::entityId,
            ByteBufCodecs.STRING_UTF8, SyncInGameEntityPayload::name,
            Template.STREAM_CODEC, SyncInGameEntityPayload::template,
            SyncInGameEntityPayload::new
    );

    @Nullable
    public InGamePlacedEntity<?> getEntity(Level level) {
        return level.getEntity(entityId) instanceof InGamePlacedEntity<?> inGamePlacedEntity ? inGamePlacedEntity : null;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
