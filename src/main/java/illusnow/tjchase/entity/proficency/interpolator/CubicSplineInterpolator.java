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

package illusnow.tjchase.entity.proficency.interpolator;

import java.util.List;

public class CubicSplineInterpolator implements Interpolator {
    private final double[] x;
    private final double[] y;
    private final double[] b, c, d;

    CubicSplineInterpolator(double[] xInput, double[] yInput) {
        LinearInterpolator.interpolatorArgumentCheck(xInput, yInput, true);
        int n = xInput.length;
        x = new double[n];
        y = new double[n];
        System.arraycopy(xInput, 0, x, 0, n);
        System.arraycopy(yInput, 0, y, 0, n);

        int intervals = n - 1;
        double[] h = new double[intervals];
        for (int i = 0; i < intervals; i++) {
            h[i] = x[i + 1] - x[i];
        }

        double[] alpha = new double[intervals];
        for (int i = 1; i < intervals; i++) {
            alpha[i] = 3.0 / h[i] * (y[i + 1] - y[i]) - 3.0 / h[i - 1] * (y[i] - y[i - 1]);
        }

        double[] l = new double[n];
        double[] mu = new double[n];
        double[] z = new double[n];
        l[0] = 1.0;
        mu[0] = 0.0;
        z[0] = 0.0;

        for (int i = 1; i < intervals; i++) {
            l[i] = 2.0 * (x[i + 1] - x[i - 1]) - h[i - 1] * mu[i - 1];
            mu[i] = h[i] / l[i];
            z[i] = (alpha[i] - h[i - 1] * z[i - 1]) / l[i];
        }

        l[intervals] = 1.0;
        z[intervals] = 0.0;

        c = new double[n];
        b = new double[intervals];
        d = new double[intervals];
        c[intervals] = 0.0;

        for (int j = intervals - 1; j >= 0; j--) {
            c[j] = z[j] - mu[j] * c[j + 1];
            b[j] = (y[j + 1] - y[j]) / h[j] - h[j] * (c[j + 1] + 2.0 * c[j]) / 3.0;
            d[j] = (c[j + 1] - c[j]) / (3.0 * h[j]);
        }
    }

    public static CubicSplineInterpolator create(List<DataPair> data) {
        double[] x = new double[data.size()];
        double[] y = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            x[i] = data.get(i).x();
            y[i] = data.get(i).y();
        }
        return new CubicSplineInterpolator(x, y);
    }

    public static CubicSplineInterpolator createClamped(List<DataPair> data) {
        double[] x = new double[data.size()];
        double[] y = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            x[i] = data.get(i).x();
            y[i] = data.get(i).y();
        }
        return new Clamped(x, y);
    }

    @Override
    public double interpolate(double targetX) {
        int n = x.length - 1;
        if (targetX <= x[0]) {
            return evaluatePolynomial(0, targetX);
        }
        if (targetX >= x[n]) {
            return evaluatePolynomial(n - 1, targetX);
        }

        int low = 0, high = n;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (targetX == x[mid]) return y[mid];
            else if (targetX < x[mid]) high = mid - 1;
            else low = mid + 1;
        }
        return evaluatePolynomial(low - 1, targetX);
    }

    private double evaluatePolynomial(int j, double targetX) {
        double dx = targetX - x[j];
        return y[j] + b[j] * dx + c[j] * dx * dx + d[j] * dx * dx * dx;
    }

    private static class Clamped extends CubicSplineInterpolator {
        private final double maxX;
        private final double minX;
        private final double ly;
        private final double ry;

        Clamped(double[] xInput, double[] yInput) {
            super(xInput, yInput);
            this.minX = xInput[0];
            this.maxX = xInput[xInput.length - 1];
            this.ly = yInput[0];
            this.ry = yInput[yInput.length - 1];
        }

        @Override
        public double interpolate(double targetX) {
            if (targetX <= minX) {
                return ly;
            }
            if (targetX >= maxX) {
                return ry;
            }
            return super.interpolate(targetX);
        }
    }
}