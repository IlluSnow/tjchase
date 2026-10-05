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
import illusnow.tjchase.world.gameplay.struggle.StruggleInstance;
import illusnow.tjchase.world.gameplay.struggle.StruggleType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class ModHudLayers {
    public static final Identifier WEAK_COUNTDOWN = TJChase.prefix("weak_countdown");
    public static final Identifier STRUGGLE = TJChase.prefix("struggle");

    private static final Identifier STRUGGLE_PROGRESS_BACKGROUND_SPRITE = TJChase.prefix("hud/struggle_progress_background");
    private static final Identifier STRUGGLE_PROGRESS_SPRITE = TJChase.prefix("hud/struggle_progress");

    private ModHudLayers() {}

    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.SPECTATOR_TOOLTIP, WEAK_COUNTDOWN, ModHudLayers::renderWeakCountdown);
        event.registerAbove(VanillaGuiLayers.SPECTATOR_TOOLTIP, STRUGGLE, ModHudLayers::renderStruggle);
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
        Component text = Component.translatable(WeakState.WEAK_COUNTDOWN_TEXT, weakCountdown);
        guiGraphics.drawCenteredString(font, text, x, y, 0xFFFFA98C);
    }

    private static int renderWidthO = 0;
    private static int renderWidth = 0;
    private static long lastStruggleTimestamp = 0;
    @Nullable
    private static StruggleType lastStruggleType = null;

    private static void renderStruggle(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        int struggleProgressBackgroundHeight = 24;
        int struggleProgressBackgroundWidth = 111;
        int struggleProgressHeight = 7;
        int struggleProgressWidth = 86;
        int startOffsetX = 24;
        int startOffsetY = 9;
        int itemStartOffsetX = 4;
        int itemStartOffsetY = 4;
        float maxSmoothTicks = (float) StruggleType.INTERVAL / 2;

        Player player = getPlayer();
        StruggleInstance struggle = StruggleInstance.getStruggle(player);
        if (struggle == null) {
            resetStruggleRenderData();
            return;
        }
        double percentage = struggle.getPercentage();
        Font font = getFont();
        int x = guiGraphics.guiWidth() / 2 - struggleProgressBackgroundWidth / 2;
        int y = guiGraphics.guiHeight() - 24 - 9 - 35;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, STRUGGLE_PROGRESS_BACKGROUND_SPRITE, x, y, struggleProgressBackgroundWidth, struggleProgressBackgroundHeight);
        if (struggle.getType() != lastStruggleType) {
            resetStruggleRenderData();
            lastStruggleType = struggle.getType();
        } else if (renderWidth != (int) (struggleProgressWidth * percentage)) {
            renderWidthO = renderWidth;
            renderWidth = (int) (struggleProgressWidth * percentage);
            lastStruggleTimestamp = player.level().getGameTime();
        }
        float timeDelta = Math.min(player.level().getGameTime() - lastStruggleTimestamp + deltaTracker.getGameTimeDeltaTicks(), maxSmoothTicks);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                STRUGGLE_PROGRESS_SPRITE,
                struggleProgressWidth,
                struggleProgressHeight,
                0,
                0,
                x + startOffsetX,
                y + startOffsetY,
                (int) Mth.clampedLerp(timeDelta / maxSmoothTicks, renderWidthO, renderWidth),
                struggleProgressHeight);
        Component text = Component.literal("%.1f%%".formatted(percentage * 100));
        guiGraphics.renderItem(struggle.getType().getIcon(), x + itemStartOffsetX, y + itemStartOffsetY);
//        guiGraphics.drawCenteredString(getFont(), text, x + 40, y, 0xFFFFFFFF);
    }

    private static void resetStruggleRenderData() {
        renderWidthO = 0;
        renderWidth = 0;
        lastStruggleTimestamp = 0;
    }

    private static Font getFont() {
        return Minecraft.getInstance().font;
    }

    private static Player getPlayer() {
        return Objects.requireNonNull(Minecraft.getInstance().player, "Player not found");
    }
}
