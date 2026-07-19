package illusnow.tjchase.attachment;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import illusnow.tjchase.TJChase;
import illusnow.tjchase.entity.HarpTester;
import illusnow.tjchase.entity.Zuri;
import illusnow.tjchase.util.AngelTomPassive2Tracker;
import illusnow.tjchase.util.DancingHelper;
import illusnow.tjchase.util.OrbitingBlockHolder;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;
import java.util.function.Predicate;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TJChase.MODID);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<AngelTomPassive2Tracker>> ANGEL_TOM_PASSIVE2_TRACKER = ATTACHMENT_TYPES.register(ModAttachmentNames.ANGEL_TOM_PASSIVE2_TRACKER, () ->
            AttachmentType.builder(AngelTomPassive2Tracker::createDefault).serialize(AngelTomPassive2Tracker.MAP_CODEC).build());
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
}
