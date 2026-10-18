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

import illusnow.tjchase.item.ModItems;
import illusnow.tjchase.mixin.LivingEntityAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

public final class StampDropHelper {
    private static final double INITIAL_DROP = 4;
    private static final double MUL1 = 0.75;
    private static final double MUL2 = 0.9;
    private static final double MUL3 = 0.95;
    private static final int CNT1 = 5;
    private static final int CNT2 = 10;
    private static final double P1 = Math.pow(MUL1, CNT1 - 1);
    private static final double P2 = P1 * Math.pow(MUL2, CNT2 - CNT1);

    private StampDropHelper() {}

    public static void mayDropStamp(LivingDropsEvent event) {
        DamageSource source = event.getSource();
        LivingEntity entity = event.getEntity();
        if (event.isRecentlyHit()
                && entity instanceof Enemy
                && source.getEntity() instanceof ServerPlayer player
                && (((LivingEntityAccessor) entity).callShouldDropLoot(player.level())) ) {
            double expected = getExpectedDropCount(player.getStats().getValue(Stats.ENTITY_KILLED, entity.getType()));

            int dropCount = getDropCount(player.getRandom(), expected);
            if (dropCount > 0) {
                ItemEntity drop = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), new ItemStack(ModItems.STAMP.get(), dropCount));
                event.getDrops().add(drop);
            }
        }
    }

    private static int getDropCount(RandomSource random, double expected) {
        int min = (int) expected;
        return min + (random.nextDouble() < (expected - min) ? 1 : 0);
    }

    public static double getExpectedDropCount(int accumulatedCount) {
        if (accumulatedCount <= 0) {
            return INITIAL_DROP;
        }
        if (accumulatedCount <= CNT1) {
            return INITIAL_DROP * Math.pow(MUL1, accumulatedCount - 1);
        }
        if (accumulatedCount <= CNT2) {
            return INITIAL_DROP * P1 * Math.pow(MUL2, accumulatedCount - CNT1);
        }
        return INITIAL_DROP * P2 * Math.pow(MUL3, accumulatedCount - CNT2);
    }
}
