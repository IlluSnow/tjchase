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

package illusnow.tjchase.attachment;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import illusnow.tjchase.TJChase;
import illusnow.tjchase.entity.Zuri;
import illusnow.tjchase.entity.controllable.Controllable;
import illusnow.tjchase.entity.dataentity.HarpTester;
import illusnow.tjchase.util.AngelTomPassive2Tracker;
import illusnow.tjchase.util.DancingHelper;
import illusnow.tjchase.util.OrbitingBlockHolder;
import illusnow.tjchase.world.gameplay.WeakState;
import illusnow.tjchase.world.gameplay.action.ActionHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TJChase.MODID);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ActionHolder>> ACTION_HOLDER = ATTACHMENT_TYPES.register(ModAttachmentNames.ACTION_HOLDER, () ->
            AttachmentType.builder(ActionHolder::new).serialize(ActionHolder.Serializer.INSTANCE).sync(ActionHolder.Syncer.INSTANCE).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<AngelTomPassive2Tracker>> ANGEL_TOM_PASSIVE2_TRACKER = ATTACHMENT_TYPES.register(ModAttachmentNames.ANGEL_TOM_PASSIVE2_TRACKER, () ->
            AttachmentType.builder(AngelTomPassive2Tracker::createDefault).serialize(AngelTomPassive2Tracker.MAP_CODEC).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> BLUEPRINT_CONVERSION_IMMUNE_TICKS = ATTACHMENT_TYPES.register(ModAttachmentNames.BLUEPRINT_CONVERSION_IMMUNE_TICKS, () ->
            AttachmentType.builder(() -> 0L).serialize(Codec.LONG.fieldOf(ModAttachmentNames.BLUEPRINT_CONVERSION_IMMUNE_TICKS)).build());
    // Tracks how many BlueprintManager entities are currently loaded in a level, so the
    // nearby-blueprint entity search can be skipped entirely when none exist. Not persisted or synced.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<AtomicInteger>> BLUEPRINT_LIVE_COUNT = ATTACHMENT_TYPES.register(ModAttachmentNames.BLUEPRINT_LIVE_COUNT, () ->
            AttachmentType.builder(() -> new AtomicInteger()).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Controllable.Holder>> CONTROLLING_ENTITY = ATTACHMENT_TYPES.register(ModAttachmentNames.CONTROLLING_ENTITY, () ->
            AttachmentType.builder(() -> Controllable.Holder.EMPTY).serialize(Controllable.Holder.Serializer.INSTANCE).sync(Controllable.Holder.Syncer.INSTANCE).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<OrbitingBlockHolder>> ORBITING_BLOCKS = ATTACHMENT_TYPES.register(ModAttachmentNames.ORBITING_BLOCKS, () ->
            AttachmentType.builder(OrbitingBlockHolder::emptyHolder).serialize(OrbitingBlockHolder.Serializer.INSTANCE).sync(OrbitingBlockHolder.STREAM_CODEC).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> VINE_CD = ATTACHMENT_TYPES.register(ModAttachmentNames.VINE_CD, () ->
            AttachmentType.builder(() -> 0L).serialize(Codec.LONG.fieldOf(ModAttachmentNames.VINE_CD)).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<EntityReference<HarpTester>>>> HARP_TESTER = ATTACHMENT_TYPES.register(ModAttachmentNames.HARP_TESTER, () ->
            AttachmentType.<Optional<EntityReference<HarpTester>>>builder(Optional::empty).serialize(optionalSerializer(EntityReference.codec())).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<EntityReference<Zuri>>>> DANCING_WITH = ATTACHMENT_TYPES.register(ModAttachmentNames.DANCING_WITH, () ->
            AttachmentType.<Optional<EntityReference<Zuri>>>builder(Optional::empty).serialize(optionalSerializer(EntityReference.codec())).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<DancingHelper.DanceEffectType>> DANCE_EFFECT_TYPE = ATTACHMENT_TYPES.register(ModAttachmentNames.DANCE_EFFECT_TYPE, () ->
            AttachmentType.builder(holder -> DancingHelper.DanceEffectType.NONE).serialize(DancingHelper.DanceEffectType.MAP_CODEC).sync(DancingHelper.DanceEffectType.STREAM_CODEC).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> POSITIVE_EFFECT_BLUEPRINT = ATTACHMENT_TYPES.register(ModAttachmentNames.POSITIVE_EFFECT_BLUEPRINT, () ->
            AttachmentType.builder(holder -> false).serialize(Codec.BOOL.fieldOf(ModAttachmentNames.POSITIVE_EFFECT_BLUEPRINT)).sync(ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> NEGATIVE_EFFECT_BLUEPRINT = ATTACHMENT_TYPES.register(ModAttachmentNames.NEGATIVE_EFFECT_BLUEPRINT, () ->
            AttachmentType.builder(holder -> false).serialize(Codec.BOOL.fieldOf(ModAttachmentNames.NEGATIVE_EFFECT_BLUEPRINT)).sync(ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<EntityReference<Entity>>>> TIED_TO = ATTACHMENT_TYPES.register(ModAttachmentNames.TIED_TO, () ->
            AttachmentType.<Optional<EntityReference<Entity>>>builder(Optional::empty).serialize(optionalSerializer(EntityReference.codec())).sync(optionalSyncer(EntityReference.streamCodec())).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<EntityReference<Player>>>> TYING = ATTACHMENT_TYPES.register(ModAttachmentNames.TYING, () ->
            AttachmentType.<Optional<EntityReference<Player>>>builder(Optional::empty).serialize(optionalSerializer(EntityReference.codec())).sync(optionalSyncer(EntityReference.streamCodec())).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<WeakState>> WEAK_STATE = ATTACHMENT_TYPES.register(ModAttachmentNames.WEAK_STATE, () ->
            AttachmentType.builder(WeakState::new).serialize(WeakState.MAP_CODEC).sync(WeakState.STREAM_CODEC).build());

    private ModAttachments() {}

    private static <T> IAttachmentSerializer<Optional<T>> optionalSerializer(Codec<T> codec) {
        return optionalSerializer(codec, Predicates.alwaysTrue());
    }

    private static <T> IAttachmentSerializer<Optional<T>> optionalSerializer(Codec<T> codec, Predicate<? super Optional<T>> shouldSerialize) {
        return new IAttachmentSerializer<>() {
            final String present = "present";
            final String data = "data";

            @Override
            public Optional<T> read(IAttachmentHolder holder, ValueInput input) {
                if (!input.getBooleanOr(present, false)) {
                    return Optional.empty();
                }
                Optional<T> parsingResult = input.read(data, codec);
                if (parsingResult.isEmpty()) {
                    throw buildException("read");
                }
                return parsingResult;
            }

            @Override
            public boolean write(Optional<T> attachment, ValueOutput output) {
                if (!shouldSerialize.test(attachment)) {
                    return false;
                }
                output.putBoolean(present, attachment.isPresent());
                if (attachment.isPresent()) {
                    output.store(data, codec, attachment.orElseThrow(() -> buildException("write")));
                }
                return true;
            }

            private RuntimeException buildException(final String operation) {
                return new IllegalStateException("Unable to " + operation + " attachment due to an internal codec error.");
            }
        };
    }

    private static <T> AttachmentSyncHandler<Optional<T>> optionalSyncer(StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return new AttachmentSyncHandler<>() {
            @Override
            public void write(RegistryFriendlyByteBuf buf, Optional<T> attachment, boolean initialSync) {
                buf.writeBoolean(attachment.isPresent());
                attachment.ifPresent(at -> streamCodec.encode(buf, at));
            }

            @Override
            public Optional<T> read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable Optional<T> previousValue) {
                if (!buf.readBoolean()) {
                    return Optional.empty();
                }
                return Optional.of(streamCodec.decode(buf));
            }
        };
    }
}
