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

package illusnow.tjchase.item.enchantment;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.sound.ModSoundEvents;
import illusnow.tjchase.tag.ModItemTags;
import illusnow.tjchase.util.AngelTomPassive2Tracker;
import illusnow.tjchase.util.HarpConstants;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.EntityTypePredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.item.enchantment.effects.MultiplyValue;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.neoforged.neoforge.common.Tags;

import java.util.List;

public final class ModEnchantments {
    public static final ResourceKey<Enchantment> FORCEFUL = key("forceful");
    public static final ResourceKey<Enchantment> EXPLOSIVE = key("explosive");
    public static final ResourceKey<Enchantment> OVERLOAD = key("overload");
    public static final ResourceKey<Enchantment> LIGHTWEIGHT = key("lightweight");
    public static final ResourceKey<Enchantment> ANTIGRAVITY = key("antigravity");
    public static final ResourceKey<Enchantment> SEEKING = key("seeking");
    public static final ResourceKey<Enchantment> ANGEL_TOM_PASSIVE_2 = key("angel_tom_passive_2");
    public static final ResourceKey<Enchantment> ANGEL_TOM_WEAPON_2 = key("angel_tom_weapon_2");
    public static final ResourceKey<Enchantment> ANGEL_TOM_WEAPON_3 = key("angel_tom_weapon_3");

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, TJChase.prefix(name));
    }

    public static void bootstrap(BootstrapContext<Enchantment> bootstrap) {
        HolderGetter<Item> itemHolderGetter = bootstrap.lookup(Registries.ITEM);
        HolderGetter<EntityType<?>> entityTypeHolderGetter = bootstrap.lookup(Registries.ENTITY_TYPE);
        bootstrap.register(
                FORCEFUL,
                Enchantment
                        .enchantment(Enchantment.definition(itemHolderGetter.getOrThrow(ModItemTags.HARPS),
                                10,
                                5,
                                Enchantment.dynamicCost(5, 10),
                                Enchantment.dynamicCost(25, 10),
                                1,
                                EquipmentSlotGroup.MAINHAND)
                        )
                        .withEffect(EnchantmentEffectComponents.DAMAGE, new AddValue(LevelBasedValue.perLevel(HarpConstants.FORCEFUL_ENCHANTMENT_DAMAGE_BOOST_PER_LEVEL)),
                                LootItemEntityPropertyCondition.hasProperties(
                                        LootContext.EntityTarget.DIRECT_ATTACKER,
                                        EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(entityTypeHolderGetter, ModEntities.ORBITING_BLOCK.get()))
                                ))
                        .withEffect(EnchantmentEffectComponents.DAMAGE, new AddValue(LevelBasedValue.perLevel(HarpConstants.FORCEFUL_ENCHANTMENT_DAMAGE_BOOST_BOSS_PER_LEVEL)),
                                AllOfCondition.allOf(
                                        LootItemEntityPropertyCondition.hasProperties(
                                                LootContext.EntityTarget.THIS,
                                                EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(entityTypeHolderGetter, Tags.EntityTypes.BOSSES))
                                        ),
                                        LootItemEntityPropertyCondition.hasProperties(
                                                LootContext.EntityTarget.DIRECT_ATTACKER,
                                                EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(entityTypeHolderGetter, ModEntities.ORBITING_BLOCK.get()))
                                        )
                                ))
                        .build(FORCEFUL.identifier())
        );
        bootstrap.register(
                EXPLOSIVE,
                Enchantment
                        .enchantment(Enchantment.definition(itemHolderGetter.getOrThrow(ModItemTags.HARPS),
                                8,
                                3,
                                Enchantment.dynamicCost(5, 13),
                                Enchantment.dynamicCost(25, 13),
                                2,
                                EquipmentSlotGroup.MAINHAND))
                        .withEffect(ModEnchantmentEffectComponents.AOE_DAMAGE.get(), new AddValue(LevelBasedValue.perLevel(HarpConstants.EXPLOSIVE_ENCHANTMENT_BASE_AOE_DAMAGE, HarpConstants.EXPLOSIVE_ENCHANTMENT_AOE_DAMAGE_PER_LEVEL)))
                        .withEffect(ModEnchantmentEffectComponents.AOE_RADIUS.get(), new AddValue(LevelBasedValue.perLevel(HarpConstants.EXPLOSIVE_ENCHANTMENT_BASE_AOE_RADIUS, HarpConstants.EXPLOSIVE_ENCHANTMENT_AOE_RADIUS_PER_LEVEL)))
                        .build(EXPLOSIVE.identifier())
        );
        bootstrap.register(
                OVERLOAD,
                Enchantment
                        .enchantment(Enchantment.definition(itemHolderGetter.getOrThrow(ModItemTags.HARPS),
                                5,
                                3,
                                Enchantment.dynamicCost(10, 12),
                                Enchantment.dynamicCost(50, 12),
                                2,
                                EquipmentSlotGroup.MAINHAND)
                        )
                        .withSpecialEffect(ModEnchantmentEffectComponents.ADDITIONAL_ORBITING_BLOCK_COUNT.get(), new AddValue(LevelBasedValue.perLevel(1)))
                        .withSpecialEffect(ModEnchantmentEffectComponents.USE_COOLDOWN_TICKS_DECREASE.get(), new AddValue(LevelBasedValue.perLevel(HarpConstants.OVERLOAD_ENCHANTMENT_USE_COOLDOWN_TICKS_DECREASE_PER_LEVEL)))
                        .build(OVERLOAD.identifier())
        );
        bootstrap.register(
                LIGHTWEIGHT,
                Enchantment
                        .enchantment(Enchantment.definition(itemHolderGetter.getOrThrow(ModItemTags.HARPS),
                                5,
                                3,
                                Enchantment.dynamicCost(10, 12),
                                Enchantment.dynamicCost(50, 12),
                                2,
                                EquipmentSlotGroup.MAINHAND)
                        )
                        .withSpecialEffect(ModEnchantmentEffectComponents.PLAY_COOLDOWN_TICKS_DECREASE.get(), new AddValue(LevelBasedValue.perLevel(HarpConstants.LIGHTWEIGHT_ENCHANTMENT_PLAY_COOLDOWN_TICKS_DECREASE_PER_LEVEL)))
                        .withSpecialEffect(ModEnchantmentEffectComponents.PLAY_TICKS_DECREASE.get(), new AddValue(LevelBasedValue.perLevel(HarpConstants.LIGHTWEIGHT_ENCHANTMENT_PLAY_TICKS_DECREASE_PER_LEVEL)))
                        .withSpecialEffect(ModEnchantmentEffectComponents.HARP_ATTRACT_SOUND.get(), List.of(ModSoundEvents.HARP_ATTRACT_BLOCKS_LIGHTWEIGHT_1, ModSoundEvents.HARP_ATTRACT_BLOCKS_LIGHTWEIGHT_2, ModSoundEvents.HARP_ATTRACT_BLOCKS_LIGHTWEIGHT_3))
                        .withSpecialEffect(ModEnchantmentEffectComponents.HARP_PLAY_SOUND.get(), List.of(ModSoundEvents.HARP_THROW_BLOCK_LIGHTWEIGHT_1, ModSoundEvents.HARP_THROW_BLOCK_LIGHTWEIGHT_2, ModSoundEvents.HARP_THROW_BLOCK_LIGHTWEIGHT_3))
                        .build(LIGHTWEIGHT.identifier())
        );
        bootstrap.register(
                ANTIGRAVITY,
                Enchantment
                        .enchantment(Enchantment.definition(itemHolderGetter.getOrThrow(ModItemTags.HARPS),
                                2,
                                1,
                                Enchantment.constantCost(20),
                                Enchantment.constantCost(50),
                                4,
                                EquipmentSlotGroup.MAINHAND))
                        .withEffect(ModEnchantmentEffectComponents.NO_GRAVITY.get())
                        .withEffect(ModEnchantmentEffectComponents.SHOOT_VELOCITY.get(), new MultiplyValue(LevelBasedValue.constant(HarpConstants.ANTIGRAVITY_SHOOT_VELOCITY_MULTIPLIER)))
                        .build(ANTIGRAVITY.identifier())
        );
        bootstrap.register(
                SEEKING,
                Enchantment
                        .enchantment(Enchantment.definition(itemHolderGetter.getOrThrow(ModItemTags.HARPS),
                                2,
                                2,
                                Enchantment.dynamicCost(15, 15),
                                Enchantment.dynamicCost(35, 25),
                                3,
                                EquipmentSlotGroup.MAINHAND))
                        .withEffect(ModEnchantmentEffectComponents.SEEKING_RANGE.get(), new AddValue(LevelBasedValue.perLevel(HarpConstants.SEEKING_ENCHANTMENT_SEEKING_RANGE_PER_LEVEL)))
                        .withEffect(ModEnchantmentEffectComponents.SEEK_POWER.get(), new AddValue(LevelBasedValue.perLevel(HarpConstants.SEEKING_ENCHANTMENT_BASE_SEEK_POWER, HarpConstants.SEEKING_ENCHANTMENT_SEEK_POWER_PER_LEVEL)))
                        .build(SEEKING.identifier())
        );
        bootstrap.register(
                ANGEL_TOM_PASSIVE_2,
                Enchantment
                        .enchantment(Enchantment.definition(itemHolderGetter.getOrThrow(ModItemTags.HARPS),
                                2,
                                1,
                                Enchantment.dynamicCost(25, 25),
                                Enchantment.dynamicCost(75, 25),
                                4,
                                EquipmentSlotGroup.MAINHAND))
                        .withEffect(ModEnchantmentEffectComponents.ANGEL_TOM_PASSIVE2_INTERVAL.get(), new AddValue(LevelBasedValue.constant(AngelTomPassive2Tracker.DEFAULT_INTERVAL)))
                        .withEffect(ModEnchantmentEffectComponents.ANGEL_TOM_PASSIVE2_HEAL_COUNT.get(), new AddValue(LevelBasedValue.constant(AngelTomPassive2Tracker.DEFAULT_HEAL_COUNT)))
                        .withEffect(ModEnchantmentEffectComponents.ANGEL_TOM_PASSIVE2_HEAL_AMOUNT.get(), new AddValue(LevelBasedValue.constant(AngelTomPassive2Tracker.DEFAULT_HEAL_AMOUNT)))
                        .build(ANGEL_TOM_PASSIVE_2.identifier())
        );
        bootstrap.register(
                ANGEL_TOM_WEAPON_2,
                Enchantment
                        .enchantment(Enchantment.definition(itemHolderGetter.getOrThrow(ModItemTags.HARPS),
                                2,
                                1,
                                Enchantment.dynamicCost(25, 25),
                                Enchantment.dynamicCost(75, 25),
                                4,
                                EquipmentSlotGroup.MAINHAND))
                        .withSpecialEffect(ModEnchantmentEffectComponents.ANGEL_TOM_WEAPON2_BLOCK_SELECTOR.get(), HarpConstants.DEFAULT_WEAPON2_BLOCK_SELECTOR)
                        .build(ANGEL_TOM_WEAPON_2.identifier())
        );
        bootstrap.register(
                ANGEL_TOM_WEAPON_3,
                Enchantment
                        .enchantment(Enchantment.definition(itemHolderGetter.getOrThrow(ModItemTags.HARPS),
                                2,
                                1,
                                Enchantment.dynamicCost(25, 25),
                                Enchantment.dynamicCost(75, 25),
                                4,
                                EquipmentSlotGroup.MAINHAND))
                        .withEffect(ModEnchantmentEffectComponents.MINIMUM_HEALTH_WHEN_BLOCKS_ORBITING.get(), new AddValue(LevelBasedValue.constant(HarpConstants.ANGEL_TOM_WEAPON3_ENCHANTMENT_MINIMUM_HEALTH)))
                        .build(ANGEL_TOM_WEAPON_3.identifier())
        );
    }

    public static String getEnchantmentDescriptionId(ResourceKey<Enchantment> key) {
        return Util.makeDescriptionId("enchantment", key.identifier());
    }
}