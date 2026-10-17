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

package illusnow.tjchase.data.loot;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.mixin.LootContextParamSetsAccessor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public final class ModLootContextParamSets {
    public static final ContextKeySet COMMON_REPLY_LETTER_REWARD = createCommonReplyLetterReward();

    private static ContextKeySet createCommonReplyLetterReward() {
        ContextKeySet contextKeySet = new ContextKeySet.Builder()
                .required(LootContextParams.ORIGIN)
                .required(LootContextParams.THIS_ENTITY)
                .build();
        Identifier id = TJChase.prefix(ModLootTables.COMMON_REPLY_LETTER_REWARD_NAME);
        if (LootContextParamSetsAccessor.tjChase$getRegistry().put(id, contextKeySet) != null) {
            throw new IllegalStateException("Loot context parameter set is already registered: " + id);
        }
        return contextKeySet;
    }

    private ModLootContextParamSets() {}
}
