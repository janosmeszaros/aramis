package hu.u_szeged.inf.aramis.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;

public class Rectangle {
    public final int minX;
    public final int minY;
    public final int maxX;
    public final int maxY;

    private Rectangle(int minX, int minY, int maxX, int maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public static Rectangle rectangle(int minX, int minY, int maxX, int maxY) {
        return new Rectangle(minX, minY, maxX, maxY);
    }

    public BigDecimal getArea() {
        return new BigDecimal(maxX - minX).add(ONE).multiply(new BigDecimal(maxY - minY).add(ONE));
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
