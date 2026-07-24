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

import com.google.common.collect.ImmutableList;
import illusnow.tjchase.TJChase;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;

public enum ProficiencyMainLevel {
    BEGINNER("beginner", 0, new int[]{100, 200, 300, 400, 500, 600}, UnaryOperator.identity()),
    APPRENTICE("apprentice", 1, new int[]{200, 400, 500, 600, 700, 800}, style -> style.withColor(ChatFormatting.GREEN)),
    ELITE("elite", 2, new int[]{300, 400, 500, 600, 800, 1000}, style -> style.withColor(ChatFormatting.AQUA)),
    EXPERT("expert", 3, new int[]{400, 500, 600, 800, 1000, 1200}, style -> style.withColor(ChatFormatting.LIGHT_PURPLE)),
    MASTER("master", 4, new int[]{2000, 3000, 5000, 10000, Integer.MAX_VALUE - 20000}, List.of(
            style -> style.withColor(0xFFA64D),
            style -> style.withColor(0xFFA64D),
            style -> style.withColor(0xFFA64D),
            style -> style.withColor(0xFF6F26),
            style -> style.withColor(ChatFormatting.RED)
    ), true);

    public static final List<ProficiencyMainLevel> LEVELS = Arrays.stream(values()).sorted(Comparator.comparing(ProficiencyMainLevel::getId)).toList();
    private final String name;
    private final int id;
    private final int[] pointsRequired;
    private final List<UnaryOperator<Style>> styleBySublevel;
    private final boolean finalLevel;
    private final int sumOfPointsRequired;

    ProficiencyMainLevel(String name, int id, int[] pointsRequired, UnaryOperator<Style> allStyles) {
        this(name, id, pointsRequired, fillStyleList(pointsRequired.length, allStyles), false);
    }

    private static List<UnaryOperator<Style>> fillStyleList(int length, UnaryOperator<Style> allStyles) {
        ImmutableList.Builder<UnaryOperator<Style>> builder = ImmutableList.builder();
        for (int i = 0; i < length; i++) {
            builder.add(allStyles);
        }
        return builder.build();
    }

    ProficiencyMainLevel(String name, int id, int[] pointsRequired, List<UnaryOperator<Style>> styleBySublevel, boolean finalLevel) {
        if (pointsRequired.length != styleBySublevel.size()) {
            throw new IllegalArgumentException("Incorrect size of styleBySublevel: %d (should be %d)".formatted(styleBySublevel.size(), pointsRequired.length));
        }
        this.name = name;
        this.id = id;
        this.pointsRequired = pointsRequired;
        this.styleBySublevel = styleBySublevel;
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
        checkSublevel(sublevel);
        if (sublevel == 0) {
            return 0;
        }
        return Arrays.stream(pointsRequired, 0, getSublevelIndex(sublevel)).sum();
    }

    public int getUpgradeNeed(int sublevel) {
        checkSublevel(sublevel);
        if (sublevel == 0) {
            return pointsRequired[0];
        }
        if (ProficiencyLevel.isFinalLevel(this, sublevel)) {
            return ProficiencyLevel.UPPER_LIMIT;
        }
        return pointsRequired[getSublevelIndex(sublevel)];
    }

    public UnaryOperator<Style> getStyle(int sublevel) {
        checkSublevel(sublevel);
        if (sublevel == 0) {
            return styleBySublevel.getFirst();
        }
        int sublevelIndex = getSublevelIndex(sublevel);
        return styleBySublevel.get(sublevelIndex);
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

    public String getKey(int sublevel) {
        checkSublevel(sublevel);
        return "proficiency." + TJChase.MODID + ".main_level." + getName() + "." + sublevel;
    }

    public MutableComponent makeDisplayName(int sublevel) {
        return Component.translatable(getKey(sublevel));
    }

    private int getSublevelIndex(int sublevel) {
        if (sublevel == 0) {
            return 0;
        }
        return pointsRequired.length - sublevel;
    }

    private void checkSublevel(int sublevel) {
        if (sublevel < 0 || sublevel >= pointsRequired.length) {
            throw new IllegalArgumentException("Invalid sublevel: " + sublevel);
        }
    }

    public record SublevelAndRemaining(int sublevel, int remainingPoints) {}
}
