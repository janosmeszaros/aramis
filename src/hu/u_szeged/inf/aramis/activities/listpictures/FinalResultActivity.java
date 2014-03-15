package hu.u_szeged.inf.aramis.activities.listpictures;

import android.app.Activity;
import android.support.v4.view.ViewPager;

import com.google.common.collect.ImmutableList;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.List;

import hu.u_szeged.inf.aramis.R;

@EActivity(R.layout.picture_swiper)
public class FinalResultActivity extends Activity {
    @ViewById
    ViewPager pager;
    @Extra("backgroundPicturePath")
    List<File> files;
    private FinalResultAdapter finalResultAdapter;

    @AfterViews
    void bindAdapter() {
        finalResultAdapter = new FinalResultAdapter(ImmutableList.copyOf(files), this);
        pager.setAdapter(finalResultAdapter);
    }


}
