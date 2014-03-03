package hu.u_szeged.inf.aramis.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.google.common.collect.Lists;
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
        List<Bitmap> bitmaps = markChains(motionSerieses);
        for (Bitmap bitmap : bitmaps) {
            PictureSaver.save(Picture.picture("bitmap" + bitmaps.indexOf(bitmap), bitmap));
        }
        FullScreenImageAdapter imageAdapter = new FullScreenImageAdapter(bitmaps, this);
        pager.setAdapter(imageAdapter);
    }

    private List<Bitmap> markChains(List<MotionSeries> motionSeriesList) {
        List<Bitmap> bitmaps = Lists.newArrayList();
        for (MotionSeries motionSeries : motionSeriesList) {
            for (Map.Entry<Picture, Cluster<Coordinate>> entry : motionSeries.getMap().entrySet()) {
                Bitmap originalBitmap = entry.getKey().bitmap;
                Bitmap copy = originalBitmap.copy(originalBitmap.getConfig(), true);
                for (Coordinate coordinate : entry.getValue().getPoints()) {
                    copy.setPixel(coordinate.x, coordinate.y, motionSeries.getColor());
                }
                bitmaps.add(copy);
            }
        }
        return bitmaps;
    }

    private List<MotionSeries> spotChains(Map<Picture, List<Pair>> map) {
        boolean isFirst = true;
        List<MotionSeries> motionSeriesList = Lists.newArrayList();
        for (Map.Entry<Picture, List<Pair>> entry : map.entrySet()) {
            for (Pair pair : entry.getValue()) {
                if (isFirst) {
                    motionSeriesList.add(new MotionSeries(randomColor(), pair, entry.getKey()));
                    isFirst = false;
                } else {
                    for (MotionSeries motionSeries : motionSeriesList) {
                        if (motionSeries.putValue(entry.getKey(), pair)) {
                            LOGGER.info("Putting to series for picture {}", entry.getKey());
                            break;
                        }
                    }
                    motionSeriesList.add(new MotionSeries(randomColor(), pair, entry.getKey()));
                }
            }
        }
        return motionSeriesList;
    }

    private int randomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }
}