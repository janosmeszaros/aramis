package hu.u_szeged.inf.aramis.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.google.common.collect.Maps;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.RoboGuice;
import com.googlecode.androidannotations.annotations.ViewById;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

import hu.u_szeged.inf.aramis.R;
import hu.u_szeged.inf.aramis.adapter.FullScreenImageAdapter;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;

import static hu.u_szeged.inf.aramis.model.Picture.picture;

@EActivity(R.layout.difference_pictures)
@RoboGuice
public class DifferencePicturesActivity extends Activity {
    private static final Logger LOGGER = LoggerFactory.getLogger(DifferencePicturesActivity.class);
    @ViewById
    ViewPager pager;

    @Extra("resultBitmapPaths")
    Map<String, Set<Coordinate>> resultBitmapPaths;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    protected void setupResult() {
        Map<Picture, Set<Coordinate>> transformedMap = transformMap();
        FullScreenImageAdapter imageAdapter = new FullScreenImageAdapter(transformedMap, this);
        pager.setAdapter(imageAdapter);
    }

    private Map<Picture, Set<Coordinate>> transformMap() {
        Map<Picture, Set<Coordinate>> transformed = Maps.newLinkedHashMap();
        int counter = 0;
        for (Map.Entry<String, Set<Coordinate>> entry : resultBitmapPaths.entrySet()) {
            LOGGER.info("Get picture from {}", entry.getKey());
            Picture key = picture(String.valueOf(counter++), createPicture(entry.getKey(), entry.getValue()));
            transformed.put(key, entry.getValue());
        }
        return transformed;
    }

    private Bitmap createPicture(String path, Set<Coordinate> diffs) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        for (Coordinate diff : diffs) {
            bitmap.setPixel(diff.x, diff.y, Color.BLUE);
        }
        return bitmap;
    }
}