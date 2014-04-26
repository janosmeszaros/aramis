package hu.u_szeged.inf.aramis.camera.process;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;

import hu.u_szeged.inf.aramis.camera.process.difference.CounterScheduler;
import hu.u_szeged.inf.aramis.model.BlurredPicture;
import hu.u_szeged.inf.aramis.model.Picture;

public class PictureCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger(PictureCollector.class);
    private final CounterScheduler counterScheduler;
    private List<BlurredPicture> pictures = Lists.newArrayList();

    private PictureCollector(CounterScheduler counterScheduler) {
        this.counterScheduler = counterScheduler;
    }

    public static PictureCollector pictureCollector(CounterScheduler counterScheduler) {
        return new PictureCollector(counterScheduler);
    }

    public void addPictures(List<Picture> pictures) {
        for (Picture picture : pictures) {
            addPicture(picture);
        }
    }

    public void addPicture(Picture picture) {
        pictures.add(BlurredPicture.blurredPicture(picture.bitmap, picture.name));
        int actualSize = pictures.size();
        if (actualSize > 1) {
            counterScheduler.schedule(pictures.get(actualSize - 2), pictures.get(actualSize - 1));
        }
    }

    public Table<Integer, Integer, Boolean> getDiffCoordinates() throws InterruptedException, ExecutionException {
        return counterScheduler.getDiffCoordinates();
    }

    public void clear() {
        pictures.clear();
    }

    public int getSize() {
        return pictures.size();
    }

    public List<BlurredPicture> getPictures() {
        ImmutableList<BlurredPicture> blurredPictures = ImmutableList.copyOf(pictures);
        clear();
        return blurredPictures;
    }
}
