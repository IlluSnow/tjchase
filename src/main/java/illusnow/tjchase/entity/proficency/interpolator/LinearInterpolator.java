package illusnow.tjchase.entity.proficency.interpolator;

import java.util.Arrays;
import java.util.List;

public class LinearInterpolator implements Interpolator {
    private final double[] x;
    private final double[] y;

    LinearInterpolator(double[] xInput, double[] yInput) {
        interpolatorArgumentCheck(xInput, yInput, false);
        int n = xInput.length;
        x = new double[n];
        y = new double[n];
        System.arraycopy(xInput, 0, this.x, 0, n);
        System.arraycopy(yInput, 0, this.y, 0, n);
    }

    public static LinearInterpolator create(List<DataPair> data) {
        double[] x = new double[data.size()];
        double[] y = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            x[i] = data.get(i).x();
            y[i] = data.get(i).y();
        }
        return new LinearInterpolator(x, y);
    }

    @Override
    public double interpolate(double targetX) {
        int n = x.length;

        if (targetX <= x[0]) {
            return doLinear(0, 1, targetX);
        }
        if (targetX >= x[n - 1]) {
            return doLinear(n - 2, n - 1, targetX);
        }

        int index = Arrays.binarySearch(x, targetX);
        if (index >= 0) {
            return y[index];
        }

        int insertionPoint = -(index + 1);
        int i = insertionPoint - 1;

        return doLinear(i, i + 1, targetX);
    }

    private double doLinear(int i0, int i1, double targetX) {
        double x0 = x[i0], y0 = y[i0];
        double x1 = x[i1], y1 = y[i1];
        return y0 + (targetX - x0) * (y1 - y0) / (x1 - x0);
    }

    static void interpolatorArgumentCheck(double[] xInput, double[] yInput, boolean checkLength) {
        if (xInput.length != yInput.length) {
            throw new IllegalArgumentException("The length of x and y arrays must be the same");
        }
        if (checkLength && xInput.length < 3) {
            throw new IllegalArgumentException("At least 3 data points are required for interpolation");
        }
        for (int i = 0; i < xInput.length - 1; i++) {
            if (xInput[i] >= xInput[i + 1]) {
                throw new IllegalArgumentException("X values must be strictly increasing");
            }
        }
    }
}
