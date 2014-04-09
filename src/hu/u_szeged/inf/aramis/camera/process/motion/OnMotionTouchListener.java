package hu.u_szeged.inf.aramis.camera.process.motion;

import android.graphics.Bitmap;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Table;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import hu.u_szeged.inf.aramis.adapter.FullScreenImageAdapter;
import hu.u_szeged.inf.aramis.camera.process.display.BitmapRefresher;
import hu.u_szeged.inf.aramis.camera.process.display.ChainDetector;
import hu.u_szeged.inf.aramis.camera.process.display.ChainResolver;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.MotionSeries;
import hu.u_szeged.inf.aramis.model.Picture;

public class OnMotionTouchListener implements View.OnTouchListener {
    private final static int TOLERANCE = 50;
    private static final Logger LOGGER = LoggerFactory.getLogger(OnMotionTouchListener.class);
    private final BitmapRefresher refresher;
    private final ChainResolver chainResolver;
    private final Map<Picture, Bitmap> pictures;
    private final FullScreenImageAdapter fullScreenImageAdapter;
    private final ViewPager pager;
    private final ChainDetector chainDetector;
    private final ImmutableSortedMap<Picture, Table<Integer, Integer, Cluster<Coordinate>>> areas;
    private int pointX;
    private int pointY;

    public OnMotionTouchListener(BitmapRefresher refresher,
                                 ChainResolver chainResolver,
                                 Map<Picture, Bitmap> pictures,
                                 FullScreenImageAdapter fullScreenImageAdapter,
                                 ViewPager pager,
                                 ChainDetector chainDetector,
                                 ImmutableSortedMap<Picture, Table<Integer, Integer, Cluster<Coordinate>>> areas) {
        this.refresher = refresher;
        this.chainResolver = chainResolver;
        this.pictures = pictures;
        this.fullScreenImageAdapter = fullScreenImageAdapter;
        this.pager = pager;
        this.chainDetector = chainDetector;
        this.areas = areas;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                return false;
            case MotionEvent.ACTION_DOWN:
                pointX = (int) event.getX();
                pointY = (int) event.getY();
                break;
            case MotionEvent.ACTION_UP:
                boolean sameX = pointX + TOLERANCE > event.getX() && pointX - TOLERANCE < event.getX();
                boolean sameY = pointY + TOLERANCE > event.getY() && pointY - TOLERANCE < event.getY();
                if (sameX && sameY) {
                    LOGGER.info("Touch happened on x:{} y:{}", pointX, pointY);
                    Picture actualPicture = getElementAt(pager.getCurrentItem());
                    Table<Integer, Integer, Cluster<Coordinate>> table = areas.get(actualPicture);
                    if (table.contains(pointX, pointY)) {
                        Cluster<Coordinate> cluster = table.get(pointX, pointY);
                        MotionSeries series = chainResolver.findChainFor(actualPicture, cluster);
                        pictures.putAll(refresher.refreshBitmaps(pictures, series.getMap()));
                        removeClusterFromTouchableAreas(series);
                        List<MotionSeries> seriesList = chainResolver.remove(series);
                        pictures.putAll(chainDetector.markChains(pictures, seriesList));
                        fullScreenImageAdapter.setPictures(pictures);
                        fullScreenImageAdapter.notifyDataSetChanged();
                    }
                }
        }
        return false;
    }

    private void removeClusterFromTouchableAreas(MotionSeries series) {
        for (Map.Entry<Picture, Cluster<Coordinate>> entry : series.getMap().entrySet()) {
            Table<Integer, Integer, Cluster<Coordinate>> clusterTable = areas.get(entry.getKey());
            for (Coordinate coordinate : entry.getValue().getPoints()) {
                clusterTable.remove(coordinate.x, coordinate.y);
            }
        }
    }

    private Picture getElementAt(int position) {
        int i = 0;
        for (Map.Entry<Picture, Bitmap> entry : pictures.entrySet()) {
            if (i == position) {
                return entry.getKey();
            }
            i++;
        }
        throw new IllegalArgumentException(String.format("Cant find %d th element in the map", position));
    }
}