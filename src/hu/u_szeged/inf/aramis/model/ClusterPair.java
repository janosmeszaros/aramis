package hu.u_szeged.inf.aramis.model;

import com.google.common.base.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.math3.ml.clustering.Cluster;

import java.io.Serializable;

public class ClusterPair implements Serializable, Comparable<ClusterPair> {
    public final Cluster<Coordinate> first;
    public final Optional<Cluster<Coordinate>> second;

    private ClusterPair(Cluster<Coordinate> first, Cluster<Coordinate> second) {
        this.first = first;
        this.second = Optional.fromNullable(second);
    }

    private ClusterPair(Cluster<Coordinate> first) {
        this.first = first;
        this.second = Optional.absent();
    }

    public static ClusterPair pair(Cluster<Coordinate> first, Cluster<Coordinate> second) {
        return new ClusterPair(first, second);
    }

    public static ClusterPair pair(Cluster<Coordinate> first) {
        return new ClusterPair(first);
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
    public int compareTo(ClusterPair o) {
        return -1;
    }
}
