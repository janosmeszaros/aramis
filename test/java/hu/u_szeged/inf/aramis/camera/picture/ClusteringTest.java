package hu.u_szeged.inf.aramis.camera.picture;


import com.google.common.base.Stopwatch;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import hu.u_szeged.inf.aramis.model.Coordinate;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ClusteringTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusteringTest.class);
    private Clustering underTest;

    @Before
    public void setup() {
        underTest = Clustering.clustering(1, 1);
    }

    @Test
    public void cluster() throws Exception {
        Table<Integer, Integer, Boolean> table = HashBasedTable.create();
        table.put(1, 1, false);
        table.put(1, 2, false);
        table.put(4, 4, false);
        table.put(4, 5, false);

        List<Cluster<Coordinate>> cluster = underTest.cluster(table);

        assertThat(cluster.size(), equalTo(2));
    }

    @Test
    public void performanceTest() throws Exception {
        Table<Integer, Integer, Boolean> table = HashBasedTable.create();
        for (int i = 0; i < 500; i++) {
            for (int j = 0; j < 100; j++) {
                table.put(i, j, false);
            }
        }

        System.out.println("Starting!");
        Stopwatch started = Stopwatch.createStarted();
        List<Cluster<Coordinate>> cluster = underTest.cluster(table);
        started.stop();

        System.out.println("finished in " + started.elapsed(TimeUnit.MILLISECONDS));
        assertThat(cluster.size(), equalTo(1));

    }
}
