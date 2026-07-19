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
import illusnow.tjchase.item.enchantment.ModEnchantments;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.EnchantmentTags;

import java.util.concurrent.CompletableFuture;

public class ModEnchantmentTagsProvider extends EnchantmentTagsProvider {
    public ModEnchantmentTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TJChase.MODID);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(EnchantmentTags.NON_TREASURE).add(ModEnchantments.FORCEFUL, ModEnchantments.EXPLOSIVE, ModEnchantments.OVERLOAD, ModEnchantments.LIGHTWEIGHT, ModEnchantments.ANTIGRAVITY, ModEnchantments.SEEKING);
        tag(EnchantmentTags.TREASURE).add(ModEnchantments.ANGEL_TOM_PASSIVE_2, ModEnchantments.ANGEL_TOM_WEAPON_2, ModEnchantments.ANGEL_TOM_WEAPON_3);
    }
}
