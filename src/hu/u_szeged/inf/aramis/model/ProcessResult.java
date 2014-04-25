package hu.u_szeged.inf.aramis.model;

import java.util.List;
import java.util.Map;

public class ProcessResult {
    public final Map<Picture, List<ClusterPair>> stringListMap;
    public final Picture backgroundPicture;

    public ProcessResult(Map<Picture, List<ClusterPair>> stringListMap, Picture backgroundPicture) {
        this.stringListMap = stringListMap;
        this.backgroundPicture = backgroundPicture;
    }

    public static ProcessResult processResult(Map<Picture, List<ClusterPair>> stringListMap, Picture backgroundPicture) {
        return new ProcessResult(stringListMap, backgroundPicture);
    }
}
