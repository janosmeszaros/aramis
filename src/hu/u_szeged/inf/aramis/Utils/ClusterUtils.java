package hu.u_szeged.inf.aramis.Utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.apache.commons.math3.ml.clustering.Cluster;

import java.util.List;

import hu.u_szeged.inf.aramis.camera.picture.PictureSaver;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;
import hu.u_szeged.inf.aramis.model.Rectangle;

public class ClusterUtils {
    public Picture createBitmapFromClusters(Bitmap original, List<Cluster<Coordinate>> clusters) {
        Bitmap result = original.copy(original.getConfig(), true);
        int count = 0;
        for (Cluster<Coordinate> cluster : clusters) {
            for (Coordinate coordinate : cluster.getPoints()) {
                result.setPixel(coordinate.x, coordinate.y, Color.rgb(count * 10, count * 10, 0));
            }
            count++;
        }
        Picture picture = Picture.picture("clusters_" + clusters.size(), result);
        PictureSaver.save(picture);
        return picture;
    }


    public static Rectangle findBoundingBox(List<Coordinate> coordinates) {
        int minX = 1000;
        int minY = 1000;
        int maxX = 0;
        int maxY = 0;

        for (Coordinate coordinate : coordinates) {
            if (coordinate.x < minX) {
                minX = coordinate.x;
            }
            if (coordinate.x > maxX) {
                maxX = coordinate.x;
            }
            if (coordinate.y < minY) {
                minY = coordinate.y;
            }
            if (coordinate.y > maxY) {
                maxY = coordinate.y;
            }
        }
        return Rectangle.rectangle(minX, minY, maxX, maxY);
    }
}