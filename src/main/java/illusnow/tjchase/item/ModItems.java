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

package illusnow.tjchase.item;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.sound.ModSoundEvents;
import illusnow.tjchase.util.HarpConstants;
import illusnow.tjchase.world.gameplay.object.ModGameplayObjectTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(TJChase.MODID);
    public static final DeferredHolder<Item, BlueprintItem> BLUEPRINT = register(ModItemNames.BLUEPRINT, BlueprintItem::new, properties -> properties.stacksTo(16).rarity(ModRarities.BLUEPRINT_CUSTOM.getValue()));
    public static final DeferredHolder<Item, TJChaseEarsItem> CAT_EARS = register(ModItemNames.CAT_EARS, properties -> new TJChaseEarsItem(ModSoundEvents.ARMOR_EQUIP_CAT_EARS, properties),
            properties -> properties.stacksTo(1)
                    .rarity(ModRarities.TOM_CUSTOM.getValue()));
    public static final DeferredHolder<Item, EntityDebugStickItem> ENTITY_DEBUG_STICK = register(ModItemNames.ENTITY_DEBUG_STICK, EntityDebugStickItem::new,
            properties -> properties.stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
    public static final DeferredHolder<Item, HarpItem> HARP = ITEMS.register(ModItemNames.HARP, () ->
            new HarpItem(new Item.Properties().setId(createItemId(ModItemNames.HARP)).rarity(ModRarities.HARP_CUSTOM.getValue()).durability(HarpConstants.DURABILITY).enchantable(HarpConstants.ENCHANTMENT_VALUE).repairable(ItemTags.GOLD_TOOL_MATERIALS),
                    HarpConstants.BASE_ORBITING_BLOCK_COUNT,
                    0));
    public static final DeferredHolder<Item, HarpItem> NETHERITE_HARP = ITEMS.register(ModItemNames.NETHERITE_HARP, () ->
            new HarpItem(new Item.Properties().setId(createItemId(ModItemNames.NETHERITE_HARP)).fireResistant().rarity(ModRarities.NETHERITE_HARP_CUSTOM.getValue()).durability(HarpConstants.DURABILITY_NETHERITE).enchantable(HarpConstants.ENCHANTMENT_VALUE_NETHERITE).repairable(ItemTags.NETHERITE_TOOL_MATERIALS),
                    HarpConstants.BASE_ORBITING_BLOCK_COUNT + HarpConstants.ORBITING_BLOCK_COUNT_NETHERITE_BONUS,
                    HarpConstants.ORBITING_BLOCK_DAMAGE_NETHERITE_BONUS));
    public static final DeferredHolder<Item, HarpTesterItem> HARP_TESTER = register(ModItemNames.HARP_TESTER, HarpTesterItem::new,
            properties -> properties.stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
    public static final DeferredHolder<Item, TJChaseEarsItem> MOUSE_EARS = register(ModItemNames.MOUSE_EARS, properties -> new TJChaseEarsItem(ModSoundEvents.ARMOR_EQUIP_MOUSE_EARS, properties),
            properties -> properties.stacksTo(1)
                    .rarity(ModRarities.JERRY_CUSTOM.getValue()));
    public static final DeferredHolder<Item, ProficiencyStickItem> PROFICIENCY_STICK = register(ModItemNames.PROFICIENCY_STICK, ProficiencyStickItem::new,
            properties -> properties.stacksTo(1)
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
    public static final DeferredHolder<Item, RemoteControlItem> REMOTE_CONTROL = register(ModItemNames.REMOTE_CONTROL, RemoteControlItem::new,
            properties -> properties.stacksTo(1));
    public static final DeferredHolder<Item, Item> ROCKET_EDITOR = register(ModItemNames.ROCKET_EDITOR, properties -> new InGamePlacedEntityEditorItem<>(properties, ModEntities.ROCKET, ModGameplayObjectTypes.ROCKET), properties -> properties.stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredHolder<Item, Item> VINE_SEED = register(ModItemNames.VINE_SEED, VineSeedItem::new, properties -> properties.stacksTo(16).rarity(ModRarities.VINE_SEED_CUSTOM.getValue()));

    private ModItems() {}

    private static DeferredHolder<Item, Item> register(String name, UnaryOperator<Item.Properties> op) {
        return register(name, Item::new, op);
    }

    private static <T extends Item> DeferredHolder<Item, T> register(String name, Function<? super Item.Properties, ? extends T> factory, UnaryOperator<Item.Properties> op) {
        return ITEMS.register(name, () -> factory.apply(op.apply(new Item.Properties().setId(createItemId(name)))));
    }

    private static ResourceKey<Item> createItemId(String name) {
        return ResourceKey.create(Registries.ITEM, TJChase.prefix(name));
    }
}
