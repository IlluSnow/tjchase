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
import illusnow.tjchase.block.ModBlocks;
import illusnow.tjchase.command.EntityCommand;
import illusnow.tjchase.command.ProficiencyCommand;
import illusnow.tjchase.entity.HarpTester;
import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.entity.proficiency.ProficiencyMainLevel;
import illusnow.tjchase.item.ModCreativeModeTabs;
import illusnow.tjchase.item.ModItems;
import illusnow.tjchase.item.EntityDebugStickItem;
import illusnow.tjchase.item.ProficiencyStickItem;
import illusnow.tjchase.item.enchantment.ModEnchantments;
import illusnow.tjchase.sound.ModSoundSubtitles;
import illusnow.tjchase.tag.ModBlockTags;
import illusnow.tjchase.tag.ModItemTags;
import illusnow.tjchase.util.ModComponents;
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
            add(ModComponents.SPACED_LEFT_PARENTHESIS_MSG, " (");
            add(ModComponents.SPACED_RIGHT_PARENTHESIS_MSG, ") ");
            add(ProficiencyMainLevel.BEGINNER.getKey(0), "Beginner");
            add(ProficiencyMainLevel.BEGINNER.getKey(5), "Beginner V");
            add(ProficiencyMainLevel.BEGINNER.getKey(4), "Beginner IV");
            add(ProficiencyMainLevel.BEGINNER.getKey(3), "Beginner III");
            add(ProficiencyMainLevel.BEGINNER.getKey(2), "Beginner II");
            add(ProficiencyMainLevel.BEGINNER.getKey(1), "Beginner I");
            add(ProficiencyMainLevel.APPRENTICE.getKey(0), "Apprentice");
            add(ProficiencyMainLevel.APPRENTICE.getKey(5), "Apprentice V");
            add(ProficiencyMainLevel.APPRENTICE.getKey(4), "Apprentice IV");
            add(ProficiencyMainLevel.APPRENTICE.getKey(3), "Apprentice III");
            add(ProficiencyMainLevel.APPRENTICE.getKey(2), "Apprentice II");
            add(ProficiencyMainLevel.APPRENTICE.getKey(1), "Apprentice I");
            add(ProficiencyMainLevel.ELITE.getKey(0), "Elite");
            add(ProficiencyMainLevel.ELITE.getKey(5), "Elite V");
            add(ProficiencyMainLevel.ELITE.getKey(4), "Elite IV");
            add(ProficiencyMainLevel.ELITE.getKey(3), "Elite III");
            add(ProficiencyMainLevel.ELITE.getKey(2), "Elite II");
            add(ProficiencyMainLevel.ELITE.getKey(1), "Elite I");
            add(ProficiencyMainLevel.EXPERT.getKey(0), "Expert");
            add(ProficiencyMainLevel.EXPERT.getKey(5), "Expert V");
            add(ProficiencyMainLevel.EXPERT.getKey(4), "Expert IV");
            add(ProficiencyMainLevel.EXPERT.getKey(3), "Expert III");
            add(ProficiencyMainLevel.EXPERT.getKey(2), "Expert II");
            add(ProficiencyMainLevel.EXPERT.getKey(1), "Expert I");
            add(ProficiencyMainLevel.MASTER.getKey(0), "Master");
            add(ProficiencyMainLevel.MASTER.getKey(4), "Master IV");
            add(ProficiencyMainLevel.MASTER.getKey(3), "Master III");
            add(ProficiencyMainLevel.MASTER.getKey(2), "Master II");
            add(ProficiencyMainLevel.MASTER.getKey(1), "Master I");
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
            add(ProficiencyCommand.INVALID_REMAINING_MSG, "Invalid remaining proficiency points %d for %s");
            add(ProficiencyCommand.INVALID_ITEM_MSG, "%s is not a Proficiency Stick");
            add(ProficiencyCommand.SUCCESS_MSG, "Successfully set the proficiency to %s");
            add(EntityCommand.FAILURE_MSG, "No valid mob was found");
            add(EntityCommand.SUCCESS_SINGLE_MSG, "Successfully resumed %s's AI");
            add(EntityCommand.SUCCESS_MULTIPLE_MSG, "Successfully resumed %d mobs' AI");
            add(getEnchantmentDescriptionId(ModEnchantments.FORCEFUL), "Forceful");
            add(getEnchantmentDescriptionId(ModEnchantments.EXPLOSIVE), "Explosive");
            add(getEnchantmentDescriptionId(ModEnchantments.OVERLOAD), "Overload");
            add(getEnchantmentDescriptionId(ModEnchantments.LIGHTWEIGHT), "Lightweight");
            add(getEnchantmentDescriptionId(ModEnchantments.ANTIGRAVITY), "Antigravity");
            add(getEnchantmentDescriptionId(ModEnchantments.SEEKING), "Seeking");
            add(getEnchantmentDescriptionId(ModEnchantments.ANGEL_TOM_PASSIVE_2), "Leeching");
            add(getEnchantmentDescriptionId(ModEnchantments.ANGEL_TOM_WEAPON_2), "Auto Creation");
            add(getEnchantmentDescriptionId(ModEnchantments.ANGEL_TOM_WEAPON_3), "Immortal");
            add(getDamageTypeMsg(ModDamageSources.MOB_ATTACK_NO_SCALING), "%1$s was slain by %2$s");
            add(getDamageTypeMsg(ModDamageSources.MOB_ATTACK_NO_SCALING, "item"), "%1$s was slain by %2$s using %3$s");
            add(getDamageTypeMsg(ModDamageSources.ORBITING_BLOCK), "%1$s was squashed by a flying block");
            add(getDamageTypeMsg(ModDamageSources.INDIRECT_ORBITING_BLOCK), "%1$s was squashed by a block thrown by %2$s");
            add(getDamageTypeMsg(ModDamageSources.YOGA_BALL), "%1$s was squashed by a inflating yoga ball thrown by %2$s");
            add(HarpTester.NAME, "Monster Rush");
            add(HarpTester.NAME_REMAINING, "Monster Rush - %d remaining");
            add(ModEntities.BLUEPRINT.get(), "Line's Blueprint");
            add(ModEntities.BLUEPRINT_MANAGER.get(), "Blueprint Manager");
            add(ModEntities.EVILINIA.get(), "Evilinia");
            add(ModEntities.HARP_TESTER.get(), "Harp Tester");
            add(ModEntities.LINIA.get(), "Linia");
            add(ModEntities.VINE_MANAGER.get(), "Vine Manager");
            add(ModEntities.VINE_SEED.get(), "Robin Hood Tuffy's Bean");
            add(ModEntities.ORBITING_BLOCK.get(), "Flying Block");
            add(ModEntities.YOGA_BALL.get(), "Yoga Ball");
            add(ModEntities.ZURI.get(), "Zuri");
            add(ModCreativeModeTabs.TAB_ID, "TJChase");
            add(ModItems.BLUEPRINT.get(), "Line's Blueprint");
            add(ModItems.ENTITY_DEBUG_STICK.get(), "Entity Debug Stick");
            add(ModItems.HARP_TESTER.get(), "Harp Tester");
            add(ModItems.HARP.get(), "Angel Tom's Harp");
            add(ModItems.NETHERITE_HARP.get(), "Angel Tom's Netherite Harp");
            add(ModItems.PROFICIENCY_STICK.get(), "Proficiency Stick");
            add(ModItems.VINE_SEED.get(), "Robin Hood Tuffy's Bean");
            add(EntityDebugStickItem.KILL, "Killed %s");
            add(EntityDebugStickItem.REMOVE, "Removed %s");
            add(EntityDebugStickItem.RESUME, "Resumed the entity's AI");
            add(EntityDebugStickItem.SET_FACING_NORTH, "Set facing north");
            add(EntityDebugStickItem.SET_FACING_SOUTH, "Set facing south");
            add(EntityDebugStickItem.SET_FACING_WEST, "Set facing west");
            add(EntityDebugStickItem.SET_FACING_EAST, "Set facing east");
            add(ProficiencyStickItem.GET_PROFICIENCY, "%1$s's proficiency is %2$s");
            add(ProficiencyStickItem.SET_PROFICIENCY, "Set %1$s's proficiency to %2$s");
            add(ProficiencyStickItem.INVALID_ENTITY, "Invalid entity: %1$s");
            add(ProficiencyStickItem.PROFICIENCY_NOT_SET, "%1$s's proficiency is not set");
            add(ModBlockTags.SPEED_BOOSTING_VINE, "Speed Boosting Vine");
            add(ModBlockTags.TEMPORARY_BLOCKS_OF_VINES, "Temporary Blocks Of Blocks");
            add(ModBlockTags.EXPLOSIVE_ORBITING_BLOCK_TAG_SPECIAL, "Special Explosive Blocks");
            add(ModBlockTags.HEAVY_ORBITING_BLOCK_TAG_SPECIAL, "Special Heavy Blocks");
            add(ModBlockTags.HARD_ORBITING_BLOCK_TAG_SPECIAL, "Special Hard Blocks");
            add(ModBlockTags.SOFT_ORBITING_BLOCK_TAG_SPECIAL, "Special Soft Blocks");
            add(ModItemTags.HARPS, "Harps");
            add(ModSoundSubtitles.BLUEPRINT_FOLD, "Blueprint folds");
            add(ModSoundSubtitles.BLUEPRINT_RELEASE, "Blueprint expands");
            add(ModSoundSubtitles.BLUEPRINT_THROW, "Blueprint thrown");
            add(ModSoundSubtitles.DANCE_TIME, "\"Dance Time\"");
            add(ModSoundSubtitles.EVILINIA_AMBIENT, "Evilinia screams");
            add(ModSoundSubtitles.EVILINIA_DEATH, "Evilinia dies");
            add(ModSoundSubtitles.EVILINIA_HURT, "Evilinia hurts");
            add(ModSoundSubtitles.HARP_ATTRACT_BLOCKS, "Harp attracts blocks");
            add(ModSoundSubtitles.HARP_THROW_BLOCK, "Block thrown");
            add(ModSoundSubtitles.LINIA_AMBIENT, "Linia screams");
            add(ModSoundSubtitles.LINIA_DEATH, "Linia dies");
            add(ModSoundSubtitles.LINIA_HURT, "Linia hurts");
            add(ModSoundSubtitles.VINE_GROW, "Beanstalk grows");
            add(ModSoundSubtitles.VINE_SEED_THROW, "Bean flies");
            add(ModSoundSubtitles.VINE_HEAL, "Bean heals");
            add(ModSoundSubtitles.VINE_VANISH, "Bean vanishes");
            add(ModSoundSubtitles.YOGA_BALL_HIT, "Yoga Ball hits");
            add(ModSoundSubtitles.ZURI_AMBIENT, "Zuri mumbles");
            add(ModSoundSubtitles.ZURI_ATTACK, "Zuri attacks");
            add(ModSoundSubtitles.ZURI_HURT, "Zuri hurts");
            add(ModSoundSubtitles.ZURI_THROW_YOGA_BALL, "Zuri throws Yoga Ball");
            add(ModSoundSubtitles.ZURI_WEAK, "Zuri becomes weak");
        }
    }

    public static class ZhCn extends LanguageProvider {
        public ZhCn(PackOutput output) {
            super(output, TJChase.MODID, "zh_cn");
        }

        @Override
        protected void addTranslations() {
            add(ModComponents.SPACED_LEFT_PARENTHESIS_MSG, "（");
            add(ModComponents.SPACED_RIGHT_PARENTHESIS_MSG, "）");
            add(ProficiencyMainLevel.BEGINNER.getKey(0), "萌新");
            add(ProficiencyMainLevel.BEGINNER.getKey(5), "萌新 V");
            add(ProficiencyMainLevel.BEGINNER.getKey(4), "萌新 IV");
            add(ProficiencyMainLevel.BEGINNER.getKey(3), "萌新 III");
            add(ProficiencyMainLevel.BEGINNER.getKey(2), "萌新 II");
            add(ProficiencyMainLevel.BEGINNER.getKey(1), "萌新 I");
            add(ProficiencyMainLevel.APPRENTICE.getKey(0), "新锐");
            add(ProficiencyMainLevel.APPRENTICE.getKey(5), "新锐 V");
            add(ProficiencyMainLevel.APPRENTICE.getKey(4), "新锐 IV");
            add(ProficiencyMainLevel.APPRENTICE.getKey(3), "新锐 III");
            add(ProficiencyMainLevel.APPRENTICE.getKey(2), "新锐 II");
            add(ProficiencyMainLevel.APPRENTICE.getKey(1), "新锐 I");
            add(ProficiencyMainLevel.ELITE.getKey(0), "精英");
            add(ProficiencyMainLevel.ELITE.getKey(5), "精英 V");
            add(ProficiencyMainLevel.ELITE.getKey(4), "精英 IV");
            add(ProficiencyMainLevel.ELITE.getKey(3), "精英 III");
            add(ProficiencyMainLevel.ELITE.getKey(2), "精英 II");
            add(ProficiencyMainLevel.ELITE.getKey(1), "精英 I");
            add(ProficiencyMainLevel.EXPERT.getKey(0), "专家");
            add(ProficiencyMainLevel.EXPERT.getKey(5), "专家 V");
            add(ProficiencyMainLevel.EXPERT.getKey(4), "专家 IV");
            add(ProficiencyMainLevel.EXPERT.getKey(3), "专家 III");
            add(ProficiencyMainLevel.EXPERT.getKey(2), "专家 II");
            add(ProficiencyMainLevel.EXPERT.getKey(1), "专家 I");
            add(ProficiencyMainLevel.MASTER.getKey(0), "宗师");
            add(ProficiencyMainLevel.MASTER.getKey(4), "宗师 IV");
            add(ProficiencyMainLevel.MASTER.getKey(3), "宗师 III");
            add(ProficiencyMainLevel.MASTER.getKey(2), "宗师 II");
            add(ProficiencyMainLevel.MASTER.getKey(1), "宗师 I");
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
            add(ProficiencyCommand.INVALID_REMAINING_MSG, "无效的剩余专精度：%s（对于%s）");
            add(ProficiencyCommand.INVALID_ITEM_MSG, "%s不是专精度设置棒");
            add(ProficiencyCommand.SUCCESS_MSG, "成功将专精度设为了%s");
            add(EntityCommand.FAILURE_MSG, "未找到合法的生物");
            add(EntityCommand.SUCCESS_SINGLE_MSG, "恢复了%s的AI");
            add(EntityCommand.SUCCESS_MULTIPLE_MSG, "恢复了%d个生物的AI");
            add(getDamageTypeMsg(ModDamageSources.MOB_ATTACK_NO_SCALING), "%1$s被%2$s杀死了");
            add(getDamageTypeMsg(ModDamageSources.MOB_ATTACK_NO_SCALING, "item"), "%1$s被%2$s用%3$s杀死了");
            add(getDamageTypeMsg(ModDamageSources.ORBITING_BLOCK), "%1$s被飞行的方块砸扁了");
            add(getDamageTypeMsg(ModDamageSources.INDIRECT_ORBITING_BLOCK), "%1$s被%2$s扔出的方块砸扁了");
            add(getDamageTypeMsg(ModDamageSources.YOGA_BALL), "%1$s在%2$s扔出的瑜伽球膨胀时被压扁了");
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
            add(ModEntities.BLUEPRINT.get(), "莱恩的蓝图");
            add(ModEntities.BLUEPRINT_MANAGER.get(), "蓝图管理器");
            add(ModEntities.EVILINIA.get(), "邪恶线灵");
            add(ModEntities.HARP_TESTER.get(), "竖琴测试器");
            add(ModEntities.LINIA.get(), "线灵");
            add(ModEntities.VINE_MANAGER.get(), "藤蔓管理器");
            add(ModEntities.VINE_SEED.get(), "罗宾汉泰菲的藤蔓种子");
            add(ModEntities.ORBITING_BLOCK.get(), "飞行的方块");
            add(ModEntities.YOGA_BALL.get(), "瑜伽球");
            add(ModEntities.ZURI.get(), "苏蕊");
            add(ModCreativeModeTabs.TAB_ID, "猫鼠");
            add(ModItems.BLUEPRINT.get(), "莱恩的蓝图");
            add(ModItems.ENTITY_DEBUG_STICK.get(), "实体调试棒");
            add(ModItems.HARP_TESTER.get(), "竖琴测试棒");
            add(ModItems.HARP.get(), "天使汤姆的竖琴");
            add(ModItems.NETHERITE_HARP.get(), "天使汤姆的下界合金竖琴");
            add(ModItems.PROFICIENCY_STICK.get(), "专精度设置棒");
            add(ModItems.VINE_SEED.get(), "罗宾汉泰菲的藤蔓种子");
            add(EntityDebugStickItem.KILL, "杀死了%s");
            add(EntityDebugStickItem.REMOVE, "移除了%s");
            add(EntityDebugStickItem.RESUME, "恢复了此实体的AI");
            add(EntityDebugStickItem.SET_FACING_NORTH, "设为面向北方");
            add(EntityDebugStickItem.SET_FACING_SOUTH, "设为面向南方");
            add(EntityDebugStickItem.SET_FACING_WEST, "设为面向西方");
            add(EntityDebugStickItem.SET_FACING_EAST, "设为面向东方");
            add(ProficiencyStickItem.GET_PROFICIENCY, "%1$s的专精度为%2$s");
            add(ProficiencyStickItem.SET_PROFICIENCY, "%1$s的专精度已设为%2$s");
            add(ProficiencyStickItem.INVALID_ENTITY, "无效的实体：%1$s");
            add(ProficiencyStickItem.PROFICIENCY_NOT_SET, "%1$s的专精度尚未设置");
            add(ModBlockTags.SPEED_BOOSTING_VINE, "加速藤蔓");
            add(ModBlockTags.TEMPORARY_BLOCKS_OF_VINES, "藤蔓的临时方块");
            add(ModBlockTags.EXPLOSIVE_ORBITING_BLOCK_TAG_SPECIAL, "特殊的爆炸性方块");
            add(ModBlockTags.HEAVY_ORBITING_BLOCK_TAG_SPECIAL, "特殊的沉重方块");
            add(ModBlockTags.HARD_ORBITING_BLOCK_TAG_SPECIAL, "特殊的坚硬方块");
            add(ModBlockTags.SOFT_ORBITING_BLOCK_TAG_SPECIAL, "特殊的柔软方块");
            add(ModItemTags.HARPS, "竖琴");
            add(ModSoundSubtitles.BLUEPRINT_FOLD, "蓝图：折叠");
            add(ModSoundSubtitles.BLUEPRINT_RELEASE, "蓝图：展开");
            add(ModSoundSubtitles.BLUEPRINT_THROW, "蓝图：飞出");
            add(ModSoundSubtitles.DANCE_TIME, "“律动时间”");
            add(ModSoundSubtitles.EVILINIA_AMBIENT, "邪恶线灵：尖叫");
            add(ModSoundSubtitles.EVILINIA_DEATH, "邪恶线灵：死亡");
            add(ModSoundSubtitles.EVILINIA_HURT, "邪恶线灵：受伤");
            add(ModSoundSubtitles.HARP_ATTRACT_BLOCKS, "竖琴：吸引方块");
            add(ModSoundSubtitles.HARP_THROW_BLOCK, "方块：被扔出");
            add(ModSoundSubtitles.LINIA_AMBIENT, "线灵：尖叫");
            add(ModSoundSubtitles.LINIA_DEATH, "线灵：死亡");
            add(ModSoundSubtitles.LINIA_HURT, "线灵：受伤");
            add(ModSoundSubtitles.VINE_GROW, "藤蔓：生长");
            add(ModSoundSubtitles.VINE_HEAL, "藤蔓：治疗");
            add(ModSoundSubtitles.VINE_SEED_THROW, "藤蔓种子：飞出");
            add(ModSoundSubtitles.VINE_VANISH, "藤蔓：消失");
            add(ModSoundSubtitles.YOGA_BALL_HIT, "瑜伽球：命中");
            add(ModSoundSubtitles.ZURI_AMBIENT, "苏蕊：喃喃自语");
            add(ModSoundSubtitles.ZURI_ATTACK, "苏蕊：攻击");
            add(ModSoundSubtitles.ZURI_HURT, "苏蕊：受伤");
            add(ModSoundSubtitles.ZURI_THROW_YOGA_BALL, "苏蕊：投掷瑜伽球");
            add(ModSoundSubtitles.ZURI_WEAK, "苏蕊：虚弱");
        }
    }
}
