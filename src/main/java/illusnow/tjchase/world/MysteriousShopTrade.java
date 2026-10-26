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

package illusnow.tjchase.world;

import com.google.common.collect.ImmutableList;
import illusnow.tjchase.TJChase;
import illusnow.tjchase.item.ModItems;
import illusnow.tjchase.util.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record MysteriousShopTrade(Identifier id, Factory result, @Nullable Component customName, int weight, boolean permanent) {
    public static final String NAME = "mysterious_shop_trade";
    private static final int DEFAULT_WEIGHT = 12;
    public static final Component HEAD_BUNDLE_CUSTOM_NAME = Component.translatable(TJChase.prefix(NAME, "head_bundle_custom_name"));
    public static final Component TRIM_SMITHING_TEMPLATE_BUNDLE_CUSTOM_NAME = Component.translatable(TJChase.prefix(NAME, "trim_smithing_template_bundle_custom_name"));
    public static final Component MUSIC_DISC_BOX_CUSTOM_NAME = Component.translatable(TJChase.prefix(NAME, "music_disc_box_custom_name"));
    public static final Component DIAMONDS_CUSTOM_NAME = Component.translatable(TJChase.prefix(NAME, "diamonds_custom_name"));
    public static final Component EMERALDS_CUSTOM_NAME = Component.translatable(TJChase.prefix(NAME, "emeralds_custom_name"));
    public static final Component STAMPS_CUSTOM_NAME = Component.translatable(TJChase.prefix(NAME, "stamps_custom_name"));
    public static final Component INFINIBOW_CUSTOM_NAME = Component.translatable(TJChase.prefix(NAME, "infinibow_custom_name"));
    public static final Component ANTI_HARASSMENT_STICK_CUSTOM_NAME = Component.translatable(TJChase.prefix(NAME, "anti_harassment_stick_custom_name"));
    public static final Component OVER_PROTECTION_ARMOR_CUSTOM_NAME = Component.translatable(TJChase.prefix(NAME, "over_protection_armor_custom_name"));
    public static final Component MENDING_CUSTOM_NAME = Component.translatable(TJChase.prefix(NAME, "mending"));
    public static final Component MYSTERIOUS_SHOP = Component.translatable(TJChase.prefixGuiMsg("mysterious_shop"));

    public static final DeferredRegister<MysteriousShopTrade> MYSTERIOUS_TRADES = DeferredRegister.create(ModRegistries.MYSTERIOUS_SHOP_TRADES, TJChase.MODID);
    public static final DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> NETHER_STAR = permanent("nether_star", new ItemStack(Items.NETHER_STAR));
    public static final DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> CONDUIT = refreshable("conduit", new ItemStack(Items.CONDUIT));
    public static final DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> NETHERITE_BLOCK = refreshable("netherite_block", new ItemStack(Items.NETHERITE_BLOCK));
    public static final DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> DIAMONDS = refreshable("diamonds", DIAMONDS_CUSTOM_NAME, new ItemStack(Items.DIAMOND, 64));
    public static final DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> EMERALDS = refreshable("emeralds", EMERALDS_CUSTOM_NAME, new ItemStack(Items.EMERALD, 64));
    public static final DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> STAMPS = refreshable("stamps", STAMPS_CUSTOM_NAME, DEFAULT_WEIGHT / 2, (level, seed) -> new ItemStack(ModItems.STAMP, 64));
    public static final DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> HEAD_BUNDLE = refreshable("head_bundle", HEAD_BUNDLE_CUSTOM_NAME, createHeadBundle());
    public static final DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> TRIM_SMITHING_TEMPLATE_BUNDLE = refreshable("trim_smithing_template_bundle", TRIM_SMITHING_TEMPLATE_BUNDLE_CUSTOM_NAME, createTrimSmithingTemplateBundle());
    public static final DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> MUSIC_DISC_BOX = refreshable("music_disc_box", MUSIC_DISC_BOX_CUSTOM_NAME, createMusicDiscBox());
    public static final DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> DRAGON_EGG = refreshable("dragon_egg", null, 1, (level, seed) -> new ItemStack(Items.DRAGON_EGG));
    public static final DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> ELYTRA = refreshable("elytra", null, DEFAULT_WEIGHT * 2 / 3, (level, seed) -> enchanted(level, seed, new ItemStack(Items.ELYTRA), List.of(new EnchantmentInstance(getEnchantment(level, Enchantments.MENDING), 1)), 0));
    public static final DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> INFINIBOW = refreshable("infinibow", INFINIBOW_CUSTOM_NAME, DEFAULT_WEIGHT * 2 / 3, MysteriousShopTrade::infinibow);
    public static final DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> ANTI_HARASSMENT_STICK = refreshable("anti_harassment_stick", ANTI_HARASSMENT_STICK_CUSTOM_NAME, DEFAULT_WEIGHT * 2 / 3, MysteriousShopTrade::knockbackStick);
    public static final DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> OVER_PROTECTION_HELMET = refreshable("over_protection_helmet", OVER_PROTECTION_ARMOR_CUSTOM_NAME, DEFAULT_WEIGHT / 2, (level, seed) -> overProtection(Items.DIAMOND_HELMET, level, seed));
    public static final DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> OVER_PROTECTION_CHESTPLATE = refreshable("over_protection_chestplate", OVER_PROTECTION_ARMOR_CUSTOM_NAME, DEFAULT_WEIGHT / 2, (level, seed) -> overProtection(Items.DIAMOND_CHESTPLATE, level, seed));
    public static final DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> OVER_PROTECTION_LEGGINGS = refreshable("over_protection_leggings", OVER_PROTECTION_ARMOR_CUSTOM_NAME, DEFAULT_WEIGHT / 2, (level, seed) -> overProtection(Items.DIAMOND_LEGGINGS, level, seed));
    public static final DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> OVER_PROTECTION_BOOTS = refreshable("over_protection_boots", OVER_PROTECTION_ARMOR_CUSTOM_NAME, DEFAULT_WEIGHT / 2, (level, seed) -> overProtection(Items.DIAMOND_BOOTS, level, seed));
    public static final DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> MENDING = refreshable("mending", MENDING_CUSTOM_NAME, DEFAULT_WEIGHT, MysteriousShopTrade::mending);

    private static List<ItemStack> createItemStackList(Item... items) {
        return Arrays.stream(items).map(Item::getDefaultInstance).toList();
    }

    private static ItemStack createHeadBundle() {
        ItemStack bundle = new ItemStack(Items.LIGHT_BLUE_BUNDLE);
        ItemStack illusnow = new ItemStack(Items.PLAYER_HEAD);
        illusnow.set(DataComponents.PROFILE, ResolvableProfile.createUnresolved(TJChase.AUTHOR));
        bundle.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(ImmutableList.<ItemStack>builder().add(illusnow).addAll(createItemStackList(
                Items.ZOMBIE_HEAD,
                Items.CREEPER_HEAD,
                Items.SKELETON_SKULL,
                Items.WITHER_SKELETON_SKULL,
                Items.PIGLIN_HEAD,
                Items.DRAGON_HEAD
        )).build()));
        bundle.set(DataComponents.CUSTOM_NAME, HEAD_BUNDLE_CUSTOM_NAME);
        bundle.set(DataComponents.RARITY, Rarity.RARE);
        return bundle;
    }

    private static ItemStack createTrimSmithingTemplateBundle() {
        ItemStack bundle = new ItemStack(Items.PURPLE_BUNDLE);
        bundle.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(createItemStackList(
                Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE,
                Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE
        )));
        bundle.set(DataComponents.CUSTOM_NAME, TRIM_SMITHING_TEMPLATE_BUNDLE_CUSTOM_NAME);
        bundle.set(DataComponents.RARITY, Rarity.RARE);
        return bundle;
    }

    private static ItemStack createMusicDiscBox() {
        ItemStack box = new ItemStack(Items.MAGENTA_SHULKER_BOX);
        Item[] vanillaMusicDiscs = new Item[] {
                Items.MUSIC_DISC_5,
                Items.MUSIC_DISC_11,
                Items.MUSIC_DISC_13,
                Items.MUSIC_DISC_BLOCKS,
                Items.MUSIC_DISC_CAT,
                Items.MUSIC_DISC_CHIRP,
                Items.MUSIC_DISC_CREATOR,
                Items.MUSIC_DISC_CREATOR_MUSIC_BOX,
                Items.MUSIC_DISC_FAR,
                Items.MUSIC_DISC_LAVA_CHICKEN,
                Items.MUSIC_DISC_MALL,
                Items.MUSIC_DISC_MELLOHI,
                Items.MUSIC_DISC_OTHERSIDE,
                Items.MUSIC_DISC_PIGSTEP,
                Items.MUSIC_DISC_PRECIPICE,
                Items.MUSIC_DISC_RELIC,
                Items.MUSIC_DISC_STAL,
                Items.MUSIC_DISC_STRAD,
                Items.MUSIC_DISC_TEARS,
                Items.MUSIC_DISC_WAIT,
                Items.MUSIC_DISC_WARD
        };
        box.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(createItemStackList(vanillaMusicDiscs)));
        box.set(DataComponents.CUSTOM_NAME, MUSIC_DISC_BOX_CUSTOM_NAME);
        box.set(DataComponents.RARITY, Rarity.RARE);
        return box;
    }

    private static ItemStack infinibow(Level level, long seed) {
        ItemStack bow = new ItemStack(Items.BOW);
        Holder<Enchantment> infinity = getEnchantment(level, Enchantments.INFINITY);
        Holder<Enchantment> mending = getEnchantment(level, Enchantments.MENDING);
        Holder<Enchantment> unbreaking = getEnchantment(level, Enchantments.UNBREAKING);
        bow.set(DataComponents.CUSTOM_NAME, INFINIBOW_CUSTOM_NAME);
        bow.set(DataComponents.RARITY, Rarity.RARE);
        return enchanted(level, seed, bow, List.of(new EnchantmentInstance(infinity, 1), new EnchantmentInstance(mending, 1), new EnchantmentInstance(unbreaking, 3)), 30);
    }

    private static ItemStack knockbackStick(Level level, long seed) {
        ItemStack stick = new ItemStack(Items.STICK);
        Holder<Enchantment> knockback = getEnchantment(level, Enchantments.KNOCKBACK);
        Holder<Enchantment> fireAspect = getEnchantment(level, Enchantments.FIRE_ASPECT);
        stick.set(DataComponents.CUSTOM_NAME, ANTI_HARASSMENT_STICK_CUSTOM_NAME);
        stick.set(DataComponents.RARITY, Rarity.RARE);
        return enchanted(level, seed, stick, List.of(new EnchantmentInstance(knockback, 10), new EnchantmentInstance(fireAspect, 5)), 0);
    }

    private static ItemStack overProtection(Item armorType, Level level, long seed) {
        ItemStack armor = new ItemStack(armorType);
        Holder<Enchantment> protection = getEnchantment(level, Enchantments.PROTECTION);
        Holder<Enchantment> mending = getEnchantment(level, Enchantments.MENDING);
        armor.set(DataComponents.RARITY, Rarity.RARE);
        return enchanted(level, seed, armor, List.of(new EnchantmentInstance(protection, 10), new EnchantmentInstance(mending, 1)), 30);
    }

    private static ItemStack mending(Level level, long seed) {
        ItemStack book = new ItemStack(Items.BOOK);
        Holder<Enchantment> mending = getEnchantment(level, Enchantments.MENDING);
        book = enchanted(level, seed, book, List.of(new EnchantmentInstance(mending, 1)), 0);
        book.setCount(3);
        return book;
    }

    private static Holder.Reference<Enchantment> getEnchantment(Level level, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key).orElseThrow();
    }

    private static ItemStack enchanted(Level level, long seed, ItemStack weapon, List<EnchantmentInstance> mandatoryEnchantments, int levelOfOptionalEnchantments) {
        RandomSource random = RandomSource.create(seed);
        if (levelOfOptionalEnchantments > 0) {
            EnchantmentHelper.enchantItem(random, weapon, levelOfOptionalEnchantments, level.registryAccess(), Optional.of(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(EnchantmentTags.IN_ENCHANTING_TABLE)));
        }
        weapon = weapon.getItem().applyEnchantments(weapon, mandatoryEnchantments);
        return weapon;
    }

    private static DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> permanent(String name, ItemStack result) {
        return permanent(name, null, (level, seed) -> result.copy());
    }

    private static DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> permanent(String name, @Nullable Component customName, Factory result) {
        return MYSTERIOUS_TRADES.register(name, id -> new MysteriousShopTrade(id, result, customName, -1, true));
    }

    private static DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> refreshable(String name, ItemStack result) {
        return refreshable(name, null, result);
    }

    private static DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> refreshable(String name, @Nullable Component customName, ItemStack result) {
        return refreshable(name, customName, DEFAULT_WEIGHT, (level, seed) -> result.copy());
    }

    private static DeferredHolder<MysteriousShopTrade, MysteriousShopTrade> refreshable(String name, @Nullable Component customName, int weight, Factory result) {
        return MYSTERIOUS_TRADES.register(name, id -> new MysteriousShopTrade(id, result, customName, weight, false));
    }

    public ItemStack result(Level level, long seed) {
        return result.createResult(level, seed);
    }

    public String getDescriptionId() {
        return NAME + "_description." + id.getNamespace() + "." + id.getPath();
    }

    public Component createDescription() {
        return Component.translatable(getDescriptionId());
    }

    public static List<MysteriousShopTrade> getRandomTrades(RegistryAccess access, long seed, int count) {
        if (count <= 0) {
            return new ArrayList<>();
        }
        List<MysteriousShopTrade> trades = new ArrayList<>(access.lookupOrThrow(ModRegistries.MYSTERIOUS_SHOP_TRADES_KEY).listElements().map(Holder.Reference::value).toList());
        List<MysteriousShopTrade> result = new ArrayList<>();
        RandomSource random = RandomSource.create(seed);
        if (count > trades.size()) {
            throw new IllegalArgumentException("Count is too large");
        } else if (count == trades.size()) {
            return trades;
        }
        for (int i = 0; i < count; i++) {
            MysteriousShopTrade trade = WeightedRandom.getRandomItem(random, trades, MysteriousShopTrade::weight).orElseThrow(() -> new IllegalStateException("No trades found"));
            result.add(trade);
            trades.remove(trade);
        }
        Collections.shuffle(result, new Random(seed));
        return result;
    }

    @FunctionalInterface
    public interface Factory {
        ItemStack createResult(Level level, long seed);
    }
}
