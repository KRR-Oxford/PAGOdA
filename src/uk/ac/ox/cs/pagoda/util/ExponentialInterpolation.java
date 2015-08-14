package uk.ac.ox.cs.pagoda.util;

/***
 * Get an exponential function given two points.
 */
public class ExponentialInterpolation {

    private final double base;
    private final double multiplicativeFactor;

    /***
     * Compute the exponential function passing for the 2 given points.
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    public ExponentialInterpolation(double x1, double y1, double x2, double y2) {
        base = Math.pow(y2/y1, 1 / (x2 - x1));
        multiplicativeFactor = y1 / Math.pow(base, x1);
    }

    /***
     * Compute value of the function in x.
     *
     * @param x
     * @return
     */
    public double computeValue(double x) {
        return multiplicativeFactor * Math.pow(base, x);
    }
}
