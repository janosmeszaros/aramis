package hu.u_szeged.inf.aramis.camera.process;

import com.google.common.base.Optional;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import hu.u_szeged.inf.aramis.camera.process.motion.SimilarityDetector;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.SimilarityVector;

import static hu.u_szeged.inf.aramis.model.Coordinate.coordinate;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class SimilarityDetectorTest {
    private Cluster<Coordinate> cluster1 = createCluster(0);
    private Cluster<Coordinate> cluster2 = createCluster(1);
    private Cluster<Coordinate> cluster3 = createCluster(4);
    private SimilarityDetector underTest;

    @Before
    public void setUp() throws Exception {
        underTest = new SimilarityDetector(2.0, 0.8, 2.0);
    }

    @Test
    public void testIsSimilarWhenMomentsTrueDistanceTrue() {
        Optional<SimilarityVector> similar = underTest.countSimilarity(cluster1, cluster2, 0.7);

        assertThat(similar.isPresent(), equalTo(true));
    }

    @Test
    public void testIsSimilarWhenMomentsTrueDistanceFalse() {
        Optional<SimilarityVector> similar = underTest.countSimilarity(cluster1, cluster3, 0.7);

        assertThat(similar.isPresent(), equalTo(false));
    }

    @Test
    public void testIsSimilarWhenMomentsFalseDistanceFalse() {
        Optional<SimilarityVector> similar = underTest.countSimilarity(cluster1, cluster3, 0.8);

        assertThat(similar.isPresent(), equalTo(false));
    }

    @Test
    public void testIsSimilarWhenMomentsFalseDistanceTrue() {
        Optional<SimilarityVector> similar = underTest.countSimilarity(cluster1, cluster2, 0.9);

        assertThat(similar.isPresent(), equalTo(false));
    }

    @Test
    public void testIsSimilarWhenMomentsTrueDistanceTrueAreaDiffTrue() {
        Optional<SimilarityVector> similar = underTest.countSimilarity(cluster1, cluster2, 0.7);

        assertThat(similar.isPresent(), equalTo(true));
    }

    @Test
    public void testIsSimilarWhenMomentsTrueDistanceTrueAreaDiffFalse() {
        cluster1.addPoint(coordinate(2, 2));

        Optional<SimilarityVector> similar = underTest.countSimilarity(cluster1, cluster2, 0.9);

        assertThat(similar.isPresent(), equalTo(false));
    }

    private Cluster<Coordinate> createCluster(int i) {
        Cluster<Coordinate> cluster = new Cluster<Coordinate>();
        cluster.addPoint(coordinate(i, i));
        cluster.addPoint(coordinate(i + 1, i + 1));
        cluster.addPoint(coordinate(i + 1, i));
        cluster.addPoint(coordinate(i, i + 1));
        return cluster;
    }
}
