package squishyaquarium;

import java.util.Set;

/**
 * Created by kvloxx
 */
interface SquishyBodyPart {
   void changeState(float state);
   boolean isContainedIn(Set<Node> nodeSet, boolean fullyContained);
   void reset();
}
