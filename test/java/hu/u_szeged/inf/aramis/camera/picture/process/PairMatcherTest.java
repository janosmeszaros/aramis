package hu.u_szeged.inf.aramis.camera.picture.process;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Pair;

import static hu.u_szeged.inf.aramis.model.Coordinate.coordinate;
import static hu.u_szeged.inf.aramis.model.Pair.pair;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PairMatcherTest {
    private final Cluster<Coordinate> cluster1 = createCluster(Lists.newArrayList(coordinate(1, 1), coordinate(1, 2)));
    private final Cluster<Coordinate> cluster2 = createCluster(Lists.newArrayList(coordinate(3, 1), coordinate(2, 2), coordinate(5, 2)));
    private final Cluster<Coordinate> cluster3 = createCluster(Lists.newArrayList(coordinate(2, 1), coordinate(3, 2), coordinate(5, 2)));
    private final Table<Cluster<Coordinate>, Cluster<Coordinate>, Double> table = createTable();
    private SimilarityDetector mockDetector;
    private PairMatcher underTest;

    @Before
    public void setup() {
        mockDetector = mock(SimilarityDetector.class);
        underTest = new PairMatcher(mockDetector);
    }

    @Test
    public void testFindSimilarPairsWhenOneClusterSimilar() {
        when(mockDetector.isSimilar(eq(cluster1), eq(cluster2), anyDouble())).thenReturn(true);
        when(mockDetector.isSimilar(eq(cluster1), eq(cluster3), anyDouble())).thenReturn(false);
        when(mockDetector.isSimilar(eq(cluster2), eq(cluster3), anyDouble())).thenReturn(false);

        List<Pair> similarPairs = underTest.findSimilarPairs(table);

        assertThat(similarPairs.size(), is(1));
        assertThat(similarPairs, allOf(
                hasItem(pair(cluster1, cluster2))));
    }

    @Test
    public void testFindSimilarPairsWhenAllOfThemSimilar() {
        when(mockDetector.isSimilar(any(Cluster.class), any(Cluster.class), anyDouble())).thenReturn(true);

        List<Pair> similarPairs = underTest.findSimilarPairs(table);

        assertThat(similarPairs.size(), is(2));
        assertThat(similarPairs, allOf(
                hasItem(pair(cluster1, cluster3)),
                hasItem(pair(cluster2, cluster3))));
    }

    @Test
    public void testFindSimilarPairsWhenSecondIsSimilar() {
        when(mockDetector.isSimilar(eq(cluster1), eq(cluster2), anyDouble())).thenReturn(false);
        when(mockDetector.isSimilar(eq(cluster1), eq(cluster3), anyDouble())).thenReturn(true);
        when(mockDetector.isSimilar(eq(cluster2), eq(cluster3), anyDouble())).thenReturn(false);

        List<Pair> similarPairs = underTest.findSimilarPairs(table);

        assertThat(similarPairs.size(), is(1));
        assertThat(similarPairs, allOf(
                hasItem(pair(cluster1, cluster3))));
    }

    @Test
    public void testFindSimilarPairsWhenTwoIsSimilar() {
        when(mockDetector.isSimilar(eq(cluster1), eq(cluster2), anyDouble())).thenReturn(false);
        when(mockDetector.isSimilar(eq(cluster1), eq(cluster3), anyDouble())).thenReturn(true);
        when(mockDetector.isSimilar(eq(cluster2), eq(cluster3), anyDouble())).thenReturn(true);

        List<Pair> similarPairs = underTest.findSimilarPairs(table);

        assertThat(similarPairs.size(), is(2));
        assertThat(similarPairs, allOf(
                hasItem(pair(cluster1, cluster3)),
                hasItem(pair(cluster2, cluster3))));
    }

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
