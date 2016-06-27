package squishyaquarium;

import java.util.Set;

/**
 * Created by kvloxx
 */
public interface SquishyBodyPart {
   void changeState(float state);
   boolean isContainedIn(Set<Node> nodeSet);
}
