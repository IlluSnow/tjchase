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
import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.tag.ModEntityTypeTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class ModEntityTypeTagsProvider extends EntityTypeTagsProvider {
    public ModEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider, TJChase.MODID);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(Tags.EntityTypes.CAPTURING_NOT_SUPPORTED).add(ModEntities.BLUEPRINT_MANAGER.get());
        tag(Tags.EntityTypes.CAPTURING_NOT_SUPPORTED).add(ModEntities.HARP_TESTER.get());
        tag(Tags.EntityTypes.CAPTURING_NOT_SUPPORTED).add(ModEntities.VINE_MANAGER.get());
        tag(EntityTypeTags.FALL_DAMAGE_IMMUNE).add(ModEntities.EVILINIA.get());
        tag(EntityTypeTags.FALL_DAMAGE_IMMUNE).add(ModEntities.LINIA.get());
        tag(Tags.EntityTypes.TELEPORTING_NOT_SUPPORTED).add(ModEntities.BLUEPRINT_MANAGER.get());
        tag(Tags.EntityTypes.TELEPORTING_NOT_SUPPORTED).add(ModEntities.HARP_TESTER.get());
        tag(Tags.EntityTypes.TELEPORTING_NOT_SUPPORTED).add(ModEntities.VINE_MANAGER.get());
        tag(ModEntityTypeTags.TJCHASE_FRIENDLY_CATS).add(ModEntities.ZURI.get());
        tag(ModEntityTypeTags.TJCHASE_FRIENDLY_MICE);
        tag(ModEntityTypeTags.TJCHASE_FRIENDLY_MOBS).addTags(ModEntityTypeTags.TJCHASE_FRIENDLY_MICE, ModEntityTypeTags.TJCHASE_FRIENDLY_CATS);
    }
}
