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

package illusnow.tjchase.entity.proficiency;

import illusnow.tjchase.util.ModComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.function.UnaryOperator;

public record ProficiencyLevel(ProficiencyMainLevel mainLevel, int sublevel, int remaining) implements Comparable<ProficiencyLevel> {
    public static final int UPPER_LIMIT = 1000000;
    public static final int UPPER_LIMIT_TOTAL = ProficiencyMainLevel.MASTER.getSumOfPointsRequiredBelow() + ProficiencyMainLevel.MASTER.getSumOfPointsRequired() + UPPER_LIMIT;

    public ProficiencyLevel {
        if (sublevel < 0 || sublevel > mainLevel.getMaxSublevel()) {
            throw new IllegalArgumentException("Invalid sublevel " + sublevel + " for the given main level");
        }
        if (remaining < 0 || !isFinalLevel(mainLevel, sublevel) && remaining >= mainLevel.getUpgradeNeed(sublevel)) {
            throw new IllegalArgumentException("Invalid remaining points " + remaining + " for the given main level and sublevel");
        }
    }

    public int getTotalPoints() {
        return mainLevel.getSumOfPointsRequiredBelow() + mainLevel.getSumOfPointsRequiredForSublevel(sublevel) + remaining;
    }

    public static ProficiencyLevel fromTotalPoints(int totalPoints) {
        for (ProficiencyMainLevel mainLevel : ProficiencyMainLevel.LEVELS) {
            int sumBelow = mainLevel.getSumOfPointsRequiredBelow();
            int sumCurrent = mainLevel.getSumOfPointsRequired();
            if (totalPoints >= sumBelow && (mainLevel.isFinalMainLevel() || totalPoints < sumBelow + sumCurrent)) {
                ProficiencyMainLevel.SublevelAndRemaining sublevel = mainLevel.getSublevel(totalPoints - sumBelow);
                return new ProficiencyLevel(mainLevel, sublevel.sublevel(), sublevel.remainingPoints());
            }
        }
        throw new IllegalArgumentException("Total points " + totalPoints + " do not correspond to any proficiency level");
    }

    public int getUpgradeNeed() {
        return mainLevel.getUpgradeNeed(sublevel);
    }

    public boolean isFinalLevel() {
        return isFinalLevel(mainLevel, sublevel);
    }

    public static boolean isFinalLevel(ProficiencyMainLevel mainLevel, int sublevel) {
        return mainLevel.isFinalMainLevel() && sublevel == 1;
    }

    public MutableComponent makeDisplayName() {
        return mainLevel.makeDisplayName(sublevel)
                .append(ModComponents.SPACED_LEFT_PARENTHESIS)
                .append(Component.literal(isFinalLevel() ? Integer.toString(remaining) : "%d/%d".formatted(remaining, mainLevel.getUpgradeNeed(sublevel))))
                .append(ModComponents.SPACED_RIGHT_PARENTHESIS)
                .withStyle(getStyle());
    }

    public UnaryOperator<Style> getStyle() {
        return mainLevel.getStyle(sublevel);
    }

    @Override
    public String toString() {
        return mainLevel.getName() + " " + sublevel + " (" + remaining + (isFinalLevel() ? "" : "/" + getUpgradeNeed()) + ")";
    }

    @Override
    public int compareTo(ProficiencyLevel o) {
        return Integer.compare(this.getTotalPoints(), o.getTotalPoints());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProficiencyLevel that = (ProficiencyLevel) o;
        return this.getTotalPoints() == that.getTotalPoints();
    }

    @Override
    public int hashCode() {
        return getTotalPoints();
    }
}
