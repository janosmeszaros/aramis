package hu.u_szeged.inf.aramis.camera;

import com.google.common.collect.Lists;
import com.googlecode.androidannotations.annotations.EBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import hu.u_szeged.inf.aramis.camera.picture.DiffCounter;
import hu.u_szeged.inf.aramis.model.Coordinate;
import hu.u_szeged.inf.aramis.model.Picture;

@EBean
public class PictureCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger(PictureCollector.class);

    private List<Picture> pictures = Lists.newArrayList();
    private List<FutureTask> tasks = Lists.newArrayList();

    private CountDownLatch countDown = new CountDownLatch(TakePictureCallback.PICTURE_NUMBER - 1);
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public void addPicture(Picture picture) {
        pictures.add(picture);
        if (pictures.size() > 1) {
            FutureTask<List<Coordinate>> task = new FutureTask<List<Coordinate>>(
                    new DiffCounter(countDown, pictures.get(pictures.size() - 2), pictures.get(pictures.size() - 1)));
            tasks.add(task);
            executorService.execute(task);
        }
    }

    public List<Coordinate> getDiffCoordinates() throws InterruptedException, ExecutionException {
        LOGGER.info("Waiting for countdown!");
        countDown.await();
        LOGGER.info("Countdown finished!");
        Set<Coordinate> diffPictures = new HashSet<Coordinate>();
        for (FutureTask<List<Coordinate>> task : tasks) {
            LOGGER.debug("Adding coordinate for task no: {}", tasks.indexOf(task));
            diffPictures.addAll(task.get());
        }
        return Lists.newArrayList(diffPictures);
    }

    public void clear() {
        tasks.clear();
        pictures.clear();
    }

    public int getSize() {
        return pictures.size();
    }

    public List<Picture> getPictures() {
        return pictures;
    }
}
