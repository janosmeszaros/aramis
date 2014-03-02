package hu.u_szeged.inf.aramis.camera.picture.process;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import hu.u_szeged.inf.aramis.model.Coordinate;

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
        underTest = new SimilarityDetector(2.0, 0.8);
    }

    @Test
    public void testIsSimilarWhenMomentsTrueDistanceTrue() {
        boolean similar = underTest.isSimilar(cluster1, cluster2, 0.9);

        assertThat(similar, equalTo(true));
    }

    @Test
    public void testIsSimilarWhenMomentsTrueDistanceFalse() {
        boolean similar = underTest.isSimilar(cluster1, cluster3, 0.9);

        assertThat(similar, equalTo(false));
    }

    @Test
    public void testIsSimilarWhenMomentsFalseDistanceFalse() {
        boolean similar = underTest.isSimilar(cluster1, cluster3, 0.8);

        assertThat(similar, equalTo(false));
    }

    @Test
    public void testIsSimilarWhenMomentsFalseDistanceTrue() {
        boolean similar = underTest.isSimilar(cluster1, cluster2, 0.8);

        assertThat(similar, equalTo(false));
    }

    private Cluster<Coordinate> createCluster(int i) {
        Cluster<Coordinate> cluster = new Cluster<Coordinate>();
        cluster.addPoint(Coordinate.coordinate(i, i));
        cluster.addPoint(Coordinate.coordinate(i + 1, i + 1));
        cluster.addPoint(Coordinate.coordinate(i + 1, i));
        cluster.addPoint(Coordinate.coordinate(i, i + 1));
        return cluster;
    }
}
