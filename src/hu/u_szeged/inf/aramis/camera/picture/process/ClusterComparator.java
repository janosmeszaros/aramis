package hu.u_szeged.inf.aramis.camera.picture.process;

import com.google.common.base.Function;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import org.apache.commons.math3.ml.clustering.Cluster;

import java.util.List;
import java.util.Map;

import hu.u_szeged.inf.aramis.model.ClusterWithMoments;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Pair;
import hu.u_szeged.inf.aramis.model.Picture;

import static hu.u_szeged.inf.aramis.Utils.MapUtils.sortMapWithPicture;

public class ClusterComparator {
    private final MomentsCounter momentsCounter;
    private final MomentsDistanceCounter distanceCounter;
    private final PairMatcher pairMatcher;

    public ClusterComparator(MomentsCounter momentsCounter, MomentsDistanceCounter counter, PairMatcher pairMatcher) {
        this.momentsCounter = momentsCounter;
        this.distanceCounter = counter;
        this.pairMatcher = pairMatcher;
    }

    public Map<Picture, List<Pair>> countSimilarity(
            Map<Picture, List<Cluster<Coordinate>>> clusters) {
        Map<Picture, List<ClusterWithMoments>> sortedMap =
                sortMapWithPicture(getMomentsForClusters(clusters));

        return countResult(sortedMap);
    }

    private Map<Picture, List<Pair>> countResult(Map<Picture, List<ClusterWithMoments>> sortedMap) {
        Map<Picture, List<Pair>> result = Maps.newLinkedHashMap();
        List<ClusterWithMoments> previousMomentsList = Lists.newArrayList();
        for (Map.Entry<Picture, List<ClusterWithMoments>> entry : sortedMap.entrySet()) {
            List<ClusterWithMoments> actualMomentsList = entry.getValue();
            Picture actualPicture = entry.getKey();
            if (previousMomentsList.isEmpty()) {
                previousMomentsList = actualMomentsList;
                continue;
            } else {
                Table<Cluster<Coordinate>, Cluster<Coordinate>, Double> table =
                        countMomentsDistances(previousMomentsList, actualMomentsList);
                if (!table.isEmpty()) {
                    result.put(actualPicture, pairMatcher.findSimilarPairs(table));
                } else {
                    result.put(actualPicture, Lists.transform(actualMomentsList, new Function<ClusterWithMoments, Pair>() {
                        @Override
                        public Pair apply(ClusterWithMoments input) {
                            return Pair.pair(input.cluster);
                        }
                    }));
                }
                previousMomentsList = actualMomentsList;
            }
        }
        return result;
    }

    private Table<Cluster<Coordinate>, Cluster<Coordinate>, Double> countMomentsDistances(
            List<ClusterWithMoments> previousMomentsList, List<ClusterWithMoments> actualMomentsList) {
        Table<Cluster<Coordinate>, Cluster<Coordinate>, Double> result = HashBasedTable.create();
        for (ClusterWithMoments previousMoments : previousMomentsList) {
            for (ClusterWithMoments actualMoments : actualMomentsList) {
                result.put(previousMoments.cluster,
                        actualMoments.cluster,
                        distanceCounter.countDistances(previousMoments, actualMoments));
            }
        }
        return result;
    }

    private Map<Picture, List<ClusterWithMoments>> getMomentsForClusters(Map<Picture, List<Cluster<Coordinate>>> clusters) {
        ImmutableMap.Builder<Picture, List<ClusterWithMoments>> resultBuilder = ImmutableMap.builder();
        for (Map.Entry<Picture, List<Cluster<Coordinate>>> pictureListEntry : clusters.entrySet()) {
            Picture picture = pictureListEntry.getKey();
            ImmutableList.Builder<ClusterWithMoments> momentsBuilder = ImmutableList.builder();
            for (Cluster<Coordinate> cluster : pictureListEntry.getValue()) {
                momentsBuilder.add(momentsCounter.countMoments(picture, cluster));
            }
            resultBuilder.put(picture, momentsBuilder.build());
        }
        return resultBuilder.build();
    }
}
