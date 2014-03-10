package hu.u_szeged.inf.aramis.activities.listpictures;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.common.collect.ImmutableList;
import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;

import hu.u_szeged.inf.aramis.camera.utils.DirectoryHelper;
import hu.u_szeged.inf.aramis.camera.utils.PictureSaver;
import hu.u_szeged.inf.aramis.model.PictureRow;

@EBean
public class PictureListAdapter extends BaseAdapter {
    @RootContext
    Context context;
    private ImmutableList<PictureRow> pictureRows;

    @AfterInject
    void initAdapter() {
        pictureRows = DirectoryHelper.getAllPictures(PictureSaver.ALBUM_NAME);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PictureListRow pictureListRow;
        if (convertView == null) {
            pictureListRow = PictureListRow_.build(context);
        } else {
            pictureListRow = (PictureListRow) convertView;
        }

        pictureListRow.bind(getItem(position));

        return pictureListRow;
    }

    @Override
    public int getCount() {
        return pictureRows.size();
    }

    @Override
    public PictureRow getItem(int position) {
        return pictureRows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}