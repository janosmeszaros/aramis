package hu.u_szeged.inf.aramis.activities.listpictures;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

import java.io.File;

import hu.u_szeged.inf.aramis.R;

public class FinalResultAdapter extends PagerAdapter {
    private ImmutableList<File> pictures;

    private final Activity activity;
    private ImageView imgDisplay;

    public FinalResultAdapter(ImmutableList<File> pictures,
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
        View viewLayout = inflater.inflate(R.layout.result_image, container,
                false);

        imgDisplay = (ImageView) viewLayout.findViewById(R.id.imgDisplay);

        Bitmap actualPicture = getElementAt(position);
        imgDisplay.setImageBitmap(actualPicture);

        ((ViewPager) container).addView(viewLayout);

        return viewLayout;
    }

    private Bitmap getElementAt(final int position) {
        ImmutableList<File> collection = ImmutableList.copyOf(Collections2.filter(pictures, new Predicate<File>() {
            @Override
            public boolean apply(File input) {
                return input.getName().endsWith(position + 1 + ".jpeg");
            }
        }));
        if (!collection.isEmpty()) {
            return BitmapFactory.decodeFile(collection.get(0).getAbsolutePath());
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
}