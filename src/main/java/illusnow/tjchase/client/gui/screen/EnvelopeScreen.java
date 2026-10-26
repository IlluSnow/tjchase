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
import illusnow.tjchase.item.EnvelopeItem;
import illusnow.tjchase.item.MysteriousEnvelopeItem;
import illusnow.tjchase.network.c2s.ClaimEnvelopeRewardPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.Nullable;

public class EnvelopeScreen extends Screen {
    private static final int PAGE_WIDTH = 220;
    private static final int PAGE_HEIGHT = 180;
    private static final Identifier BACKGROUND = TJChase.prefix("textures/gui/container/letter_background.png");
    private final ItemStack envelope;
    private final ItemStack reward;
    private final boolean offHand;
    private boolean claimed;
    @Nullable
    private Component greeting;

    public EnvelopeScreen(ItemStack envelope, ItemStack reward, boolean offHand) {
        super(Component.translatable(getTitleKey(envelope)));
        this.envelope = envelope;
        this.reward = reward;
        this.offHand = offHand;
    }

    private static String getTitleKey(ItemStack envelope) {
        return envelope.getItem() instanceof EnvelopeItem envelopeItem && envelopeItem.getEnvelopeTextKeys() != null ?
                envelopeItem.getEnvelopeTextKeys().title() : EnvelopeItem.ENVELOPE_TITLE;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int left = (width - PAGE_WIDTH) / 2;
        int top = (height - PAGE_HEIGHT) / 2;

//        guiGraphics.fill(left - 2, top - 2, left + PAGE_WIDTH + 2, top + PAGE_HEIGHT + 2, 0xff795a32);
//        guiGraphics.fill(left, top, left + PAGE_WIDTH, top + PAGE_HEIGHT, 0xffeee1bd);
//        guiGraphics.fill(left + 6, top + 6, left + PAGE_WIDTH - 6, top + PAGE_HEIGHT - 6, 0xfff5ebd1);
//        guiGraphics.fill(left + 20, top + 21, left + PAGE_WIDTH - 20, top + 22, 0xffd8c9a7);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, left, top, 0, 0, PAGE_WIDTH, PAGE_HEIGHT, PAGE_WIDTH, PAGE_HEIGHT);

        if (minecraft.player != null) {
            Component greeting = getOrCreateGreeting(minecraft.player);
            int textY = top + 35;
            int lineWidth = PAGE_WIDTH - 40;
            int start = width / 2 - lineWidth / 2;
            for (FormattedCharSequence line : font.split(greeting, lineWidth)) {
                guiGraphics.drawString(font, line, start, textY, 0xff493c2b, false);
                textY += font.lineHeight + 3;
            }

            int itemY = top + 119;
            if (envelope.getItem() instanceof MysteriousEnvelopeItem) {
                itemY += 20;
            }
            guiGraphics.renderItem(reward, width / 2 - 8, itemY);
            guiGraphics.renderItemDecorations(font, reward, width / 2 - 8, itemY);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private Component getOrCreateGreeting(@Nullable Player player) {
        if (player == null) {
            return Component.empty();
        }
        if (greeting == null) {
            Item item = envelope.getItem();
            if (item instanceof EnvelopeItem envelopeItem) {
                EnvelopeItem.EnvelopeTextKeys textKeys = envelopeItem.getEnvelopeTextKeys();
                if (textKeys != null) {
                    greeting = Component.translatable(player.getRandom().nextDouble() < 0.2 ? textKeys.greetingBadWeather() : textKeys.greeting(), player.getDisplayName());
                } else {
                    greeting = Component.empty();
                }
            } else {
                greeting = Component.empty();
            }
        }
        return greeting;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (keyEvent.isEscape() && shouldCloseOnEsc()) {
            onClose();
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
            onClose();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), getOrCreateGreeting(minecraft.player));
    }

    @Override
    public void onClose() {
        if (!claimed) {
            claimed = true;
            ClientPacketDistributor.sendToServer(new ClaimEnvelopeRewardPayload(offHand));
        }
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
