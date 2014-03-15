package hu.u_szeged.inf.aramis.activities.listpictures;

import android.app.Activity;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.ViewById;

import hu.u_szeged.inf.aramis.R;
import hu.u_szeged.inf.aramis.model.PictureRow;

@EActivity(R.layout.picture_list)
public class PictureListActivity extends Activity {
    @ViewById
    android.widget.ListView pictureList;
    @Bean
    PictureListAdapter adapter;

    @AfterViews
    void bindAdapter() {
        pictureList.setAdapter(adapter);
    }

    @ItemClick
    void pictureListItemClicked(PictureRow row) {
        FinalResultActivity_.intent(this).files(row.files).start();
    }
}
