package hu.u_szeged.inf.aramis.camera.process.motion;

import hu.u_szeged.inf.aramis.model.ClusterWithMoments;
import hu.u_szeged.inf.aramis.model.MomentsVector;

import static hu.u_szeged.inf.aramis.utils.DistanceUtils.canberraDistance;

public final class MomentsDistanceCounter {

    public double countDistances(ClusterWithMoments momentsForFirst,
                                 ClusterWithMoments momentsForSecond) {
        MomentsVector firstMomentVector = momentsForFirst.momentsVector;
        MomentsVector secondMomentVector = momentsForSecond.momentsVector;
        double result = 0.0;
        result += canberraDistance(firstMomentVector.first, secondMomentVector.first);
        result += canberraDistance(firstMomentVector.second, secondMomentVector.second);
        result += canberraDistance(firstMomentVector.third, secondMomentVector.third);
        result += canberraDistance(firstMomentVector.fourth, secondMomentVector.fourth);
        result += canberraDistance(firstMomentVector.fifth, secondMomentVector.fifth);
        result += canberraDistance(firstMomentVector.sixth, secondMomentVector.sixth);
        result += canberraDistance(firstMomentVector.seventh, secondMomentVector.seventh);
        return result;
    }
}
