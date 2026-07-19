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

package illusnow.tjchase.util;

import illusnow.tjchase.TJChase;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.loading.math.MolangQueries;

public final class ModMolangQueries {
    private static final String PREFIX = "query." + TJChase.MODID + "_";
    public static final String LERPED_LIMB_SWING = PREFIX + "lerped_limb_swing";

    private ModMolangQueries() {}

    public static void register() {
        MolangQueries.<LivingEntity>setActorVariable(LERPED_LIMB_SWING, actor -> actor.animatable().walkAnimation.position(actor.partialTick()));
    }
}
