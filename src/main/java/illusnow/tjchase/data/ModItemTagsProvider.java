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
import illusnow.tjchase.item.ModItems;
import illusnow.tjchase.tag.ModItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TJChase.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModItemTags.HARPS).add(ModItems.HARP.get(), ModItems.NETHERITE_HARP.get());
        tag(Tags.Items.RANGED_WEAPON_TOOLS).addTag(ModItemTags.HARPS);
        tag(ItemTags.DURABILITY_ENCHANTABLE).add(ModItems.HARP.get(), ModItems.NETHERITE_HARP.get());
        tag(ItemTags.VANISHING_ENCHANTABLE).add(ModItems.HARP.get(), ModItems.NETHERITE_HARP.get());
    }
}
