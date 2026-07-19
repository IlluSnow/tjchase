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
import illusnow.tjchase.entity.projectile.YogaBall;
import illusnow.tjchase.util.HarpConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, TJChase.MODID);
    public static final DeferredHolder<EntityType<?>, EntityType<HarpTester>> HARP_TESTER = register(ModEntityNames.HARP_TESTER,
            EntityType.Builder.of(HarpTester::new, MobCategory.MISC).noLootTable().noSummon().sized(0, 0).clientTrackingRange(5).updateInterval(20));
    public static final DeferredHolder<EntityType<?>, EntityType<OrbitingBlockEntity>> ORBITING_BLOCK = register(ModEntityNames.ORBITING_BLOCK,
            EntityType.Builder.<OrbitingBlockEntity>of(OrbitingBlockEntity::new, MobCategory.MISC).noLootTable().sized(HarpConstants.DEFAULT_ORBITING_BLOCK_SIZE, HarpConstants.DEFAULT_ORBITING_BLOCK_SIZE).clientTrackingRange(8).updateInterval(10));
    public static final DeferredHolder<EntityType<?>, EntityType<VineManager>> VINE_MANAGER = register(ModEntityNames.VINE_MANAGER,
            EntityType.Builder.<VineManager>of(VineManager::new, MobCategory.MISC).noLootTable().noSummon().sized(0, 0).clientTrackingRange(5).updateInterval(20));
    public static final DeferredHolder<EntityType<?>, EntityType<VineSeed>> VINE_SEED = register(ModEntityNames.VINE_SEED,
            EntityType.Builder.<VineSeed>of(VineSeed::new, MobCategory.MISC).noLootTable().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
    public static final DeferredHolder<EntityType<?>, EntityType<YogaBall>> YOGA_BALL = register(ModEntityNames.YOGA_BALL,
            EntityType.Builder.<YogaBall>of(YogaBall::new, MobCategory.MISC).noLootTable().sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20));
    public static final DeferredHolder<EntityType<?>, EntityType<Zuri>> ZURI = register(ModEntityNames.ZURI,
            EntityType.Builder.of(Zuri::new, MobCategory.CREATURE).sized(0.45F, 1.33F).eyeHeight(1.05F).clientTrackingRange(8));

    private ModEntities() {}

    private static ResourceKey<EntityType<?>> createEntityId(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE, TJChase.prefix(name));
    }

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = createEntityId(name);
        return ENTITY_TYPES.register(name, () -> builder.build(key));
    }
}
