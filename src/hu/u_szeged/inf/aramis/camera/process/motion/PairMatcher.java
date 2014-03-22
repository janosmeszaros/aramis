package hu.u_szeged.inf.aramis.camera.process.motion;

import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import hu.u_szeged.inf.aramis.model.ClusterWithMoments;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Pair;
import hu.u_szeged.inf.aramis.model.SimilarityVector;

import static hu.u_szeged.inf.aramis.model.SimilarityVector.similarityVector;

public class PairMatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(PairMatcher.class);
    private final SimilarityDetector similarityDetector;
    private final MomentsDistanceCounter distanceCounter;
    private final SimilarityVector highestMin;

    public PairMatcher(SimilarityDetector similarityDetector, MomentsDistanceCounter distanceCounter) {
        this.similarityDetector = similarityDetector;
        this.distanceCounter = distanceCounter;
        highestMin = similarityVector(similarityDetector.momentBorder, similarityDetector.distanceBorder, similarityDetector.areaDifferenceBorder);
    }

    public List<Pair> findSimilarPairs(List<ClusterWithMoments> previous,
                                       List<ClusterWithMoments> actual) {
        ImmutableList.Builder<Pair> builder = ImmutableList.builder();
        Table<Cluster<Coordinate>, Cluster<Coordinate>, SimilarityVector> table = createTable(previous, actual);
        while (table.size() > 0) {
            Pair minimumInTable = findMinimumInTable(table);
            table.row(minimumInTable.first).clear();
            table.column(minimumInTable.second.get()).clear();
            builder.add(minimumInTable);
        }
        return builder.build();
    }

    private Pair findMinimumInTable(Table<Cluster<Coordinate>, Cluster<Coordinate>, SimilarityVector> table) {
        SimilarityVector min = highestMin;
        Pair result = null;
        for (Cluster<Coordinate> rowKey : table.rowKeySet()) {
            for (Map.Entry<Cluster<Coordinate>, SimilarityVector> row : table.row(rowKey).entrySet()) {
                LOGGER.info("Compare min {} to {}", new Object[]{min.momentsDistance, row.getValue().momentsDistance});
                if (min.compareTo(row.getValue()) > -1) {
                    min = row.getValue();
                    result = Pair.pair(rowKey, row.getKey());
                }
            }
        }
        LOGGER.info("Min pair found {}", result);
        return result;
    }

    private Table<Cluster<Coordinate>, Cluster<Coordinate>, SimilarityVector> createTable(
            List<ClusterWithMoments> previous,
            List<ClusterWithMoments> actual) {
        Table<Cluster<Coordinate>, Cluster<Coordinate>, SimilarityVector> similarityTable = HashBasedTable.create();
        for (ClusterWithMoments previousMoments : previous) {
            for (ClusterWithMoments actualMoments : actual) {
                double distance = distanceCounter.countDistances(previousMoments, actualMoments);
                Cluster<Coordinate> previousCluster = previousMoments.cluster;
                Cluster<Coordinate> actualCluster = actualMoments.cluster;
                Optional<SimilarityVector> similarityVector =
                        similarityDetector.countSimilarity(previousCluster, actualCluster, distance);
                if (similarityVector.isPresent()) {
                    similarityTable.put(previousCluster, actualCluster, similarityVector.get());
                } else {
                    //Todo save orphans
                }
            }
        }
        return similarityTable;
    }
}
