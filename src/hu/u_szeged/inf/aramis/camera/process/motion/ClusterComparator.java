package hu.u_szeged.inf.aramis.camera.process.motion;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import hu.u_szeged.inf.aramis.model.ClusterPair;
import hu.u_szeged.inf.aramis.model.ClusterWithMoments;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;

import static hu.u_szeged.inf.aramis.utils.MapUtils.sortMapWithPicture;

public class ClusterComparator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterComparator.class);
    private final MomentsCounter momentsCounter;
    private final PairMatcher pairMatcher;
    private final PreFilter preFilter;

    public ClusterComparator(MomentsCounter momentsCounter,
                             PairMatcher pairMatcher,
                             PreFilter preFilter) {
        this.momentsCounter = momentsCounter;
        this.pairMatcher = pairMatcher;
        this.preFilter = preFilter;
    }

    public Map<Picture, List<ClusterPair>> countSimilarity(
            Map<Picture, List<Cluster<Coordinate>>> clusters) {
        Map<Picture, List<ClusterWithMoments>> sortedMap =
                sortMapWithPicture(getMomentsForClusters(clusters));
        return countResult(sortedMap);
    }

    private Map<Picture, List<ClusterPair>> countResult(Map<Picture, List<ClusterWithMoments>> sortedMap) {
        Map<Picture, List<ClusterPair>> result = Maps.newLinkedHashMap();
        List<ClusterWithMoments> previousMomentsList = Lists.newArrayList();
        Picture previousPicture = null;
        for (Map.Entry<Picture, List<ClusterWithMoments>> entry : sortedMap.entrySet()) {
            List<ClusterWithMoments> actualMomentsList = entry.getValue();
            List<ClusterPair> similarClusterPairs = pairMatcher.findSimilarPairs(previousMomentsList, actualMomentsList);
            if (!similarClusterPairs.isEmpty()) {
                result.put(previousPicture, similarClusterPairs);
                LOGGER.info("Similar pairs for {} : {}", previousPicture.name, similarClusterPairs);
            } else {
                LOGGER.info("No similar pairs!");
                if (previousPicture != null) {
                    List<ClusterPair> listOfClusterPairs = transformOrphanClusters(previousMomentsList);
                    LOGGER.info("Adding orphan clusters to {} : {}", previousPicture.name, listOfClusterPairs);
                    result.put(previousPicture, listOfClusterPairs);
                }
            }
            previousMomentsList = actualMomentsList;
            previousPicture = entry.getKey();
        }
        List<ClusterPair> listOfClusterPairs = transformOrphanClusters(previousMomentsList);
        result.put(previousPicture, listOfClusterPairs);
        return result;
    }

    private List<ClusterPair> transformOrphanClusters(List<ClusterWithMoments> previousMomentsList) {
        return Lists.transform(previousMomentsList, new Function<ClusterWithMoments, ClusterPair>() {
            @Override
            public ClusterPair apply(ClusterWithMoments input) {
                return ClusterPair.pair(input.cluster);
            }
        });
    }

    private Map<Picture, List<ClusterWithMoments>> getMomentsForClusters(Map<Picture, List<Cluster<Coordinate>>> clusters) {
        ImmutableMap.Builder<Picture, List<ClusterWithMoments>> resultBuilder = ImmutableMap.builder();
        for (Map.Entry<Picture, List<Cluster<Coordinate>>> pictureListEntry : clusters.entrySet()) {
            ImmutableList.Builder<ClusterWithMoments> momentsBuilder = ImmutableList.builder();
            Picture picture = pictureListEntry.getKey();
            List<Cluster<Coordinate>> filteredClusters = preFilter.filter(pictureListEntry.getValue());
            for (Cluster<Coordinate> cluster : filteredClusters) {
                momentsBuilder.add(momentsCounter.countMoments(picture, cluster));
            }
            resultBuilder.put(picture, momentsBuilder.build());
        }
        return resultBuilder.build();
    }
}
