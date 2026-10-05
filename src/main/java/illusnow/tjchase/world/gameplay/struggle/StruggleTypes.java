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

package illusnow.tjchase.world.gameplay.struggle;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.util.ModRegistries;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class StruggleTypes {
    public static final DeferredRegister<StruggleType> STRUGGLE_TYPES = DeferredRegister.create(ModRegistries.STRUGGLE_TYPES_KEY, TJChase.MODID);
    public static final DeferredHolder<StruggleType, StruggleType> PLAYER = STRUGGLE_TYPES.register("player", () -> new PlayerStruggleType(Items.NETHER_STAR.getDefaultInstance(), StruggleType.FREQUENCY * 16));
    public static final DeferredHolder<StruggleType, StruggleType> ROCKET_5 = STRUGGLE_TYPES.register("rocket_5", () -> new BaseRocketStruggleType.DelayFlying(Items.CAKE.getDefaultInstance(), StruggleType.FREQUENCY * 2, 5));
    public static final DeferredHolder<StruggleType, StruggleType> ROCKET_10 = STRUGGLE_TYPES.register("rocket_10", () -> new BaseRocketStruggleType.DelayFlying(Items.COOKED_BEEF.getDefaultInstance(), StruggleType.FREQUENCY * 7 / 2, 10));
    public static final DeferredHolder<StruggleType, StruggleType> ROCKET_LUCKY = STRUGGLE_TYPES.register("rocket_lucky", () -> new BaseRocketStruggleType.DirectlyUntie(Items.NETHER_STAR.getDefaultInstance(), StruggleType.FREQUENCY * 7));

}
