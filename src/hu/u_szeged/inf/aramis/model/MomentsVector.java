package hu.u_szeged.inf.aramis.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class MomentsVector {
    public final double first;
    public final double second;
    public final double third;
    public final double fourth;
    public final double fifth;
    public final double sixth;
    public final double seventh;

    private MomentsVector(double first, double second, double third, double fourth, double fifth, double sixth, double seventh) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.fifth = fifth;
        this.sixth = sixth;
        this.seventh = seventh;
    }

    public static MomentsVector momentsVector(double first, double second, double third, double fourth, double fifth, double sixth, double seventh) {
        return new MomentsVector(first, second, third, fourth, fifth, sixth, seventh);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
