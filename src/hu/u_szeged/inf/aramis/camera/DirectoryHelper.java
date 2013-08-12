package hu.u_szeged.inf.aramis.camera;

import android.content.Context;
import android.os.Environment;

import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

@EBean
public class DirectoryHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryHelper.class);

    @RootContext
    protected static Context context;

    public static File getAlbumStorageDir(String albumName) throws IOException {
        File file;
        if (isExternalStorageWritable()) {
            file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), albumName);
        } else {
            file = FileUtils.getFile(context.getFilesDir(), albumName);
        }
        FileUtils.forceMkdir(file);
        LOGGER.info("Directory created in {}", file.getAbsolutePath());

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
