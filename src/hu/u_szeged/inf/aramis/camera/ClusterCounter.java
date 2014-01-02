package hu.u_szeged.inf.aramis.camera;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Table;
import com.google.inject.Inject;

import org.apache.commons.math3.ml.clustering.Cluster;

import java.util.List;
import java.util.concurrent.TimeUnit;

import hu.u_szeged.inf.aramis.camera.picture.Clustering;
import hu.u_szeged.inf.aramis.camera.picture.PictureSaver;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;

public class ClusterCounter {
    private Clustering clustering;

    @Inject
    public ClusterCounter(Clustering clustering) {
        this.clustering = clustering;
    }

    public List<Cluster<Coordinate>> clusterize(Bitmap second, Table<Integer, Integer, Boolean> table) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Cluster<Coordinate>> clusters = Clustering.clustering(1, 2).cluster(table);
        stopwatch.stop();
        Bitmap result = second.copy(second.getConfig(), true);
        int count = 0;
        for (Cluster<Coordinate> cluster : clusters) {
            for (Coordinate coordinate : cluster.getPoints()) {
                result.setPixel(coordinate.x, coordinate.y, Color.rgb(count * 10, count * 10, 0));
            }
            count++;
        }
        PictureSaver.save(Picture.picture(stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms_clusters_" + clusters.size(), result));
        return clusters;
    }
}