package hu.u_szeged.inf.aramis.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.Display;

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
import hu.u_szeged.inf.aramis.camera.process.motion.OnMotionTouchListener;
import hu.u_szeged.inf.aramis.model.ClusterPair;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.MotionSeries;
import hu.u_szeged.inf.aramis.model.Picture;

import static hu.u_szeged.inf.aramis.model.Picture.picture;
import static hu.u_szeged.inf.aramis.utils.MapUtils.sortMapWithPicture;
import static hu.u_szeged.inf.aramis.utils.MapUtils.transformStringMapToPicture;

@EActivity(R.layout.picture_swiper)
@RoboGuice
public class DifferencePicturesActivity extends Activity {
    private static final Logger LOGGER = LoggerFactory.getLogger(DifferencePicturesActivity.class);
    @ViewById(R.id.pager)
    ViewPager pager;
    @Extra("resultBitmapPaths")
    Map<String, List<ClusterPair>> resultBitmapPaths;
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
        LOGGER.info("Starting");
        Map<Picture, List<ClusterPair>> transformedMap = sortMapWithPicture(transformStringMapToPicture(resultBitmapPaths));
        Picture background = picture("background", BitmapFactory.decodeFile(backgroundPicturePath));

        LOGGER.info("Spotting chains!");
        List<MotionSeries> motionSeriesList = chainDetector.spotChains(transformedMap);
        LOGGER.info("Marking chains!");
        Map<Picture, Bitmap> bitmaps = chainDetector.markChains(transformedMap.keySet(), motionSeriesList);
        Map<Picture, Bitmap> sortedBitmaps = sortMapWithPicture(bitmaps);
        LOGGER.info("Create areas");
        ImmutableSortedMap<Picture, Table<Integer, Integer, Cluster<Coordinate>>> areas =
                createAreas(transformedMap, sortedBitmaps, background.bitmap);

        BitmapRefresher refresher = new BitmapRefresher(evaluator,
                background);
        ChainResolver chainResolver = new ChainResolver(motionSeriesList);
        fullScreenImageAdapter = new FullScreenImageAdapter(sortedBitmaps, this);
        LOGGER.info("Create on touch listener");
        OnMotionTouchListener touchListener = new OnMotionTouchListener(refresher,
                chainResolver, sortedBitmaps, fullScreenImageAdapter, pager, chainDetector, areas);
        pager.setAdapter(fullScreenImageAdapter);
        pager.setOnTouchListener(touchListener);
    }

    private ImmutableSortedMap<Picture, Table<Integer, Integer, Cluster<Coordinate>>> createAreas(
            Map<Picture, List<ClusterPair>> original,
            Map<Picture, Bitmap> marked,
            Bitmap bitmap) {
        ImmutableSortedMap.Builder<Picture, Table<Integer, Integer, Cluster<Coordinate>>> builder =
                ImmutableSortedMap.<Picture, Table<Integer, Integer, Cluster<Coordinate>>>naturalOrder();
        Pair<Double, Double> scalePair = countScale(bitmap);
        for (Map.Entry<Picture, Bitmap> entry : marked.entrySet()) {
            builder.put(entry.getKey(), processListOfPairs(original.get(entry.getKey()), scalePair));
        }
        return builder.build();
    }

    private Table<Integer, Integer, Cluster<Coordinate>> processListOfPairs(List<ClusterPair> clusterPairs,
                                                                            Pair<Double, Double> scalePair) {
        Table<Integer, Integer, Cluster<Coordinate>> table = HashBasedTable.create();
        for (ClusterPair clusterPair : clusterPairs) {
            for (Coordinate coordinate : clusterPair.first.getPoints()) {
                int firstCoordinate = (int) (coordinate.x * scalePair.first);
                int secondCoordinate = (int) (coordinate.y * scalePair.second);
                table.put(firstCoordinate, secondCoordinate, clusterPair.first);
            }
        }
        return table;
    }

    private Pair<Double, Double> countScale(Bitmap bitmap) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return new Pair<Double, Double>(size.x / (double) bitmap.getWidth(), size.y / (double) bitmap.getHeight());
    }
}