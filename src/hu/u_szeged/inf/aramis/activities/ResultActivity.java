package hu.u_szeged.inf.aramis.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.inject.Inject;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.RoboGuice;
import com.googlecode.androidannotations.annotations.Touch;
import com.googlecode.androidannotations.annotations.ViewById;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import hu.u_szeged.inf.aramis.R;
import hu.u_szeged.inf.aramis.camera.PictureEvaluator;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;

@EActivity(R.layout.result_picture)
@RoboGuice
public class ResultActivity extends Activity {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultActivity.class);
    @ViewById(R.id.image)
    protected ImageView result;
    @Extra("resultBitmapPath")
    String resultBitmapPath;
    @Extra("clusterBitmapPath")
    String clusterBitmapPath;
    @Extra("clusters")
    List<Cluster<Coordinate>> clusters;
    @Inject
    PictureEvaluator evaluator;
    private Table<Integer, Integer, Cluster<Coordinate>> clusterTable;
    private Picture resultPicture;
    private Picture clusterPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    protected void setupResult() {
        Bitmap clusterBitmap = BitmapFactory.decodeFile(clusterBitmapPath);
        resultPicture = Picture.picture("result", BitmapFactory.decodeFile(resultBitmapPath));
        clusterPicture = Picture.picture("cluster", clusterBitmap);
        result.setImageBitmap(clusterBitmap);
        setAreas();
    }

    @Background
    protected void setAreas() {
        clusterTable = HashBasedTable.create();
        for (Cluster<Coordinate> cluster : clusters) {
            for (Coordinate coordinate : cluster.getPoints()) {
                clusterTable.put(coordinate.x, coordinate.y, cluster);
            }
        }
        LOGGER.info("Area size: {}!", clusterTable.size());
    }

    @Touch(R.id.image)
    protected void onImageTouch(MotionEvent ev, View v) {
        final int action = ev.getAction();
        final int evX = (int) ev.getX();
        final int evY = (int) ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                LOGGER.info("Touch happened on x:{} y:{}", evX, evY);
                Cluster<Coordinate> cluster = clusterTable.get(evX, evY);
                if (cluster != null) {
                    Bitmap evaluated = evaluator.switchColors(clusterPicture, resultPicture, cluster.getPoints());
                    clusterPicture = Picture.picture("cluster", evaluated);
                    result.setImageBitmap(evaluated);
                }
                break;
        }
    }
}
