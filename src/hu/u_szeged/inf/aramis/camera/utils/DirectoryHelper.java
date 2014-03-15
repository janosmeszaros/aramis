package hu.u_szeged.inf.aramis.camera.utils;

import android.content.Context;
import android.os.Environment;

import com.google.common.collect.ImmutableList;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import hu.u_szeged.inf.aramis.model.PictureRow;

import static hu.u_szeged.inf.aramis.model.PictureRow.pictureRow;

@EBean
public class DirectoryHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryHelper.class);
    @RootContext
    protected static Context context;

    public static ImmutableList<PictureRow> getAllPictures(String albumName) {
        ImmutableList.Builder<PictureRow> builder = ImmutableList.builder();
        File album = getAlbum(albumName);
        for (File subDir : album.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY)) {
            ImmutableList<File> pictures = ImmutableList.copyOf(
                    subDir.listFiles((FileFilter) FileFilterUtils.prefixFileFilter("finalResult")));
            if (!pictures.isEmpty()) {
                builder.add(pictureRow(new DateTime(subDir.lastModified()), pictures));
            }
        }
        return builder.build();
    }

    public static File getAlbumStorageDir(String albumName) throws IOException {
        File file = getAlbum(albumName);
        if (!file.exists()) {
            FileUtils.forceMkdir(file);
            LOGGER.info("Directory created in {}", file.getAbsolutePath());
        }
        return file;
    }

    private static File getAlbum(String albumName) {
        File file;
        if (isExternalStorageWritable()) {
            file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), albumName);
        } else {
            file = FileUtils.getFile(context.getFilesDir(), albumName);
        }
        return file;
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
