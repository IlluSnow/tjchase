package illusnow.tjchase.entity.proficency;

import com.google.common.collect.ImmutableSortedMap;
import illusnow.tjchase.entity.proficency.interpolator.CubicSplineInterpolator;
import illusnow.tjchase.entity.proficency.interpolator.Interpolator;

import java.util.*;

public class ProficiencyRelatedValue {
    private final SortedMap<Integer, Interpolator> interpolators;

    ProficiencyRelatedValue(List<Map<ProficiencyLevel, Double>> valueMaps) {
        var maps = checkMaps(valueMaps);
        SortedMap<Integer, Interpolator> interpolators = new TreeMap<>();
        for (var entry : maps.entrySet()) {
            int totalPoints = entry.getKey();
            Map<ProficiencyLevel, Double> valueMap = entry.getValue();
            List<Interpolator.DataPair> dataPairs = new ArrayList<>();
            for (var levelEntry : valueMap.entrySet()) {
                dataPairs.add(new Interpolator.DataPair(levelEntry.getKey().getTotalPoints(), levelEntry.getValue()));
            }
            int length = dataPairs.size();
            if (length == 2) {
                double x0 = dataPairs.get(0).x();
                double y0 = dataPairs.get(0).y();
                double x1 = dataPairs.get(1).x();
                double y1 = dataPairs.get(1).y();
                dataPairs.add(1, new Interpolator.DataPair((x0 + x1) / 2, Math.sqrt(y0 * y1)));
            }
            Interpolator interpolator = length == 1 ? Interpolator.singleton(dataPairs.get(0).y()) : CubicSplineInterpolator.createClamped(dataPairs);
            interpolators.put(totalPoints, interpolator);
        }
        this.interpolators = ImmutableSortedMap.copyOf(interpolators);
    }

    private static Map<Integer, Map<ProficiencyLevel, Double>> checkMaps(List<Map<ProficiencyLevel, Double>> valueMaps) {
        if (valueMaps.isEmpty()) {
            throw new IllegalArgumentException("Value map list cannot be empty.");
        }
        for (var map : valueMaps) {
            if (map.isEmpty()) {
                throw new IllegalArgumentException("Value map cannot be empty.");
            }
            int currentTotalPoints = -1;
            for (var entry : map.entrySet()) {
                int totalPoints = entry.getKey().getTotalPoints();
                if (totalPoints < 0) {
                    throw new IllegalArgumentException("ProficiencyLevel total points cannot be negative.");
                }
                if (totalPoints <= currentTotalPoints) {
                    throw new IllegalArgumentException("ProficiencyLevel total points must be in ascending order.");
                }
                currentTotalPoints = totalPoints;
            }
        }
        Map<Integer, Map<ProficiencyLevel, Double>> result = new TreeMap<>();
        for (int i = 0; i < valueMaps.size() - 1; i++) {
            Map<ProficiencyLevel, Double> currentMap = valueMaps.get(i);
            Map<ProficiencyLevel, Double> nextMap = valueMaps.get(i + 1);
            OptionalInt currentMax = currentMap.keySet().stream().mapToInt(ProficiencyLevel::getTotalPoints).max();
            OptionalInt nextMin = nextMap.keySet().stream().mapToInt(ProficiencyLevel::getTotalPoints).min();
            if (currentMax.isEmpty() || nextMin.isEmpty() || currentMax.getAsInt() != nextMin.getAsInt()) {
                throw new IllegalArgumentException("The values are not continuous between valueMaps.");
            }
            result.put(currentMax.getAsInt(), currentMap);
            if (i == valueMaps.size() - 2) {
                result.put(Integer.MAX_VALUE, nextMap);
            }
        }
        return result;
    }

    public float floatValue(ProficiencyLevel level) {
        return (float) doubleValue(level);
    }

    public double doubleValue(ProficiencyLevel level) {
        return doubleValue(level.getTotalPoints());
    }

    public float floatValue(int totalPoints) {
        return (float) doubleValue(totalPoints);
    }

    public double doubleValue(int totalPoints) {
        for (Map.Entry<Integer, Interpolator> entry : interpolators.entrySet()) {
            if (totalPoints < entry.getKey()) {
                return entry.getValue().interpolate(totalPoints);
            }
        }
        return interpolators.lastEntry().getValue().interpolate(totalPoints);
    }

    public static Builder beginner0(double beginner0) {
        return new Builder(beginner0);
    }

    public static class Builder {
        private final List<Map<ProficiencyLevel, Double>> valueMap;

        Builder(double beginner0) {
            valueMap = new ArrayList<>();
            valueMap.add(new TreeMap<>(Comparator.comparing(ProficiencyLevel::getTotalPoints)));
            whenReached(ProficiencyMainLevel.BEGINNER, beginner0);
        }

        public Builder whenReached(ProficiencyMainLevel mainLevel, double value) {
            return add(mainLevel, 0, value);
        }

        public Builder whenMaster(double value) {
            return whenMaster(0, value);
        }

        public Builder whenMaster(int sublevel, double value) {
            return add(ProficiencyMainLevel.MASTER, sublevel, value);
        }

        public Builder add(ProficiencyMainLevel mainLevel, int sublevel, double value) {
            return add(new ProficiencyLevel(mainLevel, sublevel, 0), value);
        }

        public Builder add(ProficiencyLevel level, double value) {
            valueMap.getLast().put(level, value);
            return this;
        }

        public Builder masterCutoff(double value) {
            return masterCutoff(0, value);
        }

        public Builder masterCutoff(int sublevel, double value) {
            return cutoff(ProficiencyMainLevel.MASTER, sublevel, value);
        }

        public Builder cutoff(ProficiencyMainLevel mainLevel, int sublevel, double value) {
            return cutoff(new ProficiencyLevel(mainLevel, sublevel, 0), value);
        }

        public Builder cutoff(ProficiencyLevel level, double value) {
            add(level, value);
            valueMap.add(new TreeMap<>(Comparator.comparing(ProficiencyLevel::getTotalPoints)));
            return this;
        }

        public ProficiencyRelatedValue build() {
            return new ProficiencyRelatedValue(valueMap);
        }
    }
}
