package hu.u_szeged.inf.aramis.activities.listpictures;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;

import hu.u_szeged.inf.aramis.R;
import hu.u_szeged.inf.aramis.camera.picture.PictureSaver;
import hu.u_szeged.inf.aramis.model.PictureRow;

@EViewGroup(R.layout.picture_list_row)
public class PictureListRow extends RelativeLayout {

    @ViewById
    TextView firstLine;
    @ViewById
    TextView secondLine;
    @ViewById
    ImageView icon;

    public PictureListRow(Context context) {
        super(context);
    }

    public void bind(PictureRow picture) {
        icon.setImageBitmap(picture.bitmap);
        firstLine.setText(picture.lastModified.toString(PictureSaver.DATE_TIME_FORMATTER));
    }
}
