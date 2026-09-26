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

package illusnow.tjchase.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface Freezable {
    boolean isFrozen();

    void freezeOnDisconnect(ServerPlayer player);

    void unfreezeOnConnect(ServerPlayer player);

    default boolean isPartiallyFrozen() {
        return isFrozen();
    }

    static Situation findCurrentSituation(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        if (server.isSingleplayer() && !server.isPublished()) {
            return Situation.SINGLEPLAYER;
        }
        if (server.isStopped()) {
            return Situation.SERVER_STOP;
        }
        if (Utils.onlyOnePlayerDisconnectedOrLanWorld(server)) {
            if (server.isSingleplayer()) {
                return Utils.isLanWorldHost(player) ? Situation.LAN_WORLD_HOST : Situation.LAN_WORLD_GUEST;
            } else {
                return Situation.SERVER_SINGLEPLAYER_DISCONNECT;
            }
        }
        return Situation.UNKNOWN;
    }

    enum Situation {
        SINGLEPLAYER(false),
        LAN_WORLD_GUEST(true),
        LAN_WORLD_HOST(false),
        SERVER_SINGLEPLAYER_DISCONNECT(true),
        SERVER_STOP(true),
        UNKNOWN(true);

        private final boolean playerMaybeOfflineAfterLoad;

        Situation(boolean playerMaybeOfflineAfterLoad) {
            this.playerMaybeOfflineAfterLoad = playerMaybeOfflineAfterLoad;
        }

        public boolean playerMaybeOfflineAfterLoad() {
            return playerMaybeOfflineAfterLoad;
        }
    }
}
