
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

package illusnow.tjchase.data;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.item.ModItemNames;
import illusnow.tjchase.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    protected ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        netheriteSmithing(ModItems.HARP.get(), RecipeCategory.COMBAT, ModItems.NETHERITE_HARP.get());
        shaped(RecipeCategory.MISC, ModItems.MAILBOX.get())
                .define('#', Tags.Items.INGOTS_IRON)
                .define('.', ModItems.STAMP.get())
                .pattern("###")
                .pattern("#.#")
                .pattern("###")
                .unlockedBy("has_stamp", has(ModItems.STAMP.get()))
                .save(output);
        shapeless(RecipeCategory.MISC, ModItems.GOLD_VIP_INVITATION_LETTER.get())
                .requires(Tags.Items.INGOTS_GOLD)
                .requires(Tags.Items.INGOTS_GOLD)
                .requires(Items.WRITABLE_BOOK)
                .unlockedBy("has_gold_ingot", has(Tags.Items.INGOTS_GOLD))
                .save(output);
        shapeless(RecipeCategory.MISC, ModItems.DIAMOND_VIP_INVITATION_LETTER.get())
                .requires(Tags.Items.GEMS_DIAMOND)
                .requires(Tags.Items.GEMS_DIAMOND)
                .requires(Items.WRITABLE_BOOK)
                .unlockedBy("has_diamond", has(Tags.Items.GEMS_DIAMOND))
                .save(output);
        shapeless(RecipeCategory.MISC, ModItems.NETHERITE_VIP_INVITATION_LETTER.get())
                .requires(Items.NETHERITE_SCRAP)
                .requires(Items.NETHERITE_SCRAP)
                .requires(Tags.Items.INGOTS_GOLD)
                .requires(Tags.Items.INGOTS_GOLD)
                .requires(Items.WRITABLE_BOOK)
                .unlockedBy("has_gold_ingot", has(Tags.Items.INGOTS_GOLD))
                .unlockedBy("has_netherite_scrap", has(Items.NETHERITE_SCRAP))
                .save(output);
        shapeless(RecipeCategory.MISC, ModItems.NETHERITE_VIP_INVITATION_LETTER.get())
                .requires(Items.NETHERITE_SCRAP)
                .requires(Items.NETHERITE_SCRAP)
                .requires(ModItems.GOLD_VIP_INVITATION_LETTER.get())
                .unlockedBy("has_gold_ingot", has(Tags.Items.INGOTS_GOLD))
                .unlockedBy("has_netherite_scrap", has(Items.NETHERITE_SCRAP))
                .save(output, TJChase.prefix(ModItemNames.NETHERITE_VIP_INVITATION_LETTER + "_from_" + ModItemNames.GOLD_VIP_INVITATION_LETTER).toString());
    }

    @Override
    protected void netheriteSmithing(Item ingredientItem, RecipeCategory category, Item resultItem) {
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.of(ingredientItem),
                        tag(ItemTags.NETHERITE_TOOL_MATERIALS),
                        category,
                        resultItem
                )
                .unlocks("has_netherite_ingot", has(ItemTags.NETHERITE_TOOL_MATERIALS))
                .save(output, findItemName(resultItem) + "_smithing");
    }

    private static String findItemName(ItemLike itemLike) {
        Identifier key = BuiltInRegistries.ITEM.getKey(itemLike.asItem());
        return "minecraft".equals(key.getNamespace()) ? getItemName(itemLike) : getModItemName(itemLike);
    }

    private static String getModItemName(ItemLike itemLike) {
        return TJChase.MODID + ":" + getItemName(itemLike);
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
            return new ModRecipeProvider(provider, output);
        }

        @Override
        public String getName() {
            return "TJChase Recipes";
        }
    }
}
