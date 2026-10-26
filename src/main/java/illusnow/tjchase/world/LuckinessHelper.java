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

public class LuckinessHelper {
    public static final double GOLDEN_FEATHER_AVERAGE_PROBABILITY = 0.006;
    public static final int GOLDEN_FEATHER_MAX_LUCKINESS = 251;
    public static final int GOLDEN_FEATHER_UNLUCKY_THRESHOLD = 200;
    public static final LuckinessHelper GOLDEN_FEATHER = new LuckinessHelper(GOLDEN_FEATHER_AVERAGE_PROBABILITY, GOLDEN_FEATHER_MAX_LUCKINESS);

    private final double averageProbability;
    private final int maxLuckiness;
    private final double targetAverageDraws;
    private final double lastDraw;
    private final double standardDeviation;
    private final double distributionCenter;

    public LuckinessHelper(double averageProbability, int maxLuckiness) {
        this.averageProbability = averageProbability;
        this.maxLuckiness = maxLuckiness;
        this.targetAverageDraws = 1.0 / averageProbability;
        this.lastDraw = maxLuckiness + 1.0;
        this.standardDeviation = Math.min(targetAverageDraws - 1.0, lastDraw - targetAverageDraws) / 3.0;
        this.distributionCenter = findDistributionCenter();
    }

    public static int getGoldenFeatherLuckiness(Player player) {
        return player.getData(ModAttachments.LUCKINESS_GOLDEN_FEATHER);
    }

    public static void setGoldenFeatherLuckiness(Player player, int luckiness) {
        player.setData(ModAttachments.LUCKINESS_GOLDEN_FEATHER, luckiness);
    }

    public static boolean checkWhetherToGiveGoldenFeather(Player player) {
        int luckiness = getGoldenFeatherLuckiness(player);
        boolean lucky = player.getRandom().nextDouble() < GOLDEN_FEATHER.getCurrentProbability(luckiness);
        if (lucky) {
            setGoldenFeatherLuckiness(player, 0);
        } else {
            setGoldenFeatherLuckiness(player, luckiness + 1);
        }
        return lucky;
    }

    public double getCurrentProbability(int luckiness) {
        if (luckiness >= maxLuckiness) {
            return 1.0;
        }
        int currentDraw = Math.max(0, luckiness) + 1;
        double remainingWeight = 0.0;
        for (int draw = currentDraw; draw <= maxLuckiness + 1; draw++) {
            remainingWeight += getDrawWeight(draw);
        }
        return getDrawWeight(currentDraw) / remainingWeight;
    }

    private double getDrawWeight(int draw) {
        double scaledDistance = (draw - distributionCenter) / standardDeviation;
        return Math.exp(-0.5 * scaledDistance * scaledDistance);
    }

    private double findDistributionCenter() {
        double lowerBound = -lastDraw;
        double upperBound = lastDraw * 2.0;
        for (int iteration = 0; iteration < 64; iteration++) {
            double candidate = (lowerBound + upperBound) / 2.0;
            if (getAverageDraws(candidate) > targetAverageDraws) {
                upperBound = candidate;
            } else {
                lowerBound = candidate;
            }
        }
        return (lowerBound + upperBound) / 2.0;
    }

    private double getAverageDraws(double center) {
        double totalWeight = 0.0;
        double weightedDraws = 0.0;
        for (int draw = 1; draw <= maxLuckiness + 1; draw++) {
            double scaledDistance = (draw - center) / standardDeviation;
            double weight = Math.exp(-0.5 * scaledDistance * scaledDistance);
            totalWeight += weight;
            weightedDraws += draw * weight;
        }
        return weightedDraws / totalWeight;
    }

    public double getAverageProbability() {
        return averageProbability;
    }

    public int getMaxLuckiness() {
        return maxLuckiness;
    }

    @Override
    public String toString() {
        return "LuckinessHelper[averageProbability=%.2f%%, maxLuckiness=%d]".formatted(getAverageProbability() * 100, getMaxLuckiness());
    }
}
