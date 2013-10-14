package hu.u_szeged.inf.aramis.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import com.google.common.collect.ImmutableList;
import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;

import hu.u_szeged.inf.aramis.model.PictureRow;

import static android.graphics.BitmapFactory.decodeFile;
import static hu.u_szeged.inf.aramis.model.PictureRow.pictureRow;

@EBean
public class DirectoryHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryHelper.class);

    @RootContext
    protected static Context context;

    public static ImmutableList<PictureRow> getAllPictures(String albumName) {
        ImmutableList.Builder<PictureRow> builder = ImmutableList.builder();
        for (File picture : getAlbum(albumName).listFiles()) {
            builder.add(pictureRow(decodeFile(picture.getPath()), new DateTime(picture.lastModified()), picture));
        }
        return builder.build();
    }

    public static Bitmap getLastPictureFromAlbum(String albumName) {
        File album = getAlbum(albumName);
        File[] files = album.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        });
        Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
        return decodeFile(files[0].getPath());
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
