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
               p.stroke(229, 115, 115, p.map(state, 0, 1, 255, 0));
//            p.executeNextStrokeAction(200, 0, 0);
               break;
            case TISSUE:
               return;
//            p.executeNextStrokeAction(0xffE450E4);

//            p.executeNextStrokeAction(50, 250, 50, 200);
//            break;
         }
         p.line(this.a.x, this.a.y, this.b.x, this.b.y);
      }
   }

   public void completeConnection() {
      Node parent = this.getA();
      Node child = this.getB();
      if (this.type == Spring.Type.BONE) {
         parent.addBoneChild(child);
         child.addBoneParent(parent);
      } else {
         parent.addNeighbor(child);
         child.addNeighbor(parent);
      }
      parent.attachSpring(this);
      child.attachSpring(this);
   }

   @Override
   public void changeState(float state) {
      this.state = state;
      setRestLength(PApplet.map(state, 0, 1, minLen, maxLen));
   }

   @Override
   public String toString() {
      return "[" + (type == Type.BONE ? "BONE" : (type == Type.MUSCLE ? "MUSCLE" : "TISSUE")) +
            " " + getA() +
            " " + getB() +
            " " + getAverageLen() +
            "] ";
   }

   public String getDataString() {
      return new StringBuilder()
            .append("[ ")
            .append(this.minLen)
            .append(" ")
            .append(this.maxLen)
            .append(" ")
            .append(this.str)
            .append(" ] ").toString();
   }

   public boolean isContainedIn(Set<Node> nodeSet) {
      return isContainedIn(nodeSet, true);
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

   public void setCurrentLengthAsRestLength() {
      float len = a.distanceTo(b);
      this.setRestLength(len);
      this.state = 0.5f;
      if (this.isMuscle()) {
         this.maxLen = len * 1.5f;
         this.minLen = len * 0.5f;
      } else {
         this.maxLen = len;
         this.minLen = len;
      }
   }

   public void useAsPositioningForce(float length, float strength) {
      this.setRestLength(length);
      this.setStrength(strength);
   }

   public void restore() {
      this.setStrength(str);
      this.setRestLength((minLen + maxLen) / 2.0f);
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
   protected void update(boolean b) {
      super.update(b);
   }

   public boolean isBone() {
      return this.type == Type.BONE;
   }

   public boolean isMuscle() {
      return this.type == Type.MUSCLE;
   }

   public boolean isTissue() {
      return this.type == Type.TISSUE;
   }

   public Node getA() {
      return ((Node) a);
   }

   public Node getB() {
      return ((Node) b);
   }

   public float getAverageLen() {
      return (minLen + maxLen) / 2.0f;
   }

   public void reset() {
      this.state = 0.5f;
      setRestLength((maxLen + minLen) / 2f);
   }

   enum Type {
      BONE, MUSCLE, TISSUE
   }
}
