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
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Map;

public class ModModelProvider extends ModelProvider {
    public static final String CUTOUT = Identifier.DEFAULT_NAMESPACE + ":" + "cutout";

    public ModModelProvider(PackOutput output) {
        super(output, TJChase.MODID);
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
        itemModels.generateFlatItem(ModItems.VINE_SEED.get(), ModelTemplates.FLAT_ITEM);

        createHarp(itemModels, ModItems.HARP.get());
        createHarp(itemModels, ModItems.NETHERITE_HARP.get());
        itemModels.generateFlatItem(ModItems.ENTITY_DEBUG_STICK.get(), Items.STICK, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.HARP_TESTER.get(), Items.STICK, ModelTemplates.FLAT_HANDHELD_ITEM);
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
