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

package illusnow.tjchase.data.loot;

import illusnow.tjchase.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

import static net.minecraft.world.level.storage.loot.entries.LootItem.lootTableItem;

public class CommonReplyLetterRewardLootSubProvider implements LootTableSubProvider {
    private final HolderLookup.Provider registries;

    public CommonReplyLetterRewardLootSubProvider(HolderLookup.Provider registries) {
        this.registries = registries;
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(ModLootTables.COMMON_REPLY_LETTER_REWARD, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(lootTableItem(Items.NETHERITE_INGOT).setWeight(1))
                        .add(lootTableItem(Items.ENCHANTED_GOLDEN_APPLE).setWeight(1))
                        .add(lootTableItem(Items.TOTEM_OF_UNDYING).setWeight(2))
                        .add(lootTableItem(Items.BOOK).setWeight(2).apply(
                                EnchantRandomlyFunction.randomEnchantment().withOneOf(HolderSet.direct(
                                        registries.lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.MENDING).orElseThrow(),
                                        registries.lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.FROST_WALKER).orElseThrow(),
                                        registries.lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.SWIFT_SNEAK).orElseThrow(),
                                        registries.lookupOrThrow(Registries.ENCHANTMENT).get(Enchantments.WIND_BURST).orElseThrow()
                                ))
                        ))
                        .add(lootTableItem(Items.GOLDEN_APPLE).setWeight(5))
                        .add(lootTableItem(Items.NETHERITE_SCRAP).setWeight(5))
                        .add(lootTableItem(Items.DIAMOND).setWeight(5))
                        .add(lootTableItem(Items.EMERALD).setWeight(5))
                        .add(lootTableItem(Items.BREEZE_ROD).setWeight(10))
                        .add(lootTableItem(Items.BLAZE_ROD).setWeight(10))
                        .add(lootTableItem(Items.GOLDEN_CARROT).setWeight(10))
                        .add(lootTableItem(Items.GLISTERING_MELON_SLICE).setWeight(10))
                        .add(lootTableItem(Items.ENDER_PEARL).setWeight(10))
                        .add(lootTableItem(Items.ENDER_EYE).setWeight(10))
                        .add(lootTableItem(Items.AMETHYST_SHARD).setWeight(10))
                        .add(lootTableItem(Items.GOLD_INGOT).setWeight(10))
                        .add(lootTableItem(Items.BOOK).setWeight(10).apply(
                                EnchantWithLevelsFunction.enchantWithLevels(registries, UniformGenerator.between(5, 15))
                        ))
                        .add(lootTableItem(Items.IRON_INGOT).setWeight(20))
                        .add(lootTableItem(Items.LAPIS_LAZULI).setWeight(20))
                        .add(lootTableItem(Items.COPPER_INGOT).setWeight(20))
                        .add(lootTableItem(Items.REDSTONE).setWeight(20))
                        .add(lootTableItem(Items.EXPERIENCE_BOTTLE).setWeight(20))
                        .add(lootTableItem(Items.COAL).setWeight(25))
                        .add(lootTableItem(Items.APPLE).setWeight(25))
                        .add(lootTableItem(Items.BREAD).setWeight(25))
                        .add(lootTableItem(Items.ARROW).setWeight(25))
                        .add(lootTableItem(Items.BONE).setWeight(25))
                        .add(lootTableItem(Items.STRING).setWeight(25))
                        .add(lootTableItem(Items.STICK).setWeight(25))
                        .add(lootTableItem(Items.GUNPOWDER).setWeight(25))
                        .add(lootTableItem(Items.POTATO).setWeight(25))
                        .add(lootTableItem(Items.CARROT).setWeight(25))
                        .add(lootTableItem(Items.LEATHER).setWeight(25))
                        .add(lootTableItem(ModItems.NETHERITE_HARP.get()).setWeight(1))
                        .add(lootTableItem(ModItems.HARP.get()).setWeight(12))
                        .add(lootTableItem(ModItems.VINE_SEED.get()).setWeight(35).apply(SetItemCountFunction.setCount(ConstantValue.exactly(3))))
                        .add(lootTableItem(ModItems.BLUEPRINT.get()).setWeight(35).apply(SetItemCountFunction.setCount(ConstantValue.exactly(3))))));
    }
}
