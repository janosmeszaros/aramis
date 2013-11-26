package hu.u_szeged.inf.aramis.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.math3.ml.clustering.Clusterable;

public class Coordinate implements Clusterable {
    public final int x;
    public final int y;

    private Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Coordinate coordinate(int x, int y) {
        return new Coordinate(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Coordinate that = (Coordinate) o;

        if (x != that.x) return false;
        if (y != that.y) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public double[] getPoint() {
        return new double[]{x, y};
    }
}
