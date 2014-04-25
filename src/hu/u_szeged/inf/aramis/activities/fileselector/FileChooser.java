package hu.u_szeged.inf.aramis.activities.fileselector;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.AdapterView;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import hu.u_szeged.inf.aramis.MainApplication;
import hu.u_szeged.inf.aramis.R;
import hu.u_szeged.inf.aramis.activities.DifferencePicturesActivity_;
import hu.u_szeged.inf.aramis.adapter.ProgressBarHandler;
import hu.u_szeged.inf.aramis.camera.utils.PictureSaver;
import hu.u_szeged.inf.aramis.model.Picture;

import static org.apache.commons.lang3.StringUtils.substringBefore;

@EActivity(R.layout.file_list)
public class FileChooser extends Activity implements AdapterView.OnItemClickListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileChooser.class);

    @App
    protected MainApplication application;
    @ViewById(R.id.list_files)
    android.widget.ListView pictureList;
    private ProgressBarHandler progressBarHandler;

    private File currentDir;
    private FileArrayAdapter adapter;

    @AfterViews
    void fillRoot() {
        try {
            application.getInjector().injectMembers(this);
            currentDir = new File(PictureSaver.getFilePathForRootDir());
            fill(currentDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Click(R.id.button_process)
    public void process() {
        processPictures();
    }

    @Background
    protected void processPictures() {
        List<Picture> pictures = getPictures();
        savePictures(pictures);
        startPagerActivity(pictures);
    }

    @UiThread
    void stopProgress() {
        progressBarHandler.stop();
    }

    void startProgress() {
        progressBarHandler = new ProgressBarHandler(this);
        progressBarHandler.start();
    }


    private void savePictures(List<Picture> pictures) {
        for (Picture picture : pictures) {
            PictureSaver.save(picture);
        }
    }

    private List<Picture> getPictures() {
        Collection<Item> filter = Collections2.filter(adapter.getItems(), new Predicate<Item>() {
            @Override
            public boolean apply(Item input) {
                return input.isSelected();
            }
        });
        List<Picture> pictures = Lists.newArrayList(Collections2.transform(filter, new Function<Item, Picture>() {
            @Override
            public Picture apply(Item input) {
                return Picture.picture(substringBefore(input.name, "."), BitmapFactory.decodeFile(input.path));
            }
        }));
        Collections.sort(pictures);
        return pictures;
    }

    @UiThread
    protected void startPagerActivity(List<Picture> pictures) {
        LOGGER.info("Starting pager activity!");
        DifferencePicturesActivity_.intent(this).picturePaths(Lists.transform(pictures, new Function<Picture, String>() {
            @Override
            public String apply(Picture input) {
                return input.name;
            }
        })).start();
    }

    private void fill(File f) {
        File[] dirs = f.listFiles();
        this.setTitle("Current Dir: " + f.getName());
        List<Item> dir = Lists.newArrayList();
        List<Item> fls = Lists.newArrayList();
        for (File ff : dirs) {
            Date lastModDate = new Date(ff.lastModified());
            DateFormat formatter = DateFormat.getDateTimeInstance();
            String date_modify = formatter.format(lastModDate);
            if (ff.isDirectory()) {
                dir.add(new Item(ff.getName(), date_modify, ff.getAbsolutePath(), "directory_icon"));
            } else {
                fls.add(new Item(ff.getName(), date_modify, ff.getAbsolutePath(), "file_icon"));
            }
        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
        if (!f.getName().equalsIgnoreCase("sdcard"))
            dir.add(0, new Item("..", "Parent Directory", f.getParent(), "directory_up"));
        adapter = new FileArrayAdapter(FileChooser.this, R.layout.file_view, dir);
        pictureList.setOnItemClickListener(this);
        pictureList.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Item o = adapter.getItem(position);
        if (o.image.equalsIgnoreCase("directory_icon") || o.image.equalsIgnoreCase("directory_up")) {
            currentDir = new File(o.path);
            fill(currentDir);
        }
    }

}
