package illusnow.tjchase.entity.proficency.interpolator;

public interface Interpolator {
    double interpolate(double targetX);

    static Interpolator singleton(double y) {
        return targetX -> y;
    }

    record DataPair(double x, double y) implements Comparable<DataPair> {
        @Override
        public int compareTo(DataPair o) {
            if (Double.compare(x, o.x) == 0) {
                return Double.compare(y, o.y);
            }
            return Double.compare(x, o.x);
        }
    }
}
