package illusnow.tjchase.data;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.block.ModBlocks;
import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.item.ModCreativeModeTabs;
import illusnow.tjchase.item.ModItems;
import illusnow.tjchase.sound.ModSoundSubtitles;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public final class ModLanguageProvider {
    private ModLanguageProvider() {}

    public static class EnUs extends LanguageProvider {
        public EnUs(PackOutput output) {
            super(output, TJChase.MODID, "en_us");
        }

        @Override
        protected void addTranslations() {
            add(ModBlocks.TEMPORARY_ACACIA_LEAVES.get(), "Temporary Acacia Leaves");
            add(ModBlocks.TEMPORARY_BIRCH_LEAVES.get(), "Temporary Birch Leaves");
            add(ModBlocks.TEMPORARY_DARK_OAK_LEAVES.get(), "Temporary Dark Oak Leaves");
            add(ModBlocks.TEMPORARY_FLOWERING_AZALEA_LEAVES.get(), "Temporary Flowering Azalea Leaves");
            add(ModBlocks.TEMPORARY_JUNGLE_LEAVES.get(), "Temporary Jungle Leaves");
            add(ModBlocks.TEMPORARY_OAK_LEAVES.get(), "Temporary Oak Leaves");
            add(ModBlocks.TEMPORARY_SPRUCE_LEAVES.get(), "Temporary Spruce Leaves");
            add(ModBlocks.TEMPORARY_AZALEA_LEAVES.get(), "Temporary Azalea Leaves");
            add(ModBlocks.TEMPORARY_MOSS_BLOCK.get(), "Temporary Moss Block");
            add(ModBlocks.TEMPORARY_VINE.get(), "Temporary Vine");
            add(ModEntities.VINE_MANAGER.get(), "Vine Manager");
            add(ModEntities.VINE_SEED.get(), "Vine Seed");
            add(ModCreativeModeTabs.RANDOM_CREATIONS_TAB_ID, "TJChase");
            add(ModItems.HARP.get(), "Harp");
            add(ModItems.NETHERITE_HARP.get(), "Netherite Harp");
            add(ModItems.VINE_SEED.get(), "Vine Seed");
            add(ModSoundSubtitles.HARP_ATTRACT_BLOCKS, "Harp attracts blocks");
            add(ModSoundSubtitles.HARP_THROW_BLOCK, "Block thrown");
            add(ModSoundSubtitles.VINE_GROW, "Vine grows");
            add(ModSoundSubtitles.VINE_SEED_THROW, "Vine seed flies");
            add(ModSoundSubtitles.VINE_HEAL, "Vine heals");
            add(ModSoundSubtitles.VINE_VANISH, "Vine vanishes");
        }
    }

    public static class ZhCn extends LanguageProvider {
        public ZhCn(PackOutput output) {
            super(output, TJChase.MODID, "zh_cn");
        }

        @Override
        protected void addTranslations() {
            add(ModBlocks.TEMPORARY_ACACIA_LEAVES.get(), "临时金合欢树叶");
            add(ModBlocks.TEMPORARY_BIRCH_LEAVES.get(), "临时白桦树叶");
            add(ModBlocks.TEMPORARY_DARK_OAK_LEAVES.get(), "临时深色橡树树叶");
            add(ModBlocks.TEMPORARY_FLOWERING_AZALEA_LEAVES.get(), "盛开的临时杜鹃树叶");
            add(ModBlocks.TEMPORARY_JUNGLE_LEAVES.get(), "临时丛林树叶");
            add(ModBlocks.TEMPORARY_OAK_LEAVES.get(), "临时橡树树叶");
            add(ModBlocks.TEMPORARY_SPRUCE_LEAVES.get(), "临时云杉树叶");
            add(ModBlocks.TEMPORARY_AZALEA_LEAVES.get(), "临时杜鹃树叶");
            add(ModBlocks.TEMPORARY_MOSS_BLOCK.get(), "临时苔藓块");
            add(ModBlocks.TEMPORARY_VINE.get(), "临时藤蔓");
            add(ModEntities.VINE_MANAGER.get(), "藤蔓管理器");
            add(ModEntities.VINE_SEED.get(), "藤蔓种子");
            add(ModCreativeModeTabs.RANDOM_CREATIONS_TAB_ID, "猫鼠");
            add(ModItems.HARP.get(), "竖琴");
            add(ModItems.NETHERITE_HARP.get(), "下界合金竖琴");
            add(ModItems.VINE_SEED.get(), "藤蔓种子");
            add(ModSoundSubtitles.HARP_ATTRACT_BLOCKS, "竖琴：吸引方块");
            add(ModSoundSubtitles.HARP_THROW_BLOCK, "方块：被扔出");
            add(ModSoundSubtitles.VINE_GROW, "藤蔓：生长");
            add(ModSoundSubtitles.VINE_HEAL, "藤蔓：治疗");
            add(ModSoundSubtitles.VINE_SEED_THROW, "藤蔓种子：飞出");
            add(ModSoundSubtitles.VINE_VANISH, "藤蔓：消失");
        }
    }
}
