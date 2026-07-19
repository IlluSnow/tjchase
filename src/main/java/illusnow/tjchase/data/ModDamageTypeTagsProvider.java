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
import illusnow.tjchase.world.ModDamageSources;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.tags.DamageTypeTags;

import java.util.concurrent.CompletableFuture;

public class ModDamageTypeTagsProvider extends DamageTypeTagsProvider {
    public ModDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TJChase.MODID);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(DamageTypeTags.IS_PROJECTILE).add(ModDamageSources.ORBITING_BLOCK, ModDamageSources.INDIRECT_ORBITING_BLOCK, ModDamageSources.YOGA_BALL);
        tag(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS).add(ModDamageSources.ORBITING_BLOCK, ModDamageSources.INDIRECT_ORBITING_BLOCK);
        tag(DamageTypeTags.PANIC_CAUSES).add(ModDamageSources.MOB_ATTACK_NO_SCALING, ModDamageSources.ORBITING_BLOCK, ModDamageSources.INDIRECT_ORBITING_BLOCK, ModDamageSources.YOGA_BALL);
    }
}
