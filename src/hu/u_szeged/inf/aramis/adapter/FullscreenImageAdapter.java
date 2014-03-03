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

import java.util.List;
import java.util.Map;

import hu.u_szeged.inf.aramis.R;
import hu.u_szeged.inf.aramis.model.Pair;
import hu.u_szeged.inf.aramis.model.Picture;

public class FullScreenImageAdapter extends PagerAdapter {
    private List<Bitmap> pictures;
    private Activity activity;

    public FullScreenImageAdapter(List<Bitmap> pictures, Activity activity) {
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
        ImageView imgDisplay;
        Button btnClose;

        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.difference_image, container,
                false);

        imgDisplay = (ImageView) viewLayout.findViewById(R.id.imgDisplay);
        btnClose = (Button) viewLayout.findViewById(R.id.btnClose);

        imgDisplay.setImageBitmap(pictures.get(position));

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
        ((ViewPager) container).addView(viewLayout);

        return viewLayout;
    }

    private Bitmap getElementAt(Map<Picture, List<Pair>> pictures, int position) {
        int i = 0;
        for (Picture picture : pictures.keySet()) {
            if (i == position) {
                return picture.bitmap;
            }
            i++;
        }
        throw new IllegalArgumentException(String.format("Cant find %d th element in the map", position));
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);

    }
}