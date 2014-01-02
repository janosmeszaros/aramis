package hu.u_szeged.inf.aramis.activities;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.ViewById;

import org.apache.commons.math3.ml.clustering.Cluster;

import java.util.List;

import hu.u_szeged.inf.aramis.R;
import hu.u_szeged.inf.aramis.model.Coordinate;

@EActivity(R.layout.result_picture)
public class ResultActivity extends Activity {
    @ViewById(R.id.result)
    protected ImageView resultPicture;

    @Extra("bitmapPath")
    String bitmapPath;

    @Extra("clusters")
    List<Cluster<Coordinate>> clusters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    protected void setupResult() {
        resultPicture.setImageBitmap(BitmapFactory.decodeFile(bitmapPath));
    }
}
