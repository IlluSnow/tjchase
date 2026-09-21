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

package illusnow.tjchase.client.data;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.block.ModBlockNames;
import illusnow.tjchase.block.ModBlocks;
import illusnow.tjchase.client.ModModelTemplates;
import illusnow.tjchase.item.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.Map;
import java.util.stream.Stream;

public class ModModelProvider extends ModelProvider {
    public static final String CUTOUT = Identifier.DEFAULT_NAMESPACE + ":" + "cutout";

    public ModModelProvider(PackOutput output) {
        super(output, TJChase.MODID);
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return BuiltInRegistries.ITEM.listElements()
                .filter(item -> !(item.value() instanceof GeoItem))
                .filter(id -> id.getKey() != null && id.getKey().identifier().getNamespace().equals(modId));
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        // Block Models
        copyBlockModelOnly(blockModels, Blocks.ACACIA_LEAVES, ModBlocks.TEMPORARY_ACACIA_LEAVES.get());
        copyBlockModelOnly(blockModels, Blocks.AZALEA_LEAVES, ModBlocks.TEMPORARY_AZALEA_LEAVES.get());
        copyBlockModelOnly(blockModels, Blocks.BIRCH_LEAVES, ModBlocks.TEMPORARY_BIRCH_LEAVES.get());
        copyBlockModelOnly(blockModels, Blocks.DARK_OAK_LEAVES, ModBlocks.TEMPORARY_DARK_OAK_LEAVES.get());
        copyBlockModelOnly(blockModels, Blocks.FLOWERING_AZALEA_LEAVES, ModBlocks.TEMPORARY_FLOWERING_AZALEA_LEAVES.get());
        copyBlockModelOnly(blockModels, Blocks.JUNGLE_LEAVES, ModBlocks.TEMPORARY_JUNGLE_LEAVES.get());
        copyBlockModelOnly(blockModels, Blocks.MOSS_BLOCK, ModBlocks.TEMPORARY_MOSS_BLOCK.get());
        copyBlockModelOnly(blockModels, Blocks.OAK_LEAVES, ModBlocks.TEMPORARY_OAK_LEAVES.get());
        copyBlockModelOnly(blockModels, Blocks.SPRUCE_LEAVES, ModBlocks.TEMPORARY_SPRUCE_LEAVES.get());

        blockModels.createNonTemplateHorizontalBlock(ModBlocks.TEMPORARY_VINE.get());
        blockModels.modelOutput.accept(prefixBlock(ModBlockNames.TEMPORARY_VINE), () -> ModelTemplates.create(TextureSlot.PARTICLE, TextureSlot.TEXTURE)
                .extend()
                .ambientOcclusion(false)
                .element(builder -> builder.from(0, 0, 15.2F)
                        .to(16, 16, 15.2F)
                        .shade(false)
                        .face(Direction.NORTH, fb -> fb.uvs(0, 0, 16, 16).tintindex(0).texture(TextureSlot.TEXTURE))
                        .face(Direction.SOUTH, fb -> fb.uvs(16, 0, 0, 16).tintindex(0).texture(TextureSlot.TEXTURE)))
                .renderType(CUTOUT)
                .build()
                .createBaseTemplate(
                        ModelLocationUtils.getModelLocation(ModBlocks.TEMPORARY_VINE.get()),
                        Map.of(
                                TextureSlot.PARTICLE, prefixVanillaBlock("vine"),
                                TextureSlot.TEXTURE, prefixVanillaBlock("vine")
                        )
                ));

        // Item Models
        itemModels.generateFlatItem(ModItems.BLUEPRINT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.VINE_SEED.get(), ModelTemplates.FLAT_ITEM);

        createHarp(itemModels, ModItems.HARP.get());
        createHarp(itemModels, ModItems.NETHERITE_HARP.get());
        itemModels.generateFlatItem(ModItems.ENTITY_DEBUG_STICK.get(), Items.STICK, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.HARP_TESTER.get(), Items.STICK, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.PROFICIENCY_STICK.get(), Items.STICK, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.ROCKET_EDITOR.get(), ModelTemplates.FLAT_HANDHELD_ROD_ITEM);
    }

    private static Identifier prefixVanillaBlock(String name) {
        return Identifier.withDefaultNamespace("block/" + name);
    }

    private static Identifier prefixBlock(String name) {
        return TJChase.prefix("block/" + name);
    }

    public static void createHarp(ItemModelGenerators itemModels, Item harp) {
        itemModels.generateFlatItem(harp, ModModelTemplates.HARP);
//        ModModelTemplates.HARP.create(harp, TextureMapping.layer0(harp), itemModels.modelOutput);
    }

    public static void copyBlockModelOnly(BlockModelGenerators blockModelGenerators, Block sourceBlock, Block targetBlock) {
        MultiVariant variant = BlockModelGenerators.plainVariant(ModelLocationUtils.getModelLocation(sourceBlock));
        blockModelGenerators.blockStateOutput.accept(MultiVariantGenerator.dispatch(targetBlock, variant));
    }
}
