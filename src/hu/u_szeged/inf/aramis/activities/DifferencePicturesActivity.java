package hu.u_szeged.inf.aramis.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.RoboGuice;
import com.googlecode.androidannotations.annotations.ViewById;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import hu.u_szeged.inf.aramis.R;
import hu.u_szeged.inf.aramis.adapter.FullScreenImageAdapter;
import hu.u_szeged.inf.aramis.camera.process.PictureEvaluator;
import hu.u_szeged.inf.aramis.camera.process.display.BitmapRefresher;
import hu.u_szeged.inf.aramis.camera.process.display.ChainDetector;
import hu.u_szeged.inf.aramis.camera.process.display.ChainResolver;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.MotionSeries;
import hu.u_szeged.inf.aramis.model.Pair;
import hu.u_szeged.inf.aramis.model.Picture;
import hu.u_szeged.inf.aramis.utils.ClusterUtils;

import static hu.u_szeged.inf.aramis.model.Picture.picture;
import static hu.u_szeged.inf.aramis.utils.MapUtils.sortMapWithPicture;
import static hu.u_szeged.inf.aramis.utils.MapUtils.transformStringMapToPicture;

@EActivity(R.layout.difference_pictures)
@RoboGuice
public class DifferencePicturesActivity extends Activity {
    private static final Logger LOGGER = LoggerFactory.getLogger(DifferencePicturesActivity.class);
    @ViewById
    ViewPager pager;
    @Extra("resultBitmapPaths")
    Map<String, List<Pair>> resultBitmapPaths;
    @Extra("backgroundPicturePath")
    String backgroundPicturePath;
    @Inject
    PictureEvaluator evaluator;
    @Inject
    ChainDetector chainDetector;
    private FullScreenImageAdapter fullScreenImageAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    protected void setupResult() {
        Map<Picture, List<Pair>> transformedMap = sortMapWithPicture(transformStringMapToPicture(resultBitmapPaths));
        List<MotionSeries> motionSeriesList = chainDetector.spotChains(transformedMap);
        Map<Picture, Bitmap> bitmaps = chainDetector.markChains(transformedMap.keySet(), motionSeriesList);
        Map<Picture, Bitmap> sortedBitmaps = sortMapWithPicture(bitmaps);
        ImmutableSortedMap<Picture, Table<Integer, Integer, Cluster<Coordinate>>> areas = createAreas(transformedMap, sortedBitmaps);


        BitmapRefresher refresher = new BitmapRefresher(evaluator, picture("result", BitmapFactory.decodeFile(backgroundPicturePath)));
        ChainResolver chainResolver = new ChainResolver(motionSeriesList);
        fullScreenImageAdapter = new FullScreenImageAdapter(sortedBitmaps, this);
        TouchListener touchListener = new TouchListener(refresher, chainResolver, sortedBitmaps, areas);
        pager.setAdapter(fullScreenImageAdapter);
        pager.setOnTouchListener(touchListener);
    }

    private ImmutableSortedMap<Picture, Table<Integer, Integer, Cluster<Coordinate>>> createAreas(
            Map<Picture, List<Pair>> original,
            Map<Picture, Bitmap> marked) {
        ImmutableSortedMap.Builder<Picture, Table<Integer, Integer, Cluster<Coordinate>>> builder =
                ImmutableSortedMap.<Picture, Table<Integer, Integer, Cluster<Coordinate>>>naturalOrder();
        for (Map.Entry<Picture, Bitmap> entry : marked.entrySet()) {
            builder.put(entry.getKey(), processListOfPairs(original.get(entry.getKey())));
        }
        return builder.build();
    }

    private Table<Integer, Integer, Cluster<Coordinate>> processListOfPairs(List<Pair> pairs) {
        Table<Integer, Integer, Cluster<Coordinate>> table = HashBasedTable.create();
        for (Pair pair : pairs) {
            for (Coordinate coordinate : pair.first.getPoints()) {
                table.put(coordinate.x, coordinate.y, pair.first);
            }
            LOGGER.info("table size: {} for cluster {}", table.size(), ClusterUtils.findBoundingBox(pair.first.getPoints()));
        }
        return table;
    }

    private class TouchListener implements View.OnTouchListener {
        private final static int TOLERANCE = 50;

        private final BitmapRefresher refresher;
        private final ChainResolver chainResolver;
        private final Map<Picture, Bitmap> pictures;
        private final ImmutableSortedMap<Picture, Table<Integer, Integer, Cluster<Coordinate>>> areas;

        private int pointX;
        private int pointY;

        private TouchListener(BitmapRefresher refresher,
                              ChainResolver chainResolver,
                              Map<Picture, Bitmap> pictures,
                              ImmutableSortedMap<Picture, Table<Integer, Integer, Cluster<Coordinate>>> areas) {
            this.refresher = refresher;
            this.chainResolver = chainResolver;
            this.pictures = pictures;
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
                        LOGGER.info("Got Table size for {} : {}", actualPicture.name, table.size());
                        if (table.contains(pointX, pointY)) {
                            Cluster<Coordinate> cluster = table.get(pointX, pointY);
                            Map<Picture, Cluster<Coordinate>> map = chainResolver.findChainFor(actualPicture, cluster);
                            pictures.putAll(refresher.refreshBitmaps(map));
                            fullScreenImageAdapter.setPictures(pictures);
                            fullScreenImageAdapter.notifyDataSetChanged();
                        }
                    }
            }
            return false;
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

}