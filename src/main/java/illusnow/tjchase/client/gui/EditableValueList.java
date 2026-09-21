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

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.mojang.datafixers.util.Pair;
import illusnow.tjchase.client.editablevalue.ClientEditableValueHelper;
import illusnow.tjchase.client.gui.screen.GameplayObjectEditScreen;
import illusnow.tjchase.world.gameplay.object.Template;
import illusnow.tjchase.world.gameplay.object.editablevalue.EditableValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.common.TranslatableEnum;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class EditableValueList<O> extends ContainerObjectSelectionList<EditableValueList.BaseEntry> {
    private final Template<O> template;
    private final GameplayObjectEditScreen<O> screen;

    public EditableValueList(Minecraft minecraft, GameplayObjectEditScreen<O> screen, Template<O> template, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
        this.template = template;
        this.screen = screen;
    }

    @Override
    public int getRowWidth() {
        return 300;
    }

    public void init(Map<EditableValue<?, O>, Object> tempValueMap) {
        List<Pair<EditableValue<?, O>, Object>> pairList = template.createPairList();
        for (Pair<EditableValue<?, O>, Object> pair : pairList) {
            addEntry(ClientEditableValueHelper.createEntry(this, pair.getFirst(), tempValueMap));
        }
    }

    public Template<O> getTemplate() {
        return template;
    }

    public void reset() {
        for (var child : children()) {
            child.reset();
        }
    }

    public abstract static class BaseEntry extends ContainerObjectSelectionList.Entry<BaseEntry> {
        protected final EditableValueList<?> list;
        protected final Component label;

        protected BaseEntry(EditableValueList<?> list, Component label) {
            this.list = list;
            this.label = label;
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
            Font font = Minecraft.getInstance().font;
            guiGraphics.drawString(font, label, getContentX(), getContentY() + (getContentHeight() - 9) / 2, 0xFFFFFFFF);
        }

        public void reset() {}
    }

    public static abstract class EditableValueEntry extends BaseEntry {
        protected final EditableValue<?, ?> editableValue;

        protected EditableValueEntry(EditableValueList<?> list, Component label, EditableValue<?, ?> editableValue) {
            super(list, label);
            this.editableValue = editableValue;
        }

        public abstract void reset();
    }

    public static class EnumEntry<E extends Enum<E> & StringRepresentable & TranslatableEnum> extends EditableValueEntry {
        private final CycleButton<E> button;

        public EnumEntry(EditableValueList<?> list, Component label, Template<?> template, EditableValue<E, ?> editableValue, Consumer<E> onValueChange) {
            super(list, label, editableValue);
            this.button = CycleButton.builder(TranslatableEnum::getTranslatedName, template.getValue(editableValue))
                    .withValues(Objects.requireNonNull(editableValue.getBehavior().getPossibleValues()))
                    .displayOnlyValue()
                    .create(0, 0, 100, 20, label, (btn, val) -> onValueChange.accept(val));
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
            super.renderContent(guiGraphics, mouseX, mouseY, isHovering, partialTick);
            button.setX(getContentX() + getContentWidth() - 100);
            button.setY(getContentY());
            button.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() { return List.of(button); }

        @Override
        public List<? extends NarratableEntry> narratables() { return List.of(button); }

        @SuppressWarnings("unchecked")
        @Override
        public void reset() {
            button.setValue((E) editableValue.defaultValue());
        }
    }

    public static class NumberEditBoxEntry extends EditableValueEntry {
        private final EditBox editBox;

        public NumberEditBoxEntry(EditableValueList<?> list, Component label, Template<?> template, EditableValue<? extends Number, ?> editableValue, boolean decimal, Consumer<String> onValueChange) {
            super(list, label, editableValue);
            Font font = Minecraft.getInstance().font;
            Component hint = editableValue.getBehavior().createHint();
            editBox = new EditBox(font, 0, 0, 98, 18, label) {
                @Override
                protected MutableComponent createNarrationMessage() {
                    if (hint == null) {
                        return super.createNarrationMessage();
                    }
                    return super.createNarrationMessage().append(CommonComponents.NARRATION_SEPARATOR).append(hint);
                }
            };
            editBox.setValue(String.valueOf(template.getValue(editableValue)));

            String regex = decimal ? "^-?\\d*(\\.\\d*)?$" : "^-?\\d*$";
            editBox.setFilter(s -> canInput(s, regex));
            editBox.setResponder(s -> {
                if (numberInRange(editableValue, s, decimal)) {
                    onValueChange.accept(s);
                    list.screen.clearInvalid(editableValue);
                    editBox.setTextColor(-2039584);
                } else {
                    list.screen.markInvalid(editableValue);
                    editBox.setTextColor(-65536);
                }
            });
            if (hint != null) {
                editBox.setTooltip(Tooltip.create(hint));
            }
        }

        private static boolean canInput(String s, String regex) {
            return s.isEmpty() || s.matches(regex);
        }

        private static boolean numberInRange(EditableValue<? extends Number, ?> editableValue, String numberString, boolean decimal) {
            if (numberString.isEmpty()) {
                return false;
            }
            Pair<? extends Number, ? extends Number> minMax = Objects.requireNonNull(editableValue.getBehavior().getMinMax());
            if (decimal) {
                Double number = Doubles.tryParse(numberString);
                if (number == null) {
                    return false;
                }
                return number >= minMax.getFirst().doubleValue() && number <= minMax.getSecond().doubleValue();
            } else {
                Integer number = Ints.tryParse(numberString);
                if (number == null) {
                    return false;
                }
                return number >= minMax.getFirst().intValue() && number <= minMax.getSecond().intValue();
            }
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
            super.renderContent(guiGraphics, mouseX, mouseY, isHovering, partialTick);
            editBox.setX(getContentX() + getContentWidth() - 100);
            editBox.setY(getContentY() + 1);
            editBox.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() { return List.of(editBox); }

        @Override
        public List<? extends NarratableEntry> narratables() { return List.of(editBox); }

        @Override
        public void reset() {
            editBox.setValue(String.valueOf(editableValue.defaultValue()));
        }
    }
}
