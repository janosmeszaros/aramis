package hu.u_szeged.inf.aramis.camera.process;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import hu.u_szeged.inf.aramis.camera.process.difference.CounterScheduler;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;

public class PictureCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger(PictureCollector.class);
    private List<Picture> pictures = Lists.newArrayList();
    private final CounterScheduler counterScheduler;

    private PictureCollector(CounterScheduler counterScheduler) {
        this.counterScheduler = counterScheduler;
    }

    public static PictureCollector pictureCollector(CounterScheduler counterScheduler) {
        return new PictureCollector(counterScheduler);
    }

    public void addPictureWithoutSchedule(Picture picture) {
        pictures.add(picture);
    }

    public void addPicture(Picture picture) {
        pictures.add(picture);
        int actualSize = pictures.size();
        if (actualSize > 1) {
            counterScheduler.schedule(pictures.get(actualSize - 2), pictures.get(actualSize - 1));
        }
    }

    public Set<Coordinate> getDiffCoordinates() throws InterruptedException, ExecutionException {
        return counterScheduler.getDiffCoordinates();
    }

    public void clear() {
        pictures.clear();
        counterScheduler.clear();
    }

    public int getSize() {
        return pictures.size();
    }

    public List<Picture> getPictures() {
        return ImmutableList.copyOf(pictures);
    }
}
