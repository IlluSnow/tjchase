package illusnow.tjchase.entity;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.entity.projectile.OrbitingBlockEntity;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;

public final class ModEntityDataSerializers {
    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, TJChase.MODID);
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<OrbitingBlockEntity.Properties>> ORBITING_BLOCK_ENTITY_PROPERTIES
            = ENTITY_DATA_SERIALIZERS.register("orbiting_block_entity_properties", () -> EntityDataSerializer.forValueType(OrbitingBlockEntity.Properties.STREAM_CODEC));
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<Optional<EntityReference<Entity>>>> OPTIONAL_ENTITY_REFERENCE
            = ENTITY_DATA_SERIALIZERS.register("optional_entity_reference", () -> EntityDataSerializer.forValueType(EntityReference.<Entity>streamCodec().apply(ByteBufCodecs::optional)));
    private ModEntityDataSerializers() {}
}
