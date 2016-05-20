package squishyaquarium;

import processing.core.PApplet;
import toxi.physics2d.VerletParticle2D;
import toxi.physics2d.VerletSpring2D;

import java.util.UUID;

/**
 * Created by kvloxx
 */
public class Spring extends VerletSpring2D implements SquishyBodyPart {
   public final String ID = UUID.randomUUID().toString();


   private PApplet p;
   private float str;
   private float maxLen, minLen;
   float state;  //between 0 and 1, the current length

   enum Type {
      BONE, MUSCLE, TISSUE
   }
   private Type type;

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
      switch (type) {
         case BONE:
            p.stroke(255);
            break;
         case MUSCLE:
//            p.stroke(229,115,115,p.map(state, 0, 1, 255, 0));
            p.stroke(200, 0, 0);
            break;
         case TISSUE:
            p.stroke(50, 50, 50, 200);
            break;
      }
      p.line(this.a.x, this.a.y, this.b.x, this.b.y);
   }


   @Override
   public void changeState(float state) {
      this.state = state;
      setRestLength(PApplet.map(state, 0, 1, minLen, maxLen));
   }

   @Override
   public String getID() {
      return ID;
   }

   @Override
   public String toString() {
      return " " + ID +
            " " + (type == Type.BONE ? "BONE" : (type == Type.MUSCLE ? "MUSCLE" : "TISSUE")) +
            " " + ((Node) a).getID() +
            " " + ((Node) b).getID() +
            " " + (maxLen + minLen)/2.0f +
            " ";
   }
}
