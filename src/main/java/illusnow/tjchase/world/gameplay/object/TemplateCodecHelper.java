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
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import illusnow.tjchase.util.ModRegistries;
import illusnow.tjchase.world.gameplay.object.editablevalue.EditableValue;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class TemplateCodecHelper {
    private TemplateCodecHelper() {}

    static <O> void encodeAll(RegistryFriendlyByteBuf buf, Template<O> template) {
        List<Pair<EditableValue<?, O>, Object>> values = template.createPairList();
        buf.writeVarInt(template.size());
        for (Pair<EditableValue<?, O>, Object> keyValuePair : values) {
            encodeSingleValue(buf, keyValuePair.getFirst(), keyValuePair.getSecond());
        }
    }

    @SuppressWarnings("unchecked")
    static <O> Template<O> decodeAll(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        ImmutableList.Builder<Pair<EditableValue<?, O>, Object>> builder = ImmutableList.builder();
        for (int i = 0; i < size; i++) {
            int id = buf.readVarInt();
            EditableValue<?, ?> key = buf.registryAccess().lookupOrThrow(ModRegistries.EDITABLE_VALUES_KEY).byId(id);
            Objects.requireNonNull(key, "EditableValue with id " + id + " was not found");
            Object value = decodeSingleValue(buf, key);
            builder.add(Pair.of((EditableValue<?, O>) key, value));
        }
        return new Template<>(builder.build());
    }

    @SuppressWarnings("unchecked")
    private static <T, O> void encodeSingleValue(RegistryFriendlyByteBuf buf, EditableValue<T, O> key, Object value) {
        int id = buf.registryAccess().lookupOrThrow(ModRegistries.EDITABLE_VALUES_KEY).getId(key);
        buf.writeVarInt(id);
        StreamCodec<? super RegistryFriendlyByteBuf, T> codec = key.getBehavior().streamCodec();
        codec.encode(buf, (T) value);
    }

    private static <T, O> T decodeSingleValue(RegistryFriendlyByteBuf buf, EditableValue<T, O> editableValue) {
        StreamCodec<? super RegistryFriendlyByteBuf, T> codec = editableValue.getBehavior().streamCodec();
        return codec.decode(buf);
    }

    static <T> DataResult<T> writeAll(Template<?> template, DynamicOps<T> ops, T prefix) {
        ListBuilder<T> builder = ops.listBuilder();

        for (Pair<? extends EditableValue<?, ?>, Object> pair : template.createPairList()) {
            EditableValue<?, ?> key = pair.getFirst();
            Object value = pair.getSecond();

            RecordBuilder<T> entryBuilder = ops.mapBuilder();
            entryBuilder.add("key", EditableValue.CODEC.encodeStart(ops, key));
            entryBuilder.add("value", encodeValue(key, value, ops));

            builder.add(entryBuilder.build(ops.empty()));
        }

        return builder.build(prefix);
    }

    @SuppressWarnings("unchecked")
    private static <V, T> DataResult<T> encodeValue(EditableValue<V, ?> key, Object value, DynamicOps<T> ops) {
        return key.getBehavior().codec().encodeStart(ops, (V) value);
    }

    @SuppressWarnings("unchecked")
    public static <T> DataResult<Pair<Template<?>, T>> readAll(DynamicOps<T> ops, T input) {
        return ops.getStream(input).flatMap(stream -> {
            List<Pair<EditableValue<?, Object>, Object>> pairList = new ArrayList<>();
            DataResult<Unit> result = DataResult.success(Unit.INSTANCE, Lifecycle.stable());

            for (T entryTag : (Iterable<T>) stream::iterator) {
                DataResult<Pair<EditableValue<?, Object>, Object>> entryResult = ops.getMap(entryTag).flatMap(mapLike -> {
                    T keyData = mapLike.get("key");
                    T valData = mapLike.get("value");

                    if (keyData == null || valData == null) {
                        return DataResult.error(() -> "Missing 'key' or 'value' field in entry: " + entryTag);
                    }

                    return EditableValue.CODEC.parse(ops, keyData).flatMap(key -> {
                        EditableValue<?, Object> typedKey = (EditableValue<?, Object>) key;
                        return typedKey.getBehavior().codec().parse(ops, valData)
                                .map(value -> Pair.of(typedKey, value));
                    });
                });

                result = result.apply2stable((u, pair) -> {
                    pairList.add(pair);
                    return Unit.INSTANCE;
                }, entryResult);
            }

            return result.map(u -> Pair.of(new Template<>(pairList), ops.empty()));
        });
    }
}
