package hu.u_szeged.inf.aramis.activities;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.ViewById;

import hu.u_szeged.inf.aramis.R;

@EActivity(R.layout.result_picture)
public class ResultActivity extends Activity {
    @ViewById(R.id.result)
    protected ImageView resultPicture;

    @Extra("bitmapPath")
    String bitmapPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    protected void setupResult() {
        resultPicture.setImageBitmap(BitmapFactory.decodeFile(bitmapPath));
    }
}
