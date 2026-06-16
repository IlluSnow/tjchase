package illusnow.tjchase.data;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.block.ModBlocks;
import illusnow.tjchase.entity.HarpTester;
import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.item.ModCreativeModeTabs;
import illusnow.tjchase.item.ModItems;
import illusnow.tjchase.item.enchantment.EntityDebugStickItem;
import illusnow.tjchase.item.enchantment.ModEnchantments;
import illusnow.tjchase.sound.ModSoundSubtitles;
import illusnow.tjchase.tag.ModBlockTags;
import illusnow.tjchase.tag.ModItemTags;
import illusnow.tjchase.world.ModDamageSources;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import static illusnow.tjchase.client.data.ModDataGenerators.getDamageTypeMsg;
import static illusnow.tjchase.item.enchantment.ModEnchantments.getEnchantmentDescriptionId;

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
            add(getEnchantmentDescriptionId(ModEnchantments.FORCEFUL), "Forceful");
            add(getEnchantmentDescriptionId(ModEnchantments.EXPLOSIVE), "Explosive");
            add(getEnchantmentDescriptionId(ModEnchantments.OVERLOAD), "Overload");
            add(getEnchantmentDescriptionId(ModEnchantments.LIGHTWEIGHT), "Lightweight");
            add(getEnchantmentDescriptionId(ModEnchantments.ANTIGRAVITY), "Antigravity");
            add(getEnchantmentDescriptionId(ModEnchantments.SEEKING), "Seeking");
            add(getEnchantmentDescriptionId(ModEnchantments.ANGEL_TOM_PASSIVE_2), "Leeching");
            add(getEnchantmentDescriptionId(ModEnchantments.ANGEL_TOM_WEAPON_2), "Auto Creation");
            add(getEnchantmentDescriptionId(ModEnchantments.ANGEL_TOM_WEAPON_3), "Immortal");
            add(getDamageTypeMsg(ModDamageSources.ORBITING_BLOCK, false), "%1$s was squashed by a flying block");
            add(getDamageTypeMsg(ModDamageSources.INDIRECT_ORBITING_BLOCK, false), "%1$s was squashed by a block thrown by %2$s");
            add(HarpTester.NAME, "Monster Rush");
            add(HarpTester.NAME_REMAINING, "Monster Rush - %d remaining");
            add(ModEntities.HARP_TESTER.get(), "Harp Tester");
            add(ModEntities.VINE_MANAGER.get(), "Vine Manager");
            add(ModEntities.VINE_SEED.get(), "Vine Seed");
            add(ModEntities.ORBITING_BLOCK.get(), "Flying Block");
            add(ModCreativeModeTabs.RANDOM_CREATIONS_TAB_ID, "TJChase");
            add(ModItems.ENTITY_DEBUG_STICK.get(), "Entity Debug Stick");
            add(EntityDebugStickItem.KILL, "Killed %s");
            add(EntityDebugStickItem.REMOVE, "Removed %s");
            add(EntityDebugStickItem.RESUME, "Resumed the entity's AI");
            add(EntityDebugStickItem.SET_FACING_NORTH, "Set facing north");
            add(EntityDebugStickItem.SET_FACING_SOUTH, "Set facing south");
            add(EntityDebugStickItem.SET_FACING_WEST, "Set facing west");
            add(EntityDebugStickItem.SET_FACING_EAST, "Set facing east");
            add(ModItems.HARP_TESTER.get(), "Harp Tester");
            add(ModItems.HARP.get(), "Harp");
            add(ModItems.NETHERITE_HARP.get(), "Netherite Harp");
            add(ModItems.VINE_SEED.get(), "Vine Seed");
            add(ModBlockTags.SPEED_BOOSTING_VINE, "Speed Boosting Vine");
            add(ModBlockTags.TEMPORARY_BLOCKS_OF_VINES, "Temporary Blocks Of Blocks");
            add(ModBlockTags.EXPLOSIVE_ORBITING_BLOCK_TAG_SPECIAL, "Special Explosive Blocks");
            add(ModBlockTags.HEAVY_ORBITING_BLOCK_TAG_SPECIAL, "Special Heavy Blocks");
            add(ModBlockTags.HARD_ORBITING_BLOCK_TAG_SPECIAL, "Special Hard Blocks");
            add(ModBlockTags.SOFT_ORBITING_BLOCK_TAG_SPECIAL, "Special Soft Blocks");
            add(ModItemTags.HARPS, "Harps");
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
            add(getDamageTypeMsg(ModDamageSources.ORBITING_BLOCK, false), "%1$s被飞行的方块砸扁了");
            add(getDamageTypeMsg(ModDamageSources.INDIRECT_ORBITING_BLOCK, false), "%1$s被%2$s扔出的方块砸扁了");
            add(getEnchantmentDescriptionId(ModEnchantments.FORCEFUL), "重击");
            add(getEnchantmentDescriptionId(ModEnchantments.EXPLOSIVE), "爆裂");
            add(getEnchantmentDescriptionId(ModEnchantments.OVERLOAD), "过载");
            add(getEnchantmentDescriptionId(ModEnchantments.LIGHTWEIGHT), "轻质");
            add(getEnchantmentDescriptionId(ModEnchantments.ANTIGRAVITY), "反重力");
            add(getEnchantmentDescriptionId(ModEnchantments.SEEKING), "追踪");
            add(getEnchantmentDescriptionId(ModEnchantments.ANGEL_TOM_PASSIVE_2), "汲取");
            add(getEnchantmentDescriptionId(ModEnchantments.ANGEL_TOM_WEAPON_2), "无中生有");
            add(getEnchantmentDescriptionId(ModEnchantments.ANGEL_TOM_WEAPON_3), "不死之身");
            add(HarpTester.NAME, "怪物之潮");
            add(HarpTester.NAME_REMAINING, "怪物之潮 - 剩余：%d");
            add(ModEntities.HARP_TESTER.get(), "竖琴测试器");
            add(ModEntities.VINE_MANAGER.get(), "藤蔓管理器");
            add(ModEntities.VINE_SEED.get(), "藤蔓种子");
            add(ModEntities.ORBITING_BLOCK.get(), "飞行的方块");
            add(ModCreativeModeTabs.RANDOM_CREATIONS_TAB_ID, "猫鼠");
            add(ModItems.ENTITY_DEBUG_STICK.get(), "实体调试棒");
            add(EntityDebugStickItem.KILL, "杀死了%s");
            add(EntityDebugStickItem.REMOVE, "移除了%s");
            add(EntityDebugStickItem.RESUME, "恢复了此实体的AI");
            add(EntityDebugStickItem.SET_FACING_NORTH, "设为面向北方");
            add(EntityDebugStickItem.SET_FACING_SOUTH, "设为面向南方");
            add(EntityDebugStickItem.SET_FACING_WEST, "设为面向西方");
            add(EntityDebugStickItem.SET_FACING_EAST, "设为面向东方");
            add(ModItems.HARP_TESTER.get(), "竖琴测试棒");
            add(ModItems.HARP.get(), "竖琴");
            add(ModItems.NETHERITE_HARP.get(), "下界合金竖琴");
            add(ModItems.VINE_SEED.get(), "藤蔓种子");
            add(ModBlockTags.SPEED_BOOSTING_VINE, "加速藤蔓");
            add(ModBlockTags.TEMPORARY_BLOCKS_OF_VINES, "藤蔓的临时方块");
            add(ModBlockTags.EXPLOSIVE_ORBITING_BLOCK_TAG_SPECIAL, "特殊的爆炸性方块");
            add(ModBlockTags.HEAVY_ORBITING_BLOCK_TAG_SPECIAL, "特殊的沉重方块");
            add(ModBlockTags.HARD_ORBITING_BLOCK_TAG_SPECIAL, "特殊的坚硬方块");
            add(ModBlockTags.SOFT_ORBITING_BLOCK_TAG_SPECIAL, "特殊的柔软方块");
            add(ModItemTags.HARPS, "竖琴");
            add(ModSoundSubtitles.HARP_ATTRACT_BLOCKS, "竖琴：吸引方块");
            add(ModSoundSubtitles.HARP_THROW_BLOCK, "方块：被扔出");
            add(ModSoundSubtitles.VINE_GROW, "藤蔓：生长");
            add(ModSoundSubtitles.VINE_HEAL, "藤蔓：治疗");
            add(ModSoundSubtitles.VINE_SEED_THROW, "藤蔓种子：飞出");
            add(ModSoundSubtitles.VINE_VANISH, "藤蔓：消失");
        }
    }
}
