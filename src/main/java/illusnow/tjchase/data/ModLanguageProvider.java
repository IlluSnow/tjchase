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
import illusnow.tjchase.command.DebugCommand;
import illusnow.tjchase.command.EntityCommand;
import illusnow.tjchase.command.ProficiencyCommand;
import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.entity.dataentity.HarpTester;
import illusnow.tjchase.entity.gameplay.Rocket;
import illusnow.tjchase.entity.proficiency.ProficiencyMainLevel;
import illusnow.tjchase.item.*;
import illusnow.tjchase.item.enchantment.ModEnchantments;
import illusnow.tjchase.sound.ModSoundSubtitles;
import illusnow.tjchase.tag.ModBlockTags;
import illusnow.tjchase.tag.ModEntityTypeTags;
import illusnow.tjchase.tag.ModItemTags;
import illusnow.tjchase.util.ModComponents;
import illusnow.tjchase.util.ModKeyMappingNames;
import illusnow.tjchase.world.ModDamageSources;
import illusnow.tjchase.world.MysteriousShopTrade;
import illusnow.tjchase.world.gameplay.WeakState;
import illusnow.tjchase.world.gameplay.object.editablevalue.EditableValue;
import illusnow.tjchase.world.gameplay.object.editablevalue.EditableValueBehaviors;
import illusnow.tjchase.world.gameplay.object.editablevalue.ModEditableValues;
import illusnow.tjchase.world.inventory.MysteriousShopMenu;
import illusnow.tjchase.world.vip.VIPManager;
import illusnow.tjchase.world.vip.VIPType;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.function.Supplier;

import static illusnow.tjchase.client.data.ModDataGenerators.getDamageTypeMsg;
import static illusnow.tjchase.item.enchantment.ModEnchantments.getEnchantmentDescriptionId;

public abstract class ModLanguageProvider extends LanguageProvider {
    protected ModLanguageProvider(PackOutput output, String locale) {
        super(output, TJChase.MODID, locale);
    }

    public void add(EditableValue<?, ?> key, String name) {
        add(key.getTranslationKey(), name);
    }

    public void addKeyCategory(Identifier categoryId, String name) {
        add(categoryId.toLanguageKey("key.category"), name);
    }

    @Override
    public void addBlock(Supplier<? extends Block> key, String name) {
        super.addBlock(key, name);
        if (key.get().asItem() != Items.AIR) {
            addItem(() -> key.get().asItem(), name);
        }
    }

    public static class EnUs extends ModLanguageProvider {
        public EnUs(PackOutput output) {
            super(output, "en_us");
        }

        @Override
        protected void addTranslations() {
            addBlock(ModBlocks.MAILBOX, "Mailbox");
            add(ModComponents.SPACED_LEFT_PARENTHESIS_MSGID, " (");
            add(ModComponents.SPACED_RIGHT_PARENTHESIS_MSGID, ") ");
            add(ModComponents.CONTAINER_RENAME_MSGID, "Rename");
            add(ModComponents.GUI_RESET_TO_DEFAULT_MSGID, "Reset to Default");
            add(MysteriousShopTrade.MYSTERIOUS_SHOP.getString(), "Mysterious Shop");
            add(MysteriousShopMenu.REFRESH_SECONDS, "Shop refreshes in %d second(s)");
            add(MysteriousShopTrade.ANTI_HARASSMENT_STICK_CUSTOM_NAME.getString(), "Anti-Harassment Stick");
            add(MysteriousShopTrade.DIAMONDS_CUSTOM_NAME.getString(), "Stack of Diamonds");
            add(MysteriousShopTrade.EMERALDS_CUSTOM_NAME.getString(), "Stack of Emeralds");
            add(MysteriousShopTrade.HEAD_BUNDLE_CUSTOM_NAME.getString(), "Head Bundle");
            add(MysteriousShopTrade.INFINIBOW_CUSTOM_NAME.getString(), "Infinibow");
            add(MysteriousShopTrade.OVER_PROTECTION_ARMOR_CUSTOM_NAME.getString(), "Sheltering Armor");
            add(MysteriousShopTrade.MENDING_CUSTOM_NAME.getString(), "Mending Pack");
            add(MysteriousShopTrade.MUSIC_DISC_BOX_CUSTOM_NAME.getString(), "Music Disc Box");
            add(MysteriousShopTrade.STAMPS_CUSTOM_NAME.getString(), "Stack of Stamps");
            add(MysteriousShopTrade.TRIM_SMITHING_TEMPLATE_BUNDLE_CUSTOM_NAME.getString(), "Trim Bundle");
            add(MysteriousShopTrade.ANTI_HARASSMENT_STICK.get().getDescriptionId(), "Use this stick to drive those bad guys away!");
            add(MysteriousShopTrade.CONDUIT.get().getDescriptionId(), "The power of the sea...");
            add(MysteriousShopTrade.DIAMONDS.get().getDescriptionId(), "Suitable for diamond-lovers");
            add(MysteriousShopTrade.DRAGON_EGG.get().getDescriptionId(), "Why do you want to buy this??");
            add(MysteriousShopTrade.ELYTRA.get().getDescriptionId(), "Sky's the limit!!");
            add(MysteriousShopTrade.EMERALDS.get().getDescriptionId(), "Suitable for emerald-lovers");
            add(MysteriousShopTrade.HEAD_BUNDLE.get().getDescriptionId(), "Where did the heads come from?!");
            add(MysteriousShopTrade.INFINIBOW.get().getDescriptionId(), "Never worry about durability!");
            add(MysteriousShopTrade.MENDING.get().getDescriptionId(), "Always in fashion");
            add(MysteriousShopTrade.MUSIC_DISC_BOX.get().getDescriptionId(), "If you love music then the box will be a great choice");
            add(MysteriousShopTrade.NETHER_STAR.get().getDescriptionId(), "Wow, that's a star!");
            add(MysteriousShopTrade.NETHERITE_BLOCK.get().getDescriptionId(), "Do you want to get rid of the dangerous mining work in the nether?");
            add(MysteriousShopTrade.OVER_PROTECTION_BOOTS.get().getDescriptionId(), "Is overprotection a form of indulgence...");
            add(MysteriousShopTrade.OVER_PROTECTION_CHESTPLATE.get().getDescriptionId(), "Is overprotection a form of indulgence...");
            add(MysteriousShopTrade.OVER_PROTECTION_HELMET.get().getDescriptionId(), "Is overprotection a form of indulgence...");
            add(MysteriousShopTrade.OVER_PROTECTION_LEGGINGS.get().getDescriptionId(), "Is overprotection a form of indulgence...");
            add(MysteriousShopTrade.STAMPS.get().getDescriptionId(), "Are you willing to take a gamble?");
            add(MysteriousShopTrade.TRIM_SMITHING_TEMPLATE_BUNDLE.get().getDescriptionId(), "It's time to decorate your armors!");
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
            add(DebugCommand.CLEAR_ACTIONS_MSG_FAIL, "No actions were reset");
            add(DebugCommand.CLEAR_ACTIONS_MSG_SUCCESS_SINGLE, "Reset %s's actions");
            add(DebugCommand.CLEAR_ACTIONS_MSG_SUCCESS_MULTIPLE, "Reset actions of %d players");
            add(DebugCommand.CLEAR_GRAVITY_MSG_FAIL, "No players unaffected by gravity found");
            add(DebugCommand.CLEAR_GRAVITY_MSG_SUCCESS_SINGLE, "Resumed gravity for %s");
            add(DebugCommand.CLEAR_GRAVITY_MSG_SUCCESS_MULTIPLE, "Resumed gravity for %d players");
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
            add(ModEntities.ROCKET.get(), "Rocket");
            add(Rocket.FuseDisplayDirection.FRONT.getTranslatedName().getString(), "Front");
            add(Rocket.FuseDisplayDirection.BACK.getTranslatedName().getString(), "Back");
            add(Rocket.FuseDisplayDirection.LEFT.getTranslatedName().getString(), "Left");
            add(Rocket.FuseDisplayDirection.RIGHT.getTranslatedName().getString(), "Right");
            add(Rocket.FuseDisplayDirection.UP.getTranslatedName().getString(), "Up");
            add(ModEntities.YOGA_BALL.get(), "Yoga Ball");
            add(ModEntities.ZURI.get(), "Zuri");
            add(ModEditableValues.BURNING_SPEED.get(), "Fuse Burning Speed");
            add(ModEditableValues.DEFAULT_FUSE_SECONDS.get(), "Default Fuse Length");
            add(ModEditableValues.INSTABURN_SECONDS.get(), "Fuse Instaburn Length");
            add(ModEditableValues.FUSE_DISPLAY_DIRECTION.get(), "Countdown Display Direction");
            add(ModEditableValues.FUSE_DISPLAY_DISTANCE.get(), "Countdown Display Distance");
            add(ModEditableValues.FUSE_DISPLAY_HEIGHT_OFFSET.get(), "Countdown Display Height");
            add(ModEditableValues.FUSE_DISPLAY_FONT_SCALE.get(), "Countdown Font Size");
            add(EditableValueBehaviors.RangedBehavior.HINT_KEY, "Value Range: [%d, %d]");
            add(EditableValueBehaviors.RangedBehavior.HINT_KEY_DECIMAL, "Value Range: [%.1f, %.1f]");
            add(ModCreativeModeTabs.TAB_ID, "TJChase");
            add(ModCreativeModeTabs.GAMEPLAY_TAB_ID, "TJChase - Gameplay");
            add(WeakState.WEAK_COUNTDOWN_TEXT, "Weak time remaining: %ds");
            add(ModItems.BLUEPRINT.get(), "Line's Blueprint");
            add(ModItems.CAT_EARS.get(), "Cat Ears");
            add(ModItems.DIAMOND_VIP_INVITATION_LETTER.get(), "Diamond Member Invitation Letter");
            add(ModItems.ENTITY_DEBUG_STICK.get(), "Entity Debug Stick");
            add(ModItems.ENVELOPE.get(), "Common Reply Letter");
            add(ModItems.GOLD_VIP_INVITATION_LETTER.get(), "Gold Member Invitation Letter");
            add(ModItems.GOLDEN_FEATHER.get(), "Golden Feather");
            add(ModItems.HARP_TESTER.get(), "Harp Tester");
            add(ModItems.HARP.get(), "Angel Tom's Harp");
            add(ModItems.MOUSE_EARS.get(), "Mouse Ears");
            add(ModItems.MYSTERIOUS_ENVELOPE.get(), "Mysterious Reply Letter");
            add(ModItems.NETHERITE_HARP.get(), "Angel Tom's Netherite Harp");
            add(ModItems.NETHERITE_VIP_INVITATION_LETTER.get(), "Netherite Member Invitation Letter");
            add(ModItems.PROFICIENCY_STICK.get(), "Proficiency Stick");
            add(ModItems.REMOTE_CONTROL.get(), "Remote Control");
            add(ModItems.ROCKET_EDITOR.get(), "Rocket Editor");
            add(ModItems.SENT_ENVELOPE.get(), "Sent Letter");
            add(ModItems.STAMP.get(), "Stamp");
            add(ModItems.VINE_SEED.get(), "Robin Hood Tuffy's Bean");
            add(GameplayObjectEditorItem.NAME_TRANSLATION_KEY, "%s (Carrying %s)");
            add(EntityDebugStickItem.KILL, "Killed %s");
            add(EntityDebugStickItem.REMOVE, "Removed %s");
            add(EntityDebugStickItem.RESUME, "Resumed the entity's AI");
            add(EntityDebugStickItem.SET_FACING_NORTH, "Set facing north");
            add(EntityDebugStickItem.SET_FACING_SOUTH, "Set facing south");
            add(EntityDebugStickItem.SET_FACING_WEST, "Set facing west");
            add(EntityDebugStickItem.SET_FACING_EAST, "Set facing east");
            add(EnvelopeItem.ENVELOPE_TITLE, "Common Reply Letter");
            add(EnvelopeItem.ENVELOPE_GREETING, "Dear %s, \n\nThank you for your letter! The weather here today is very nice, and I'm delighted. I'd like to bring this good mood to you!\nThis is my gift for you. I hope you'll like it.");
            add(EnvelopeItem.ENVELOPE_GREETING_BAD_WEATHER, "Dear %s, \n\nThank you for your letter! I'm so happy today despite the bad weather here. I'd like to bring this good mood to you!\nThis is my gift for you. I hope you'll like it.");
            add(MysteriousEnvelopeItem.MYSTERIOUS_ENVELOPE_TITLE, "Mysterious Reply Letter");
            add(MysteriousEnvelopeItem.MYSTERIOUS_ENVELOPE_GREETING, "Dear %s, \n\nThank you for your letter! I'm in a great mood today because my magic show has once again won the audience's favor. Would you like to know what kind of show it is?\nHere is a feather I used in my performance. Guess what kind of show it is!");
            add(ProficiencyStickItem.GET_PROFICIENCY, "%1$s's proficiency is %2$s");
            add(ProficiencyStickItem.SET_PROFICIENCY, "Set %1$s's proficiency to %2$s");
            add(ProficiencyStickItem.INVALID_ENTITY, "Invalid entity: %1$s");
            add(ProficiencyStickItem.PROFICIENCY_NOT_SET, "%1$s's proficiency is not set");
            add(StampItem.INVALID_MAILBOX, "The mailbox must be placed in an open area when using stamps");
            add(VIPType.GOLD.getDisplayNameId(), "Gold Member");
            add(VIPType.DIAMOND.getDisplayNameId(), "Diamond Member");
            add(VIPType.NETHERITE.getDisplayNameId(), "Netherite Member");
            add(VIPType.VIP_DAYS_INFO, "Deducts days when claiming Daily Rewards");
            add(VIPType.VIP_FREQUENCY_WARNING, "Daily Rewards are not re-claimable within %d minutes");
            add(VIPType.VIP_REPLACEABLE_WARNING, "Replaces lower-level membership types upon usage");
            add(VIPType.VIP_DURATION, "Lasts %d days");
            add(VIPType.VIP_INITIAL_REWARD, "Activation Reward");
            add(VIPType.VIP_DAILY_REWARD, "Daily Reward");
            add(VIPManager.VIP_REPLACED_WARNING, "Warning: Your %s has been replaced by %s");
            add(VIPManager.VIP_ACTIVATED, "Successfully activated %s");
            add(VIPManager.VIP_TOO_MANY_DAYS_REMAINING, "Membership cannot last for more than %d days");
            add(VIPManager.VIP_NOT_ACTIVATED, "Failed to activate %s because your %s is currently active");
            add(VIPManager.VIP_DAYS_REMAINING, "%s has %d day(s) remaining");
            addKeyCategory(ModKeyMappingNames.TJCHASE_MAIN, "TJChase");
            add(ModKeyMappingNames.KEY_STRUGGLE, "Struggle");
            add(ModSoundSubtitles.ARMOR_EQUIP_CAT_EARS, "Cat ears equipped");
            add(ModSoundSubtitles.ARMOR_EQUIP_MOUSE_EARS, "Mouse ears equipped");
            add(ModSoundSubtitles.BLUEPRINT_FOLD, "Blueprint folds");
            add(ModSoundSubtitles.BLUEPRINT_RELEASE, "Blueprint expands");
            add(ModSoundSubtitles.BLUEPRINT_THROW, "Blueprint thrown");
            add(ModSoundSubtitles.DANCE_TIME, "\"Dance Time\"");
            add(ModSoundSubtitles.EVILINIA_AMBIENT, "Evilinia screams");
            add(ModSoundSubtitles.EVILINIA_DEATH, "Evilinia dies");
            add(ModSoundSubtitles.EVILINIA_HURT, "Evilinia hurts");
            add(ModSoundSubtitles.ENVELOPE_OPEN, "Envelope opens");
            add(ModSoundSubtitles.HARP_ATTRACT_BLOCKS, "Harp attracts blocks");
            add(ModSoundSubtitles.HARP_THROW_BLOCK, "Block thrown");
            add(ModSoundSubtitles.INVITATION_LETTER_OPEN, "Invitation Letter opens");
            add(ModSoundSubtitles.LINIA_AMBIENT, "Linia screams");
            add(ModSoundSubtitles.LINIA_DEATH, "Linia dies");
            add(ModSoundSubtitles.LINIA_HURT, "Linia hurts");
            add(ModSoundSubtitles.PRIMED_ROCKET, "Rocket Fuse burns");
            add(ModSoundSubtitles.REMOTE_CONTROL_PRESS, "Remote Control's button pressed");
            add(ModSoundSubtitles.ROCKET_LAUNCH, "Rocket launches");
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
            add(ModBlockTags.EXPLOSIVE_ORBITING_BLOCK_TAG_SPECIAL, "Special Explosive Flying Blocks");
            add(ModBlockTags.HARD_ORBITING_BLOCK_TAG_SPECIAL, "Special Hard Flying Blocks");
            add(ModBlockTags.HEAVY_ORBITING_BLOCK_TAG_SPECIAL, "Special Heavy Flying Blocks");
            add(ModBlockTags.SOFT_ORBITING_BLOCK_TAG_SPECIAL, "Special Soft Flying Blocks");
            add(ModBlockTags.SPEED_BOOSTING_VINE, "Speed Boosting Vine");
            add(ModBlockTags.TEMPORARY_BLOCKS_OF_VINES, "Vine's Temporary Blocks");
            add(ModEntityTypeTags.TJCHASE_FRIENDLY_CATS, "Friendly Cats");
            add(ModEntityTypeTags.TJCHASE_FRIENDLY_MICE, "Friendly Mice");
            add(ModEntityTypeTags.TJCHASE_FRIENDLY_MOBS, "Friendly TJChase Mobs");
            add(ModEntityTypeTags.ZURI_FULLY_CONTROLLABLE, "Zuri Fully Controllable");
            add(ModEntityTypeTags.ZURI_PARTIALLY_CONTROLLABLE, "Zuri Partially Controllable");
            add(ModEntityTypeTags.ZURI_UNCONTROLLABLE, "Zuri Uncontrollable");
            add(ModItemTags.HARPS, "Harps");
        }
    }

    public static class ZhCn extends ModLanguageProvider {
        public ZhCn(PackOutput output) {
            super(output, "zh_cn");
        }

        @Override
        protected void addTranslations() {
            addBlock(ModBlocks.MAILBOX, "邮箱");
            add(ModComponents.SPACED_LEFT_PARENTHESIS_MSGID, "（");
            add(ModComponents.SPACED_RIGHT_PARENTHESIS_MSGID, "）");
            add(ModComponents.CONTAINER_RENAME_MSGID, "重命名");
            add(ModComponents.GUI_RESET_TO_DEFAULT_MSGID, "重置为默认值");
            add(MysteriousShopTrade.MYSTERIOUS_SHOP.getString(), "神秘商店");
            add(MysteriousShopMenu.REFRESH_SECONDS, "商店将于%d秒后刷新");
            add(MysteriousShopTrade.ANTI_HARASSMENT_STICK_CUSTOM_NAME.getString(), "防狼棍");
            add(MysteriousShopTrade.DIAMONDS_CUSTOM_NAME.getString(), "钻石堆");
            add(MysteriousShopTrade.EMERALDS_CUSTOM_NAME.getString(), "绿宝石堆");
            add(MysteriousShopTrade.HEAD_BUNDLE_CUSTOM_NAME.getString(), "头颅袋");
            add(MysteriousShopTrade.INFINIBOW_CUSTOM_NAME.getString(), "无尽之弓");
            add(MysteriousShopTrade.MENDING_CUSTOM_NAME.getString(), "修理包");
            add(MysteriousShopTrade.MUSIC_DISC_BOX_CUSTOM_NAME.getString(), "唱片箱");
            add(MysteriousShopTrade.OVER_PROTECTION_ARMOR_CUSTOM_NAME.getString(), "庇护之甲");
            add(MysteriousShopTrade.STAMPS_CUSTOM_NAME.getString(), "邮票堆");
            add(MysteriousShopTrade.TRIM_SMITHING_TEMPLATE_BUNDLE_CUSTOM_NAME.getString(), "纹饰袋");
            add(MysteriousShopTrade.ANTI_HARASSMENT_STICK.get().getDescriptionId(), "快用这根木棍把不怀好意的家伙们赶走");
            add(MysteriousShopTrade.CONDUIT.get().getDescriptionId(), "海洋的力量……");
            add(MysteriousShopTrade.DIAMONDS.get().getDescriptionId(), "适合热爱钻石的宝宝们");
            add(MysteriousShopTrade.DRAGON_EGG.get().getDescriptionId(), "你为什么想买这个？？");
            add(MysteriousShopTrade.ELYTRA.get().getDescriptionId(), "天空即为极限！！");
            add(MysteriousShopTrade.EMERALDS.get().getDescriptionId(), "适合热爱绿宝石的宝宝们");
            add(MysteriousShopTrade.HEAD_BUNDLE.get().getDescriptionId(), "这些头是从哪来的？！");
            add(MysteriousShopTrade.INFINIBOW.get().getDescriptionId(), "永远不要担心耐用性！");
            add(MysteriousShopTrade.MENDING.get().getDescriptionId(), "永不过时");
            add(MysteriousShopTrade.MUSIC_DISC_BOX.get().getDescriptionId(), "如果你喜欢音乐，那么这个箱子会是一个不错的选择");
            add(MysteriousShopTrade.NETHER_STAR.get().getDescriptionId(), "哇，一颗星星！");
            add(MysteriousShopTrade.NETHERITE_BLOCK.get().getDescriptionId(), "你是否想摆脱危险的下界采矿工作？");
            add(MysteriousShopTrade.OVER_PROTECTION_BOOTS.get().getDescriptionId(), "过度的保护是否是一种纵容呢……");
            add(MysteriousShopTrade.OVER_PROTECTION_CHESTPLATE.get().getDescriptionId(), "过度的保护是否是一种纵容呢……");
            add(MysteriousShopTrade.OVER_PROTECTION_HELMET.get().getDescriptionId(), "过度的保护是否是一种纵容呢……");
            add(MysteriousShopTrade.OVER_PROTECTION_LEGGINGS.get().getDescriptionId(), "过度的保护是否是一种纵容呢……");
            add(MysteriousShopTrade.STAMPS.get().getDescriptionId(), "想不想赌一把呀？");
            add(MysteriousShopTrade.TRIM_SMITHING_TEMPLATE_BUNDLE.get().getDescriptionId(), "是时候装饰你的盔甲啦！");
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
            add(DebugCommand.CLEAR_ACTIONS_MSG_FAIL, "未重置任何玩家的动作");
            add(DebugCommand.CLEAR_ACTIONS_MSG_SUCCESS_SINGLE, "重置了%s的动作");
            add(DebugCommand.CLEAR_ACTIONS_MSG_SUCCESS_MULTIPLE, "重置了%d个玩家的动作");
            add(DebugCommand.CLEAR_GRAVITY_MSG_FAIL, "没有找到不受重力影响的玩家");
            add(DebugCommand.CLEAR_GRAVITY_MSG_SUCCESS_SINGLE, "重置了%s的重力");
            add(DebugCommand.CLEAR_GRAVITY_MSG_SUCCESS_MULTIPLE, "重置了%d个玩家的重力");
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
            add(ModEntities.ROCKET.get(), "火箭");
            add(Rocket.FuseDisplayDirection.FRONT.getTranslatedName().getString(), "前");
            add(Rocket.FuseDisplayDirection.BACK.getTranslatedName().getString(), "后");
            add(Rocket.FuseDisplayDirection.LEFT.getTranslatedName().getString(), "左");
            add(Rocket.FuseDisplayDirection.RIGHT.getTranslatedName().getString(), "右");
            add(Rocket.FuseDisplayDirection.UP.getTranslatedName().getString(), "上");
            add(ModEntities.YOGA_BALL.get(), "瑜伽球");
            add(ModEntities.ZURI.get(), "苏蕊");
            add(ModEditableValues.BURNING_SPEED.get(), "引线燃烧速度");
            add(ModEditableValues.DEFAULT_FUSE_SECONDS.get(), "默认引线长度");
            add(ModEditableValues.INSTABURN_SECONDS.get(), "引线瞬间燃烧长度");
            add(ModEditableValues.FUSE_DISPLAY_DIRECTION.get(), "倒计时显示方向");
            add(ModEditableValues.FUSE_DISPLAY_DISTANCE.get(), "倒计时显示距离");
            add(ModEditableValues.FUSE_DISPLAY_HEIGHT_OFFSET.get(), "倒计时显示高度");
            add(ModEditableValues.FUSE_DISPLAY_FONT_SCALE.get(), "倒计时字体大小");
            add(EditableValueBehaviors.RangedBehavior.HINT_KEY, "取值范围：[%d, %d]");
            add(EditableValueBehaviors.RangedBehavior.HINT_KEY_DECIMAL, "取值范围：[%.1f, %.1f]");
            add(ModCreativeModeTabs.TAB_ID, "猫鼠");
            add(ModCreativeModeTabs.GAMEPLAY_TAB_ID, "猫鼠 - 主游戏");
            add(WeakState.WEAK_COUNTDOWN_TEXT, "虚弱时间剩余：%ds");
            add(ModItems.BLUEPRINT.get(), "莱恩的蓝图");
            add(ModItems.CAT_EARS.get(), "猫猫的耳朵");
            add(ModItems.DIAMOND_VIP_INVITATION_LETTER.get(), "钻石会员邀请函");
            add(ModItems.ENTITY_DEBUG_STICK.get(), "实体调试棒");
            add(ModItems.ENVELOPE.get(), "普通回信");
            add(ModItems.GOLD_VIP_INVITATION_LETTER.get(), "黄金会员邀请函");
            add(ModItems.GOLDEN_FEATHER.get(), "金羽毛");
            add(ModItems.HARP_TESTER.get(), "竖琴测试棒");
            add(ModItems.HARP.get(), "天使汤姆的竖琴");
            add(ModItems.MOUSE_EARS.get(), "鼠鼠的耳朵");
            add(ModItems.MYSTERIOUS_ENVELOPE.get(), "神秘回信");
            add(ModItems.NETHERITE_HARP.get(), "天使汤姆的下界合金竖琴");
            add(ModItems.NETHERITE_VIP_INVITATION_LETTER.get(), "下界合金会员邀请函");
            add(ModItems.PROFICIENCY_STICK.get(), "专精度设置棒");
            add(ModItems.REMOTE_CONTROL.get(), "遥控器");
            add(ModItems.ROCKET_EDITOR.get(), "火箭编辑器");
            add(ModItems.SENT_ENVELOPE.get(), "寄出的信");
            add(ModItems.STAMP.get(), "邮票");
            add(ModItems.VINE_SEED.get(), "罗宾汉泰菲的藤蔓种子");
            add(GameplayObjectEditorItem.NAME_TRANSLATION_KEY, "%s（携带了%s）");
            add(EntityDebugStickItem.KILL, "杀死了%s");
            add(EntityDebugStickItem.REMOVE, "移除了%s");
            add(EntityDebugStickItem.RESUME, "恢复了此实体的AI");
            add(EntityDebugStickItem.SET_FACING_NORTH, "设为面向北方");
            add(EntityDebugStickItem.SET_FACING_SOUTH, "设为面向南方");
            add(EntityDebugStickItem.SET_FACING_WEST, "设为面向西方");
            add(EntityDebugStickItem.SET_FACING_EAST, "设为面向东方");
            add(EnvelopeItem.ENVELOPE_TITLE, "普通回信");
            add(EnvelopeItem.ENVELOPE_GREETING, "亲爱的%s：\n\n感谢你的来信！今天我这里的天气十分晴朗，我的心情也非常愉悦，希望把这份好心情带给你！\n这是我送给你的礼物，希望你能喜欢。");
            add(EnvelopeItem.ENVELOPE_GREETING_BAD_WEATHER, "亲爱的%s：\n\n感谢你的来信！虽然我这儿天气不好，可是我仍然十分愉悦，希望把这份好心情带给你！\n这是我送给你的礼物，希望你能喜欢。");
            add(MysteriousEnvelopeItem.MYSTERIOUS_ENVELOPE_TITLE, "神秘回信");
            add(MysteriousEnvelopeItem.MYSTERIOUS_ENVELOPE_GREETING, "亲爱的%s：\n\n感谢你的来信！我今天的心情非常好，因为我的魔术表演节目又获得了观众的喜爱。你想知道是什么节目么？\n这里有一根我在表演中使用的羽毛，你来猜猜是什么节目吧！");
            add(ProficiencyStickItem.GET_PROFICIENCY, "%1$s的专精度为%2$s");
            add(ProficiencyStickItem.SET_PROFICIENCY, "%1$s的专精度已设为%2$s");
            add(ProficiencyStickItem.INVALID_ENTITY, "无效的实体：%1$s");
            add(ProficiencyStickItem.PROFICIENCY_NOT_SET, "%1$s的专精度尚未设置");
            add(StampItem.INVALID_MAILBOX, "使用邮票时，邮箱必须放在开阔区域");
            add(VIPType.GOLD.getDisplayNameId(), "黄金会员");
            add(VIPType.DIAMOND.getDisplayNameId(), "钻石会员");
            add(VIPType.NETHERITE.getDisplayNameId(), "下界合金会员");
            add(VIPType.VIP_DAYS_INFO, "每次领取每日奖励时扣除天数");
            add(VIPType.VIP_FREQUENCY_WARNING, "%d分钟内无法重新领取每日奖励");
            add(VIPType.VIP_REPLACEABLE_WARNING, "使用时覆盖较低等级的会员类型");
            add(VIPType.VIP_DURATION, "持续时间为%d天");
            add(VIPType.VIP_INITIAL_REWARD, "激活奖励");
            add(VIPType.VIP_DAILY_REWARD, "每日奖励");
            add(VIPManager.VIP_REPLACED_WARNING, "注意：你的%s已被%s替代");
            add(VIPManager.VIP_ACTIVATED, "成功激活了%s");
            add(VIPManager.VIP_TOO_MANY_DAYS_REMAINING, "会员的持续时间不能超过%d天");
            add(VIPManager.VIP_NOT_ACTIVATED, "无法激活%s，你的%s仍处于激活状态");
            add(VIPManager.VIP_DAYS_REMAINING, "%s剩余%d天");
            addKeyCategory(ModKeyMappingNames.TJCHASE_MAIN, "猫鼠");
            add(ModKeyMappingNames.KEY_STRUGGLE, "挣扎");
            add(ModSoundSubtitles.ARMOR_EQUIP_CAT_EARS, "猫耳：穿戴");
            add(ModSoundSubtitles.ARMOR_EQUIP_MOUSE_EARS, "鼠耳：穿戴");
            add(ModSoundSubtitles.BLUEPRINT_FOLD, "蓝图：折叠");
            add(ModSoundSubtitles.BLUEPRINT_RELEASE, "蓝图：展开");
            add(ModSoundSubtitles.BLUEPRINT_THROW, "蓝图：飞出");
            add(ModSoundSubtitles.DANCE_TIME, "“律动时间”");
            add(ModSoundSubtitles.EVILINIA_AMBIENT, "邪恶线灵：尖叫");
            add(ModSoundSubtitles.EVILINIA_DEATH, "邪恶线灵：死亡");
            add(ModSoundSubtitles.EVILINIA_HURT, "邪恶线灵：受伤");
            add(ModSoundSubtitles.ENVELOPE_OPEN, "信封：打开");
            add(ModSoundSubtitles.HARP_ATTRACT_BLOCKS, "竖琴：吸引方块");
            add(ModSoundSubtitles.HARP_THROW_BLOCK, "方块：被扔出");
            add(ModSoundSubtitles.INVITATION_LETTER_OPEN, "邀请函：打开");
            add(ModSoundSubtitles.LINIA_AMBIENT, "线灵：尖叫");
            add(ModSoundSubtitles.LINIA_DEATH, "线灵：死亡");
            add(ModSoundSubtitles.LINIA_HURT, "线灵：受伤");
            add(ModSoundSubtitles.PRIMED_ROCKET, "火箭引线：燃烧");
            add(ModSoundSubtitles.REMOTE_CONTROL_PRESS, "遥控器按钮：按下");
            add(ModSoundSubtitles.ROCKET_LAUNCH, "火箭：发射");
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
            add(ModBlockTags.EXPLOSIVE_ORBITING_BLOCK_TAG_SPECIAL, "特殊的爆炸性的飞行的方块");
            add(ModBlockTags.HARD_ORBITING_BLOCK_TAG_SPECIAL, "特殊的坚硬的飞行的方块");
            add(ModBlockTags.HEAVY_ORBITING_BLOCK_TAG_SPECIAL, "特殊的沉重的飞行的方块");
            add(ModBlockTags.SOFT_ORBITING_BLOCK_TAG_SPECIAL, "特殊的柔软的飞行的方块");
            add(ModBlockTags.SPEED_BOOSTING_VINE, "可加速攀爬的藤蔓");
            add(ModBlockTags.TEMPORARY_BLOCKS_OF_VINES, "藤蔓的临时方块");
            add(ModEntityTypeTags.TJCHASE_FRIENDLY_CATS, "友好的猫");
            add(ModEntityTypeTags.TJCHASE_FRIENDLY_MICE, "友好的老鼠");
            add(ModEntityTypeTags.TJCHASE_FRIENDLY_MOBS, "友好的猫鼠中的生物");
            add(ModEntityTypeTags.ZURI_FULLY_CONTROLLABLE, "苏蕊能完全控制的生物");
            add(ModEntityTypeTags.ZURI_PARTIALLY_CONTROLLABLE, "苏蕊能部分控制的生物");
            add(ModEntityTypeTags.ZURI_UNCONTROLLABLE, "苏蕊不能控制的生物");
            add(ModItemTags.HARPS, "竖琴");
        }
    }
}
