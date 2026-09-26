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

package illusnow.tjchase.client.gui;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.world.gameplay.WeakState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.Objects;

public final class ModHudLayers {
    public static final Identifier WEAK_COUNTDOWN = TJChase.prefix("weak_countdown");

    public static final String WEAK_COUNTDOWN_TEXT = prefix("weak_countdown");

    private ModHudLayers() {}

    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.SPECTATOR_TOOLTIP, WEAK_COUNTDOWN, ModHudLayers::renderWeakCountdown);
    }

    private static void renderWeakCountdown(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        WeakState weak = getPlayer().getData(ModAttachments.WEAK_STATE);
        if (!weak.isWeak()) {
            return;
        }
        Font font = getFont();
        int x = guiGraphics.guiWidth() / 2;
        int y = guiGraphics.guiHeight() - 24 - 9 - 10;
        int weakCountdown = Mth.ceil(weak.getRecoverTicks() / 20.0);
        Component text = Component.translatable(WEAK_COUNTDOWN_TEXT, weakCountdown);
        guiGraphics.drawCenteredString(font, text, x, y, 0xFFFFA98C);
    }

    private static String prefix(String name) {
        return TJChase.prefix("gui", name);
    }

    private static Font getFont() {
        return Minecraft.getInstance().font;
    }

    private static Player getPlayer() {
        return Objects.requireNonNull(Minecraft.getInstance().player, "Player not found");
    }
}
