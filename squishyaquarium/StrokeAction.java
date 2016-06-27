package squishyaquarium;

import java.util.Set;

/**
 * Created by kvloxx
 */
public class StrokeAction {
   SquishyBodyPart obj;
   float state;

   StrokeAction(SquishyBodyPart obj, float state) {
      this.obj = obj;
      this.state = state;
   }

   void executeAction() {
      obj.changeState(state);
   }

   boolean isNode() {
      return obj instanceof Node;
   }

   boolean actionIsContainedIn(Set<Node> nodeSet) {
      return obj.isContainedIn(nodeSet);
   }
}
