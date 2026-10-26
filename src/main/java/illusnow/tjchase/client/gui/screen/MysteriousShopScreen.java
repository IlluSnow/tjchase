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

package illusnow.tjchase.client.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import illusnow.tjchase.TJChase;
import illusnow.tjchase.util.Utils;
import illusnow.tjchase.world.MysteriousShopTrade;
import illusnow.tjchase.world.inventory.MysteriousShopMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.awt.Color;
import java.util.List;

import static illusnow.tjchase.world.inventory.MysteriousShopMenu.*;

public class MysteriousShopScreen extends AbstractContainerScreen<MysteriousShopMenu> {
    private static final int WIDTH = 230;
    private static final int HEIGHT = 219;
    private static final Identifier NOT_SELECTED = TJChase.prefix("container/mysterious_shop_entry");
    private static final Identifier NOT_SELECTED_PERMANENT = TJChase.prefix("container/mysterious_shop_entry_permanent");
    private static final Identifier SELECTED = TJChase.prefix("container/mysterious_shop_entry_highlighted");
    private static final Identifier MYSTERIOUS_SHOP_BACKGROUND = TJChase.prefix("textures/gui/container/mysterious_shop_background.png");
    private final Player player;
    private final int day;

    public MysteriousShopScreen(MysteriousShopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
        titleLabelX += 3;
        titleLabelY += 3;
//        titleLabelX = 12;
//        titleLabelY = 7;
//        inventoryLabelX = 8;
//        inventoryLabelY = 126;
        player = inventory.player;
        day = Utils.getDay(player.level());
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float partialTicks) {
        super.render(graphics, x, y, partialTicks);
        renderTooltip(graphics, x, y);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, MYSTERIOUS_SHOP_BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        int dx = mouseX - leftPos;
        int dy = mouseY - topPos;
        for (int i = 0; menu.hasInput() && i < menu.getTrades().size(); i++) {
            int y = topPos + TRADE_Y0 + i * TRADE_HEIGHT;
            if (dx >= TRADE_X && dx <= TRADE_X + TRADE_WIDTH && dy >= TRADE_Y0 + i * TRADE_HEIGHT && dy < TRADE_Y0 + (i + 1) * TRADE_HEIGHT) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SELECTED, leftPos + TRADE_X, y, TRADE_WIDTH, TRADE_HEIGHT);
                if (dx >= TRADE_X + TRADE_HEIGHT) {
                    graphics.setComponentTooltipForNextFrame(font, List.of(menu.getTrades().get(i).createDescription()), mouseX, mouseY);
                }
            } else {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, menu.getTrades().get(i).permanent() ? NOT_SELECTED_PERMANENT : NOT_SELECTED, leftPos + TRADE_X, y, TRADE_WIDTH, TRADE_HEIGHT);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFF404040, false);
//        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xff3e2c20, false);
        int remainingTicks = 24000 - Utils.getTimeOfDay(player.level());
        if (Utils.getDay(player.level()) != day) {
            remainingTicks = 0;
        }
        if (menu.hasInput() && !menu.getTrades().isEmpty()) {
            graphics.drawString(font, Component.translatable(MysteriousShopMenu.REFRESH_SECONDS, remainingTicks % 20 == 0 ? remainingTicks / 20 : remainingTicks / 20 + 1),
                    TRADE_X, TRADE_Y0 - 14, 0xFF404040, false);
        }
        for (int i = 0; menu.hasInput() && i < menu.getTrades().size(); i++) {
            if (i < menu.getTrades().size()) {
                MysteriousShopTrade trade = menu.getTrades().get(i);
                ItemStack result = trade.result(player.level(), menu.getSeed());
                Component name = trade.customName() != null ? trade.customName() : result.getHoverName();
                new Color(0x193B4F);
                graphics.drawString(font, name, TRADE_X + 22, TRADE_Y0 + 5 + i * TRADE_HEIGHT, 0XFF503B19, false);
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double x = event.x() - leftPos;
        double y = event.y() - topPos;
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && x >= TRADE_X && x <= TRADE_X + TRADE_WIDTH && y >= TRADE_Y0 && y < TRADE_Y0 + TRADE_HEIGHT * MysteriousShopMenu.VISIBLE_TRADE_COUNT) {
            int row = (int) ((y - TRADE_Y0) / TRADE_HEIGHT);
            if (minecraft.gameMode == null) {
                return false;
            }
            if (row >= 0 && row < MysteriousShopMenu.VISIBLE_TRADE_COUNT && menu.clickMenuButton(player, row)) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, row);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
