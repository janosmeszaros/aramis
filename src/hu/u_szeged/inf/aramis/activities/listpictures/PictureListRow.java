package hu.u_szeged.inf.aramis.activities.listpictures;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.EViewGroup;
import com.googlecode.androidannotations.annotations.ViewById;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.u_szeged.inf.aramis.R;
import hu.u_szeged.inf.aramis.camera.utils.PictureSaver;
import hu.u_szeged.inf.aramis.model.PictureRow;

@EViewGroup(R.layout.picture_list_row)
public class PictureListRow extends RelativeLayout {
    private static final Logger LOGGER = LoggerFactory.getLogger(PictureListRow.class);
    @ViewById(R.id.firstLine)
    TextView firstLine;
    @ViewById(R.id.secondLine)
    TextView secondLine;
    @ViewById
    ImageView icon;

    public PictureListRow(Context context) {
        super(context);
    }

    public void bind(PictureRow picture) {
        LOGGER.info("Write out picture: {}", picture);
        icon.setImageBitmap(picture.bitmap);
        firstLine.setText(picture.file.getName());
        secondLine.setText(picture.lastModified.toString(PictureSaver.DATE_TIME_FORMATTER));
    }
}
