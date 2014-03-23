package hu.u_szeged.inf.aramis.model;

public class SimilarityVector implements Comparable<SimilarityVector> {
    public final Double momentsDistance;
    public final Double euclideanDistance;
    public final Double areaDifference;

    private SimilarityVector(Double momentsDistance, Double euclideanDistance, Double areaDifference) {
        this.momentsDistance = momentsDistance;
        this.euclideanDistance = euclideanDistance;
        this.areaDifference = areaDifference;
    }

    public static SimilarityVector similarityVector(Double momentsDistance, Double euclideanDistance, Double areaDifference) {
        return new SimilarityVector(momentsDistance, euclideanDistance, areaDifference);
    }

    @Override
    public int compareTo(SimilarityVector other) {
        return momentsDistance.compareTo(other.momentsDistance) +
                euclideanDistance.compareTo(other.euclideanDistance) +
                areaDifference.compareTo(other.areaDifference);
    }
}
