package hu.u_szeged.inf.aramis.utils;

public final class DistanceUtils {
    private DistanceUtils() {
    }

    public static double canberraDistance(double first, double second) {
        return Math.abs(first - second) / (Math.abs(first) + Math.abs(second));
    }
}