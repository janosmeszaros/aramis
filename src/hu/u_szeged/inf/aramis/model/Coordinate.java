package hu.u_szeged.inf.aramis.model;

public class Coordinate {
    public final int x;
    public final int y;

    private Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Coordinate coordinate(int x, int y) {
        return new Coordinate(x, y);
    }
}
