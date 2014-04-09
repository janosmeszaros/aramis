package hu.u_szeged.inf.aramis.camera.process;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import hu.u_szeged.inf.aramis.camera.process.motion.PairMatcher;
import hu.u_szeged.inf.aramis.camera.process.motion.SimilarityDetector;
import hu.u_szeged.inf.aramis.model.Coordinate;

import static hu.u_szeged.inf.aramis.model.Coordinate.coordinate;

@RunWith(RobolectricTestRunner.class)
public class ClusterPairMatcherTest {
    private final Cluster<Coordinate> cluster1 = createCluster(Lists.newArrayList(coordinate(1, 1), coordinate(1, 2)));
    private final Cluster<Coordinate> cluster2 = createCluster(Lists.newArrayList(coordinate(3, 1), coordinate(2, 2), coordinate(5, 2)));
    private final Cluster<Coordinate> cluster3 = createCluster(Lists.newArrayList(coordinate(2, 1), coordinate(3, 2), coordinate(5, 2)));
    private final Table<Cluster<Coordinate>, Cluster<Coordinate>, Double> table = createTable();
    private SimilarityDetector mockDetector;
    private PairMatcher underTest;


    private Table<Cluster<Coordinate>, Cluster<Coordinate>, Double> createTable() {
        Table<Cluster<Coordinate>, Cluster<Coordinate>, Double> table = HashBasedTable.create();
        table.put(cluster1, cluster2, 0.4);
        table.put(cluster1, cluster3, 0.6);
        table.put(cluster2, cluster3, 0.7);
        return table;
    }

    private Cluster<Coordinate> createCluster(List<Coordinate> coordinates) {
        Cluster<Coordinate> cluster = new Cluster<Coordinate>();
        for (Coordinate coordinate : coordinates) {
            cluster.addPoint(coordinate);
        }
        return cluster;
    }
}
