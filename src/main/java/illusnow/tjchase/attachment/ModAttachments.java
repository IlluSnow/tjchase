package illusnow.tjchase.attachment;

import com.mojang.serialization.Codec;
import illusnow.tjchase.TJChase;
import illusnow.tjchase.util.OrbitingBlockHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TJChase.MODID);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<OrbitingBlockHolder>> ORBITING_BLOCKS = ATTACHMENT_TYPES.register(ModAttachmentNames.ORBITING_BLOCKS, () ->
            AttachmentType.builder(OrbitingBlockHolder::emptyHolder).serialize(OrbitingBlockHolder.Serializer.INSTANCE).sync(OrbitingBlockHolder.STREAM_CODEC).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> VINE_CD = ATTACHMENT_TYPES.register(ModAttachmentNames.VINE_CD, () ->
            AttachmentType.builder(() -> 0L).serialize(Codec.LONG.fieldOf(ModAttachmentNames.VINE_CD)).build());

    private ModAttachments() {}
}
