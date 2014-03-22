package hu.u_szeged.inf.aramis.model;

import com.google.common.base.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.math3.ml.clustering.Cluster;

import java.io.Serializable;

public class Pair implements Serializable, Comparable<Pair> {
    public final Cluster<Coordinate> first;
    public final Optional<Cluster<Coordinate>> second;

    private Pair(Cluster<Coordinate> first, Cluster<Coordinate> second) {
        this.first = first;
        this.second = Optional.fromNullable(second);
    }

    private Pair(Cluster<Coordinate> first) {
        this.first = first;
        this.second = Optional.absent();
    }

    public static Pair pair(Cluster<Coordinate> first, Cluster<Coordinate> second) {
        return new Pair(first, second);
    }

    public static Pair pair(Cluster<Coordinate> first) {
        return new Pair(first);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public int compareTo(Pair o) {
        return -1;
    }
}
