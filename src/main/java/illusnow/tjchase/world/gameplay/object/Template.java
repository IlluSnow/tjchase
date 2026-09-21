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

package illusnow.tjchase.world.gameplay.object;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import illusnow.tjchase.world.gameplay.object.editablevalue.EditableValue;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class Template<O> implements TooltipProvider {
    public static final Codec<Template<?>> CODEC = Codec.of(
            TemplateCodecHelper::writeAll,
            TemplateCodecHelper::readAll
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, Template<?>> STREAM_CODEC = StreamCodec.of(
            TemplateCodecHelper::encodeAll,
            TemplateCodecHelper::decodeAll
    );
    private final Map<EditableValue<?, O>, Object> valueMap;

    public Template(Map<EditableValue<?, O>, Object> valueMap) {
        validate(valueMap);
        this.valueMap = ImmutableMap.copyOf(valueMap);
    }

    Template(List<Pair<EditableValue<?, O>, Object>> valuePairList) {
        this(valuePairList.stream().collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond)));
    }

    Template(List<EditableValue<?, O>> editableValues, O obj) {
        ImmutableMap.Builder<EditableValue<?, O>, Object> builder = ImmutableMap.builder();
        for (EditableValue<?, O> editableValue : editableValues) {
            builder.put(editableValue, editableValue.get(obj));
        }
        valueMap = validate(builder.build());
    }

    private static <O> Map<EditableValue<?, O>, Object> validate(Map<EditableValue<?, O>, Object> valueMap) {
        for (Map.Entry<EditableValue<?, O>, Object> entry : valueMap.entrySet()) {
            EditableValue<?, O> key = entry.getKey();
            Object value = entry.getValue();
            Objects.requireNonNull(value, "Value for key " + key + " is null");
            validateSingleEntry(key, value);
        }
        return valueMap;
    }

    @SuppressWarnings("unchecked")
    private static <T, O> void validateSingleEntry(EditableValue<T, O> key, Object value) {
        try {
            T castedValue = (T) value;
            T sanitizedValue = key.getBehavior().sanitize(castedValue);
            if (castedValue != sanitizedValue) {
                throw new IllegalArgumentException("Value for key " + key + " is not valid: " + castedValue);
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Value type does not match EditableValue type for key: " + key);
        }
    }

    public int size() {
        return valueMap.size();
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(EditableValue<T, ?> editableValue) {
        return (T) valueMap.get(editableValue);
    }

    public Map<EditableValue<?, O>, Object> seeValueMap() {
        return ImmutableMap.copyOf(valueMap);
    }

    public List<Pair<EditableValue<?, O>, Object>> createPairList() {
        ImmutableList.Builder<Pair<EditableValue<?, O>, Object>> builder = ImmutableList.builder();
        for (var editableValue : valueMap.entrySet()) {
            builder.add(Pair.of(editableValue.getKey(), editableValue.getValue()));
        }
        return builder.build();
    }

    public void loadTemplateData(O gameplayObj) {
        for (var entry : valueMap.entrySet()) {
            EditableValue<?, O> key = entry.getKey();
            Object value = entry.getValue();
            loadSingleEntry(gameplayObj, key, value);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void loadSingleEntry(O obj, EditableValue<T, O> key, Object value) {
        key.set(obj, (T) value);
    }

    public boolean isDefault() {
        for (var entry : valueMap.entrySet()) {
            EditableValue<?, O> key = entry.getKey();
            Object value = entry.getValue();
            if (!key.isDefault(value)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        //noinspection ConstantValue
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Template<?> template = (Template<?>) obj;
        return valueMap.equals(template.valueMap);
    }

    @Override
    public int hashCode() {
        return valueMap.hashCode();
    }

    @Override
    public String toString() {
        return "Template{" +
                "valueMap=" + valueMap +
                '}';
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag flag, DataComponentGetter componentGetter) {
        for (var entry : valueMap.entrySet()) {
            addSingle(entry, tooltipAdder);
        }
    }

    private static <O> void addSingle(Map.Entry<EditableValue<?, O>, Object> entry, Consumer<Component> tooltipAdder) {
        Component component = CommonComponents.optionNameValue(
                entry.getKey().createTranslation().withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true)),
                createValueTranslation(entry.getKey(), entry.getValue()).withStyle(Style.EMPTY.withItalic(!entry.getKey().isDefault(entry.getValue()))));
        tooltipAdder.accept(component);
    }

    @SuppressWarnings("unchecked")
    private static <T, O> MutableComponent createValueTranslation(EditableValue<T, O> editableValue, Object value) {
        return editableValue.getBehavior().createValueTranslation((T) value);
    }
}
