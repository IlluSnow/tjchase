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
import illusnow.tjchase.util.WeightedBlockSelector;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Unit;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public final class ModEnchantmentEffectComponents {
    public static final DeferredRegister<DataComponentType<?>> ENCHANTMENT_COMPONENT_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, TJChase.MODID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EnchantmentValueEffect>> ADDITIONAL_ORBITING_BLOCK_COUNT
            = ENCHANTMENT_COMPONENT_TYPES.register("additional_orbiting_block_count", () ->
            DataComponentType.<EnchantmentValueEffect>builder()
                    .persistent(EnchantmentValueEffect.CODEC)
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EnchantmentValueEffect>> USE_COOLDOWN_TICKS_DECREASE
            = ENCHANTMENT_COMPONENT_TYPES.register("use_cooldown_ticks_decrease", () ->
            DataComponentType.<EnchantmentValueEffect>builder()
                    .persistent(EnchantmentValueEffect.CODEC)
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EnchantmentValueEffect>> PLAY_COOLDOWN_TICKS_DECREASE
            = ENCHANTMENT_COMPONENT_TYPES.register("play_cooldown_ticks_decrease", () ->
            DataComponentType.<EnchantmentValueEffect>builder()
                    .persistent(EnchantmentValueEffect.CODEC)
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EnchantmentValueEffect>> PLAY_TICKS_DECREASE
            = ENCHANTMENT_COMPONENT_TYPES.register("play_ticks_decrease", () ->
            DataComponentType.<EnchantmentValueEffect>builder()
                    .persistent(EnchantmentValueEffect.CODEC)
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<Holder<SoundEvent>>>> HARP_ATTRACT_SOUND
            = ENCHANTMENT_COMPONENT_TYPES.register("harp_attract_sound", () ->
            DataComponentType.<List<Holder<SoundEvent>>>builder()
                    .persistent(SoundEvent.CODEC.listOf())
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<Holder<SoundEvent>>>> HARP_PLAY_SOUND
            = ENCHANTMENT_COMPONENT_TYPES.register("harp_play_sound", () ->
            DataComponentType.<List<Holder<SoundEvent>>>builder()
                    .persistent(SoundEvent.CODEC.listOf())
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>>> SHOOT_VELOCITY
            = ENCHANTMENT_COMPONENT_TYPES.register("shoot_velocity", () ->
            DataComponentType.<List<ConditionalEffect<EnchantmentValueEffect>>>builder()
                    .persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ITEM).listOf())
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>>> SEEKING_RANGE
            = ENCHANTMENT_COMPONENT_TYPES.register("max_seeking_range", () ->
            DataComponentType.<List<ConditionalEffect<EnchantmentValueEffect>>>builder()
                    .persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ITEM).listOf())
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>>> SEEK_POWER
            = ENCHANTMENT_COMPONENT_TYPES.register("seek_power", () ->
            DataComponentType.<List<ConditionalEffect<EnchantmentValueEffect>>>builder()
                    .persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ITEM).listOf())
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>>> AOE_DAMAGE
            = ENCHANTMENT_COMPONENT_TYPES.register("aoe_damage", () ->
            DataComponentType.<List<ConditionalEffect<EnchantmentValueEffect>>>builder()
                    .persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ITEM).listOf())
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>>> AOE_RADIUS
            = ENCHANTMENT_COMPONENT_TYPES.register("aoe_radius", () ->
            DataComponentType.<List<ConditionalEffect<EnchantmentValueEffect>>>builder()
                    .persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ITEM).listOf())
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>>> ANGEL_TOM_PASSIVE2_INTERVAL
            = ENCHANTMENT_COMPONENT_TYPES.register("angel_tom_passive2_interval", () ->
            DataComponentType.<List<ConditionalEffect<EnchantmentValueEffect>>>builder()
                    .persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf())
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>>> ANGEL_TOM_PASSIVE2_HEAL_COUNT
            = ENCHANTMENT_COMPONENT_TYPES.register("angel_tom_passive2_heal_count", () ->
            DataComponentType.<List<ConditionalEffect<EnchantmentValueEffect>>>builder()
                    .persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf())
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>>> ANGEL_TOM_PASSIVE2_HEAL_AMOUNT
            = ENCHANTMENT_COMPONENT_TYPES.register("angel_tom_passive2_heal_amount", () ->
            DataComponentType.<List<ConditionalEffect<EnchantmentValueEffect>>>builder()
                    .persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_DAMAGE).listOf())
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WeightedBlockSelector>> ANGEL_TOM_WEAPON2_BLOCK_SELECTOR
            = ENCHANTMENT_COMPONENT_TYPES.register("angel_tom_weapon2_block_selector", () ->
            DataComponentType.<WeightedBlockSelector>builder()
                    .persistent(WeightedBlockSelector.CODEC)
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>>> MINIMUM_HEALTH_WHEN_BLOCKS_ORBITING
            = ENCHANTMENT_COMPONENT_TYPES.register("minimum_health_when_blocks_orbiting", () ->
            DataComponentType.<List<ConditionalEffect<EnchantmentValueEffect>>>builder()
                    .persistent(ConditionalEffect.codec(EnchantmentValueEffect.CODEC, LootContextParamSets.ENCHANTED_ITEM).listOf())
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> NO_GRAVITY
            = ENCHANTMENT_COMPONENT_TYPES.register("no_gravity", () -> DataComponentType.<Unit>builder().persistent(Unit.CODEC).build());

    private ModEnchantmentEffectComponents() {}
}
