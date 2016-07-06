package squishyaquarium;

import processing.core.PApplet;
import toxi.physics2d.VerletParticle2D;
import toxi.physics2d.VerletSpring2D;

import java.util.Set;
import java.util.UUID;

/**
 * Created by kvloxx
 */
public class Spring extends VerletSpring2D implements SquishyBodyPart {
   private final String springID = UUID.randomUUID().toString();
   boolean isBroken = false;
   float str;
   float maxLen, minLen;
   float state;  //between 0 and 1, the current length
   Type type;
   private PApplet p;

   Spring(VerletParticle2D a, VerletParticle2D b, Type type, float minLen, float maxLen, float str, PApplet p) {
      super(a, b, PApplet.map(0.5f, 0, 1, minLen, maxLen), str);
      this.type = type;
      this.p = p;
      this.state = 0.5f;
      this.maxLen = maxLen;
      this.minLen = minLen;
      this.str = str;
   }

   void display() {
      if(!isBroken) {
         switch (type) {
            case BONE:
               p.stroke(0xffffffff);
               break;
//            return;
            case MUSCLE:
               p.stroke(229, 115, 115, PApplet.map(state, 0, 1, 255, 0));
//            p.executeNextStrokeAction(200, 0, 0);
               break;
            case TISSUE:
               return;
//            p.executeNextStrokeAction(0xffE450E4);

//            p.executeNextStrokeAction(50, 250, 50, 200);
//            break;
         }
         p.line(a.x, a.y, b.x, b.y);
      }
   }

   public void completeConnection() {
      Node parent = getA();
      Node child = getB();
      if (type == Spring.Type.BONE) {
         parent.addBoneChild(child);
         child.addBoneParent(parent);
      } else {
         parent.addNeighbor(child);
         child.addNeighbor(parent);
      }
      parent.attachSpring(this);
      child.attachSpring(this);
   }

   public Node getA() {
      return ((Node) a);
   }

   public Node getB() {
      return ((Node) b);
   }

   @Override
   public void changeState(float state) {
      this.state = state;
      setRestLength(PApplet.map(state, 0, 1, minLen, maxLen));
   }

   public boolean isContainedIn(Set<Node> nodeSet, boolean fullyContained) {
      if (fullyContained) {
         return nodeSet.contains(getA()) && nodeSet.contains(getB());
      } else {
         return nodeSet.contains(getA()) || nodeSet.contains(getB());
      }
     /* return ((a=nodeSet.contains(this.getA())) || (b=nodeSet.contains(this.getB())) && !fullyContained)
            || a && b;*/
   }

   public void resetState() {
      this.state = 0.5f;
      setRestLength((maxLen + minLen) / 2f);
   }

   public String getDataString() {
      return new StringBuilder()
            .append("[ ")
            .append(minLen)
            .append(" ")
            .append(maxLen)
            .append(" ")
            .append(str)
            .append(" ] ").toString();
   }

   public boolean isContainedIn(Set<Node> nodeSet) {
      return isContainedIn(nodeSet, true);
   }

   public void setCurrentLengthAsRestLength() {
      float len = a.distanceTo(b);
      setRestLength(len);
      this.state = 0.5f;
      if (isMuscle()) {
         this.maxLen = len * 1.5f;
         this.minLen = len * 0.5f;
      } else {
         this.maxLen = len;
         this.minLen = len;
      }
   }

   public boolean isMuscle() {
      return type == Type.MUSCLE;
   }

   public void useAsPositioningForce(float length, float strength) {
      setRestLength(length);
      setStrength(strength);
   }

   public void restore() {
      setStrength(str);
      setRestLength((minLen + maxLen) / 2.0f);
   }

   @Override
   public int hashCode() {
      return springID.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      return (obj instanceof Spring && ((Spring) obj).springID.equals(springID));
   }

   @Override
   public String toString() {
      return "[" + (type == Type.BONE ? "BONE" : (type == Type.MUSCLE ? "MUSCLE" : "TISSUE")) +
            " " + getA() +
            " " + getB() +
            " " + getAverageLen() +
            "] ";
   }

   public float getAverageLen() {
      return (minLen + maxLen) / 2.0f;
   }

   @Override
   protected void update(boolean b) {
      super.update(b);
   }

   public boolean isBone() {
      return type == Type.BONE;
   }

   public boolean isTissue() {
      return type == Type.TISSUE;
   }

   enum Type {
      BONE, MUSCLE, TISSUE
   }
}
