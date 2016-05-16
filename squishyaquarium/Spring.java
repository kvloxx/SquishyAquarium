package squishyaquarium;

import processing.core.PApplet;
import toxi.physics2d.VerletParticle2D;
import toxi.physics2d.VerletSpring2D;

/**
 * Created by Adam on 5/15/2016.
 *
 */
public class Spring extends VerletSpring2D {
   PApplet p;
   float str;
   float maxLen, minLen;
   float len;  //between 0 and 1, the current length
   enum Type{
      BONE, MUSCLE, TISSUE
   }
   Type type;

   public Spring(VerletParticle2D a, VerletParticle2D b, Type type, float minLen, float maxLen, float str, PApplet p) {
      super(a, b, p.map(0.5f, 0, 1, minLen, maxLen), str);
      this.type=type;
      this.p=p;
      this.len=0.5f;
      this.maxLen = maxLen;
      this.minLen = minLen;
      this.str=str;
   }

   public void changeLength(float newlen){
      this.len=newlen;
      setRestLength(p.map(newlen, 0, 1, minLen, maxLen));
   }

   public void display(){
      switch (type){
         case BONE:
            p.stroke(255);
            break;
         case MUSCLE:
            p.stroke(229,115,115,p.map(len, 0, 1, 255, 0));
            break;
         case TISSUE:
            p.stroke(50, 50, 50, 50);
            break;
      }
      p.line(this.a.x, this.a.y, this.b.x, this.b.y);
   }
}
