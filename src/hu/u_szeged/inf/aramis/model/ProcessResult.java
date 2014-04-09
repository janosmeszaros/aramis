package hu.u_szeged.inf.aramis.model;

import java.util.List;
import java.util.Map;

public class ProcessResult {
    public final Map<String, List<ClusterPair>> stringListMap;
    public final String backgroundFilePath;

    public ProcessResult(Map<String, List<ClusterPair>> stringListMap, String backgroundFilePath) {
        this.stringListMap = stringListMap;
        this.backgroundFilePath = backgroundFilePath;
    }

    public static ProcessResult processResult(Map<String, List<ClusterPair>> stringListMap, String filePathForPicture) {
        return new ProcessResult(stringListMap, filePathForPicture);
    }
}
