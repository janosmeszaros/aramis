package hu.u_szeged.inf.aramis.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.RoboGuice;
import com.googlecode.androidannotations.annotations.ViewById;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import hu.u_szeged.inf.aramis.R;
import hu.u_szeged.inf.aramis.adapter.FullScreenImageAdapter;
import hu.u_szeged.inf.aramis.model.Pair;
import hu.u_szeged.inf.aramis.model.Picture;

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
        Map<Picture, List<Pair>> transformedMap = transformStringMapToPicture(resultBitmapPaths);
        FullScreenImageAdapter imageAdapter = new FullScreenImageAdapter(transformedMap, this);
        pager.setAdapter(imageAdapter);
    }
}