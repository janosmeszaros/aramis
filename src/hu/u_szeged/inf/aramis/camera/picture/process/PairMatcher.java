package hu.u_szeged.inf.aramis.camera.picture.process;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import org.apache.commons.math3.ml.clustering.Cluster;

import java.util.List;
import java.util.Map;

import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Pair;

import static hu.u_szeged.inf.aramis.model.Pair.pair;

public class PairMatcher {
    private final SimilarityDetector similarityDetector;

    public PairMatcher(SimilarityDetector similarityDetector) {
        this.similarityDetector = similarityDetector;
    }

    public List<Pair> findSimilarPairs(
            Table<Cluster<Coordinate>, Cluster<Coordinate>, Double> data) {
        ImmutableList.Builder<Pair> builder = ImmutableList.builder();
        for (Cluster<Coordinate> rowKey : data.rowKeySet()) {
            Map<Double, Pair> result = createOrder(data, rowKey);
            if (!result.isEmpty()) {
                builder.add(Lists.reverse(Lists.newArrayList(result.values())).get(0));
            }
        }
        return builder.build();
    }

    private Map<Double, Pair> createOrder(Table<Cluster<Coordinate>, Cluster<Coordinate>, Double> data, Cluster<Coordinate> rowKey) {
        Map<Cluster<Coordinate>, Double> row = data.row(rowKey);
        Map<Double, Pair> result = Maps.newTreeMap();
        for (Map.Entry<Cluster<Coordinate>, Double> entry : row.entrySet()) {
            Cluster<Coordinate> first = rowKey;
            Cluster<Coordinate> second = entry.getKey();
            Double similarity = entry.getValue();
            if (similarityDetector.isSimilar(first, second, similarity)) {
                result.put(similarity, pair(first, second));
            }
        }
        return result;
    }
}
