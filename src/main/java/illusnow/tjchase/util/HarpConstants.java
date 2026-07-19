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

import net.minecraft.world.level.block.Blocks;

import java.util.List;

public final class HarpConstants {
    public static final int DURABILITY = 300;
    public static final int DURABILITY_NETHERITE = 2031;
    public static final int ENCHANTMENT_VALUE = 25;
    public static final int ENCHANTMENT_VALUE_NETHERITE = 15;
    public static final int BASE_PLAY_DURATION = 40;
    public static final int BASE_COOLDOWN = 100;
    public static final int BASE_PLAY_COOLDOWN = 20;
    public static final double PLAY_SOUND_PROGRESS = 0.25;
    public static final double SHOW_NOTE_PROGRESS = 0.5;

    public static final int BASE_ORBITING_BLOCK_COUNT = 3;
    public static final float BASE_ORBITING_BLOCK_DAMAGE = 3;
    public static final float BASE_ORBITING_BLOCK_DAMAGE_CONSTANT = BASE_ORBITING_BLOCK_DAMAGE - calculateBaseDamageFromHardness(0);
    public static final int ORBITING_BLOCK_COUNT_NETHERITE_BONUS = 2;
    public static final float ORBITING_BLOCK_DAMAGE_NETHERITE_BONUS = 5;
    public static final double DEFAULT_ORBITING_BLOCK_HEIGHT_MUL = 0.6;
    public static final double ORBITING_BLOCK_INERTIA = 1;
    public static final double ORBITING_BLOCK_INERTIA_WATER = 1;
    public static final float DEFAULT_ORBITING_BLOCK_SIZE = 0.4F;
    public static final float DEFAULT_ORBITING_BLOCK_ROTATING_SPEED = 4.5F;
    public static final double ORBITING_BLOCK_GRAVITY = 0.03;
    public static final float ORBITING_BLOCK_SHOOT_VELOCITY = 2;
    public static final float ORBITING_BLOCK_SHOOT_INACCURACY = 0;
    public static final double SEEK_VELOCITY_MULTIPLIER = 0.75;
    public static final double DEFAULT_ORBIT_RADIUS = 1.25 * Math.sqrt(2);
    public static final float ORBITING_BLOCK_DAMAGE_REDUCTION = 6;
    public static final float ORBITING_BLOCK_MIN_DAMAGE_TAKEN = 0.15F;
    public static final double DEFAULT_SEEK_POWER = 0.4;

    public static final float TNT_EXPLOSION_POWER = 3;
    public static final float TNT_EXPLOSION_POWER_NEARBY = 1;
    public static final float HEAVY_BLOCK_MIN_HARDNESS = 10;

    public static final float FORCEFUL_ENCHANTMENT_DAMAGE_BOOST_PER_LEVEL = 1.5F;
    public static final float FORCEFUL_ENCHANTMENT_DAMAGE_BOOST_BOSS_PER_LEVEL = 1F;
    public static final float EXPLOSIVE_ENCHANTMENT_BASE_AOE_DAMAGE = 0.5F;
    public static final float EXPLOSIVE_ENCHANTMENT_AOE_DAMAGE_PER_LEVEL = 0.15F;
    public static final float EXPLOSIVE_ENCHANTMENT_BASE_AOE_RADIUS = 3F;
    public static final float EXPLOSIVE_ENCHANTMENT_AOE_RADIUS_PER_LEVEL = 1F;
    public static final int OVERLOAD_ENCHANTMENT_USE_COOLDOWN_TICKS_DECREASE_PER_LEVEL = 20;
    public static final int LIGHTWEIGHT_ENCHANTMENT_PLAY_COOLDOWN_TICKS_DECREASE_PER_LEVEL = 5;
    public static final int LIGHTWEIGHT_ENCHANTMENT_PLAY_TICKS_DECREASE_PER_LEVEL = 8;
    public static final float ANTIGRAVITY_SHOOT_VELOCITY_MULTIPLIER = 1.5F;
    public static final float SEEKING_ENCHANTMENT_SEEKING_RANGE_PER_LEVEL = 4;
    public static final float SEEKING_ENCHANTMENT_BASE_SEEK_POWER = (float) DEFAULT_SEEK_POWER;
    public static final float SEEKING_ENCHANTMENT_SEEK_POWER_PER_LEVEL = 0.2F;
    public static final float ANGEL_TOM_WEAPON3_ENCHANTMENT_MINIMUM_HEALTH = 10;

    public static final WeightedBlockSelector DEFAULT_WEAPON2_BLOCK_SELECTOR = new WeightedBlockSelector(List.of(
            new WeightedBlock(Blocks.COBBLESTONE.defaultBlockState(), 50),
            new WeightedBlock(Blocks.DIRT.defaultBlockState(), 30),
            new WeightedBlock(Blocks.COBBLED_DEEPSLATE.defaultBlockState(), 20),
            new WeightedBlock(Blocks.SAND.defaultBlockState(), 10),
            new WeightedBlock(Blocks.OBSIDIAN.defaultBlockState(), 4)
    ));

    private HarpConstants() {}

    public static float calculateBaseDamageFromHardness(double hardness) {
        return (float) (4 * Math.log(hardness / 5 + 0.2));
    }
}
