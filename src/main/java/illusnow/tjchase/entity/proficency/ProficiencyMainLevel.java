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

package illusnow.tjchase.entity.proficency;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public enum ProficiencyMainLevel {
    BEGINNER("beginner", 0, new int[]{100, 200, 300, 400, 500, 600}),
    APPRENTICE("apprentice", 1, new int[]{200, 400, 500, 600, 700, 800}),
    ELITE("elite", 2, new int[]{300, 400, 500, 600, 800, 1000}),
    EXPERT("expert", 3, new int[]{400, 500, 600, 800, 1000, 1200}),
    MASTER("master", 4, new int[]{2000, 3000, 5000, 10000, Integer.MAX_VALUE - 20000}, true);

    public static final List<ProficiencyMainLevel> LEVELS = Arrays.stream(values()).sorted(Comparator.comparing(ProficiencyMainLevel::getId)).toList();
    private final String name;
    private final int id;
    private final int[] pointsRequired;
    private final boolean finalLevel;
    private final int sumOfPointsRequired;

    ProficiencyMainLevel(String name, int id, int[] pointsRequired) {
        this(name, id, pointsRequired, false);
    }

    ProficiencyMainLevel(String name, int id, int[] pointsRequired, boolean finalLevel) {
        this.name = name;
        this.id = id;
        this.pointsRequired = pointsRequired;
        this.finalLevel = finalLevel;
        this.sumOfPointsRequired = isFinalMainLevel()
                ? Arrays.stream(pointsRequired, 0, pointsRequired.length - 1).sum()
                : Arrays.stream(pointsRequired).sum();
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public boolean isFinalMainLevel() {
        return finalLevel;
    }

    public SublevelAndRemaining getSublevel(int pointsRemaining) {
        if (!isFinalMainLevel() && pointsRemaining >= getSumOfPointsRequired()) {
            throw new IllegalArgumentException("Points remaining exceeds or equals the total points required for this main level");
        }
        int totalLen = pointsRequired.length;
        int sublevelIndex = 0;
        for (int i = 0; i < totalLen; i++) {
            if (pointsRemaining < pointsRequired[i]) {
                sublevelIndex = i;
                break;
            }
            pointsRemaining -= pointsRequired[i];
        }
        if (sublevelIndex == 0) {
            return new SublevelAndRemaining(0, pointsRemaining);
        }
        return new SublevelAndRemaining(totalLen - sublevelIndex, pointsRemaining);
    }

    public int getMaxSublevel() {
        return pointsRequired.length - 1;
    }

    public int getSumOfPointsRequired() {
        return sumOfPointsRequired;
    }

    public int getSumOfPointsRequiredForSublevel(int sublevel) {
        if (sublevel < 0 || sublevel >= pointsRequired.length) {
            throw new IllegalArgumentException("Invalid sublevel: " + sublevel);
        }
        if (sublevel == 0) {
            return 0;
        }
        int sublevelIndex = pointsRequired.length - sublevel;
        return Arrays.stream(pointsRequired, 0, sublevelIndex).sum();
    }

    public int getUpgradeNeed(int sublevel) {
        if (sublevel < 0 || sublevel >= pointsRequired.length) {
            throw new IllegalArgumentException("Invalid sublevel: " + sublevel);
        }
        if (sublevel == 0) {
            return pointsRequired[0];
        }
        if (ProficiencyLevel.isFinalLevel(this, sublevel)) {
            return ProficiencyLevel.UPPER_LIMIT;
        }
        int sublevelIndex = pointsRequired.length - sublevel;
        return pointsRequired[sublevelIndex];
    }

    public int getSumOfPointsRequiredBelow() {
        int sum = 0;
        for (ProficiencyMainLevel mainLevel : LEVELS) {
            if (mainLevel.getId() == id) {
                break;
            }
            sum += mainLevel.getSumOfPointsRequired();
        }
        return sum;
    }

    @Override
    public String toString() {
        return getName();
    }

    public record SublevelAndRemaining(int sublevel, int remainingPoints) {}
}
