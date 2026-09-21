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

import illusnow.tjchase.client.gui.EditableValueList;
import illusnow.tjchase.util.ModComponents;
import illusnow.tjchase.world.gameplay.object.GameplayObject;
import illusnow.tjchase.world.gameplay.object.Template;
import illusnow.tjchase.world.gameplay.object.editablevalue.EditableValue;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class GameplayObjectEditScreen<O> extends Screen {
    private final GameplayObject<?> gameplayObj;
    private final Template<O> template;
    private final Map<EditableValue<?, O>, Object> tempValueMap = new HashMap<>();
    private final Set<EditableValue<?, ?>> invalid = new HashSet<>();
    private String originalName;
    private String tempName;
    private boolean nameChanged;
    private EditableValueList<O> list;
    private EditBox rocketName;
    private Button doneButton;
    private Button cancelButton;
    private Button resetButton;

    public GameplayObjectEditScreen(Component title, GameplayObject<?> gameplayObj, Template<O> template) {
        super(title);
        this.gameplayObj = gameplayObj;
        this.template = template;
    }

    @Override
    protected void init() {
        super.init();
        tempValueMap.putAll(template.seeValueMap());
        originalName = gameplayObj.getName().getString();
        tempName = originalName;
        rocketName = addRenderableWidget(
                new EditBox(font, width / 2 - 150, height / 14, 300, 18, ModComponents.CONTAINER_RENAME)
        );
        rocketName.setBordered(false);
        rocketName.setFilter(s -> !s.isBlank());
        rocketName.setMaxLength(50);
        rocketName.setCentered(true);
        rocketName.addFormatter((text, displayPos) -> gameplayObj.hasCustomName() || nameChanged ? FormattedCharSequence.forward(text, Style.EMPTY.withItalic(true)) : null);
        rocketName.setValue(gameplayObj.getName().getString());
        rocketName.setResponder(this::onNameChanged);
        list = new EditableValueList<>(minecraft, this, template, width, height * 2 / 3, height * 3 / 20, 24);
        list.init(tempValueMap);
        addRenderableWidget(list);
        doneButton = addRenderableWidget(
                Button.builder(CommonComponents.GUI_DONE, button -> onDone()).bounds(width / 2 - 9 - 150, height * 7 / 8, 100, 20).build()
        );
        cancelButton = addRenderableWidget(
                Button.builder(CommonComponents.GUI_CANCEL, button -> onClose()).bounds(width / 2 - 3 - 50, height * 7 / 8, 100, 20).build()
        );
        resetButton = addRenderableWidget(
                Button.builder(ModComponents.GUI_RESET_TO_DEFAULT, button -> onReset()).bounds(width / 2 + 3 + 50, height * 7 / 8, 100, 20).build()
        );
    }

    @Override
    public void setFocused(@Nullable GuiEventListener listener) {
        if (listener == resetButton) {
            return;
        }
        super.setFocused(listener);
    }

    private void onNameChanged(String name) {
        nameChanged = !Objects.equals(name, originalName);
        tempName = name;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void tick() {
        super.tick();
        if (!gameplayObj.isValid()) {
            onClose();
        }
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (super.keyPressed(keyEvent)) {
            return true;
        } else if (keyEvent.isConfirmation() && invalid.isEmpty()) {
            onDone();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    private void onDone() {
        ClientPacketDistributor.sendToServer(gameplayObj.createServerboundPayload(new Template<>(tempValueMap), nameChanged ? tempName : ""));
        minecraft.setScreen(null);
    }

    private void onReset() {
        for (var entry : tempValueMap.entrySet()) {
            entry.setValue(entry.getKey().defaultValue());
        }
        list.reset();
    }

    public void markInvalid(EditableValue<?, ?> editableValue) {
        invalid.add(editableValue);
        doneButton.active = false;
    }

    public void clearInvalid(EditableValue<?, ?> editableValue) {
        invalid.remove(editableValue);
        doneButton.active = invalid.isEmpty();
    }
}
