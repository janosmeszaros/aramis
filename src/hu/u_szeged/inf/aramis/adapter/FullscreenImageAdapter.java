package hu.u_szeged.inf.aramis.adapter;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import hu.u_szeged.inf.aramis.R;
import hu.u_szeged.inf.aramis.camera.utils.PictureSaver;
import hu.u_szeged.inf.aramis.model.Picture;

import static hu.u_szeged.inf.aramis.model.Picture.picture;

public class FullScreenImageAdapter extends PagerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(FullScreenImageAdapter.class);

    private Map<Picture, Bitmap> pictures;

    private final Activity activity;
    private ImageView imgDisplay;

    private Button btnClose;
    private Button btnSave;

    public FullScreenImageAdapter(Map<Picture, Bitmap> pictures,
                                  Activity activity) {
        this.pictures = pictures;
        this.activity = activity;
    }


    @Override
    public int getCount() {
        return this.pictures.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.difference_image, container,
                false);

        imgDisplay = (ImageView) viewLayout.findViewById(R.id.imgDisplay);
        btnClose = (Button) viewLayout.findViewById(R.id.btnClose);
        btnSave = (Button) viewLayout.findViewById(R.id.btnSave);

        Picture actualPicture = getElementAt(position);
        imgDisplay.setImageBitmap(pictures.get(actualPicture));

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
                PictureSaver.newAlbum();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePictures();
                activity.finish();
                PictureSaver.newAlbum();
            }
        });
        ((ViewPager) container).addView(viewLayout);

        return viewLayout;
    }

    private void savePictures() {
        int count = 0;
        for (Bitmap bitmap : pictures.values()) {
            PictureSaver.save(picture("finalResult" + ++count, bitmap));
        }
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

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);

    }

    public void setPictures(Map<Picture, Bitmap> pictures) {
        this.pictures = pictures;
    }
}