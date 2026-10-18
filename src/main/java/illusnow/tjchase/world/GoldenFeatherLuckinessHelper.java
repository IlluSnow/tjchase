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

import illusnow.tjchase.attachment.ModAttachments;
import net.minecraft.world.entity.player.Player;

public final class GoldenFeatherLuckinessHelper {
    public static final double AVERAGE_PROBABILITY = 0.006;
    public static final int MAX_LUCKINESS = 251;
    public static final int AVERAGE_LUCKINESS = 170;
    public static final int UNLUCKY_THRESHOLD = 200;
    private static final double LEFT_STANDARD_DEVIATION = 34.73;
    private static final double RIGHT_STANDARD_DEVIATION = findRightStandardDeviation();

    private GoldenFeatherLuckinessHelper() {}

    public static int getLuckiness(Player player) {
        return player.getData(ModAttachments.LUCKINESS_GOLDEN_FEATHER);
    }

    public static void setLuckiness(Player player, int luckiness) {
        player.setData(ModAttachments.LUCKINESS_GOLDEN_FEATHER, luckiness);
    }

    public static boolean checkWhetherToGiveGoldenFeather(Player player) {
        int luckiness = getLuckiness(player);
        boolean lucky = player.getRandom().nextDouble() < getCurrentProbability(luckiness);
        if (lucky) {
            setLuckiness(player, 0);
        } else {
            setLuckiness(player, luckiness + 1);
        }
        return lucky;
    }

    private static double getCurrentProbability(int luckiness) {
        if (luckiness >= MAX_LUCKINESS) {
            return 1.0;
        }

        int currentDraw = Math.max(0, luckiness) + 1;
        double remainingWeight = 0.0;
        for (int draw = currentDraw; draw <= MAX_LUCKINESS + 1; draw++) {
            remainingWeight += getDrawWeight(draw);
        }

        return getDrawWeight(currentDraw) / remainingWeight;
    }

    private static double getDrawWeight(int draw) {
        double distance = draw - AVERAGE_LUCKINESS;
        double standardDeviation = distance < 0.0 ? LEFT_STANDARD_DEVIATION : RIGHT_STANDARD_DEVIATION;
        double scaledDistance = distance / standardDeviation;
        return Math.exp(-0.5 * scaledDistance * scaledDistance);
    }

    private static double findRightStandardDeviation() {
        double targetAverageDraws = 1.0 / AVERAGE_PROBABILITY;
        double lowerBound = 0.0;
        double upperBound = MAX_LUCKINESS + 1.0;

        for (int iteration = 0; iteration < 64; iteration++) {
            double candidate = (lowerBound + upperBound) / 2.0;
            if (getAverageDraws(candidate) < targetAverageDraws) {
                lowerBound = candidate;
            } else {
                upperBound = candidate;
            }
        }

        return (lowerBound + upperBound) / 2.0;
    }

    private static double getAverageDraws(double rightStandardDeviation) {
        double totalWeight = 0.0;
        double weightedDraws = 0.0;

        for (int draw = 1; draw <= MAX_LUCKINESS + 1; draw++) {
            double distance = draw - AVERAGE_LUCKINESS;
            double standardDeviation = distance < 0.0 ? LEFT_STANDARD_DEVIATION : rightStandardDeviation;
            double scaledDistance = distance / standardDeviation;
            double weight = Math.exp(-0.5 * scaledDistance * scaledDistance);
            totalWeight += weight;
            weightedDraws += draw * weight;
        }

        return weightedDraws / totalWeight;
    }
}
