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

package illusnow.tjchase.entity;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.entity.projectile.OrbitingBlockEntity;
import illusnow.tjchase.util.Utils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;

public final class ModEntityDataSerializers {
    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, TJChase.MODID);
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<AABB>> AABB
            = ENTITY_DATA_SERIALIZERS.register("aabb", () -> EntityDataSerializer.forValueType(Utils.AABB_STREAM_CODEC));
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<Double>> DOUBLE
            = ENTITY_DATA_SERIALIZERS.register("double", () -> EntityDataSerializer.forValueType(ByteBufCodecs.DOUBLE));
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<OrbitingBlockEntity.Properties>> ORBITING_BLOCK_ENTITY_PROPERTIES
            = ENTITY_DATA_SERIALIZERS.register("orbiting_block_entity_properties", () -> EntityDataSerializer.forValueType(OrbitingBlockEntity.Properties.STREAM_CODEC));
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<Optional<EntityReference<Entity>>>> OPTIONAL_ENTITY_REFERENCE
            = ENTITY_DATA_SERIALIZERS.register("optional_entity_reference", () -> EntityDataSerializer.forValueType(EntityReference.<Entity>streamCodec().apply(ByteBufCodecs::optional)));
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<Optional<EntityReference<Player>>>> OPTIONAL_PLAYER_REFERENCE
            = ENTITY_DATA_SERIALIZERS.register("optional_player_reference", () -> EntityDataSerializer.forValueType(EntityReference.<Player>streamCodec().apply(ByteBufCodecs::optional)));

    private ModEntityDataSerializers() {}
}
