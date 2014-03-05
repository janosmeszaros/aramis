package hu.u_szeged.inf.aramis.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import java.util.Random;
import java.util.Set;

import hu.u_szeged.inf.aramis.R;
import hu.u_szeged.inf.aramis.adapter.FullScreenImageAdapter;
import hu.u_szeged.inf.aramis.camera.picture.PictureSaver;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.MotionSeries;
import hu.u_szeged.inf.aramis.model.Pair;
import hu.u_szeged.inf.aramis.model.Picture;

import static hu.u_szeged.inf.aramis.Utils.MapUtils.sortMapWithPicture;
import static hu.u_szeged.inf.aramis.Utils.MapUtils.transformStringMapToPicture;

@EActivity(R.layout.difference_pictures)
@RoboGuice
public class DifferencePicturesActivity extends Activity {
    private static final Logger LOGGER = LoggerFactory.getLogger(DifferencePicturesActivity.class);
    @ViewById
    ViewPager pager;
    @Extra("resultBitmapPaths")
    Map<String, List<Pair>> resultBitmapPaths;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    protected void setupResult() {
        Map<Picture, List<Pair>> transformedMap = sortMapWithPicture(transformStringMapToPicture(resultBitmapPaths));
        List<MotionSeries> motionSerieses = spotChains(transformedMap);
        List<Bitmap> bitmaps = markChains(transformedMap.keySet(), motionSerieses);
        for (Bitmap bitmap : bitmaps) {
            PictureSaver.save(Picture.picture("bitmap" + bitmaps.indexOf(bitmap), bitmap));
        }
        FullScreenImageAdapter imageAdapter = new FullScreenImageAdapter(bitmaps, this);
        pager.setAdapter(imageAdapter);
    }

    private List<Bitmap> markChains(Set<Picture> pictures, List<MotionSeries> motionSeriesList) {
        Map<Picture, Bitmap> result = Maps.newHashMap(Maps.asMap(pictures, new Function<Picture, Bitmap>() {
            @Override
            public Bitmap apply(Picture input) {
                return input.bitmap.copy(input.bitmap.getConfig(), true);
            }
        }));
        for (MotionSeries motionSeries : motionSeriesList) {
            for (Map.Entry<Picture, Cluster<Coordinate>> entry : motionSeries.getMap().entrySet()) {
                Picture key = entry.getKey();
                LOGGER.info("Coloring coordinates to {} for picture {}", motionSeries.getColor(), key.name);
                result.put(key, setPixels(motionSeries.getColor(),
                        result.get(key), entry.getValue().getPoints()));
            }
        }
        Map<Picture, Bitmap> sortedResult = sortMapWithPicture(result);
        return Lists.newArrayList(sortedResult.values());
    }

    private Bitmap setPixels(int color, Bitmap bitmap, List<Coordinate> coordinates) {
        Bitmap result = bitmap.copy(bitmap.getConfig(), true);
        for (Coordinate coordinate : coordinates) {
            result.setPixel(coordinate.x, coordinate.y, color);
        }
        return result;
    }

    private List<MotionSeries> spotChains(Map<Picture, List<Pair>> map) {
        List<MotionSeries> motionSeriesList = Lists.newArrayList();
        for (Map.Entry<Picture, List<Pair>> entry : map.entrySet()) {
            LOGGER.info("Processing pairs for picture #{}", entry.getKey().name);
            List<MotionSeries> motionSeriesListForActualPicture = Lists.newArrayList();
            for (Pair pair : entry.getValue()) {
                boolean isPutted = false;
                for (MotionSeries motionSeries : motionSeriesList) {
                    if (motionSeries.putValue(entry.getKey(), pair)) {
                        isPutted = true;
                        LOGGER.info("Putting to series for picture {} to {}", entry.getKey().name, motionSeries.getColor());
                        break;
                    }
                }
                if (!isPutted) {
                    motionSeriesListForActualPicture.add(new MotionSeries(randomColor(), pair, entry.getKey()));
                }
            }
            motionSeriesList.addAll(motionSeriesListForActualPicture);
        }
        return motionSeriesList;
    }

    private int randomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }
}