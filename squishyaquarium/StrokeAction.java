package squishyaquarium;

/**
 * Created by kvloxx
 */
class StrokeAction {
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

}
