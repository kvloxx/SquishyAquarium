package squishyaquarium;

import processing.core.*;
import toxi.physics2d.*;
import toxi.geom.*;

import java.util.ArrayList;


public class SquishyAquarium extends PApplet {
   VerletPhysics2D world;
   float DRAG = 0.01f;

   SquishyBody squash;
   ArrayList<SquishyBody> squishes;
   int numSquishes =5;

   public void settings() {
      size(1000, 800, P2D);
   }

   public void setup() {
      background(0);
      surface.setResizable(true);

      world = new VerletPhysics2D(null, 1, DRAG, 1);
      world.setWorldBounds(new Rect(0, 0, width, height));

      squishes=new ArrayList<>();
      for (int i = 0; i < numSquishes; i++) {
         squishes.add(new SquishyBody(this, world, random(width), random(height)));
      }
      squash = squishes.get(0);
   }

   public void draw() {

      background(0);
      world.update();

      squishes.forEach(squish->{
         if(frameCount%squish.strokeInterval==0){
            squish.stroke();
         }
         squish.display();
      });

      if (mousePressed) {

         squash.head.lock();
         squash.head.x = mouseX;
         squash.head.y = mouseY;
         squash.head.unlock();

      }
   }

   public void keyReleased() {
      for (int i = 0; i < squishes.size(); i++) {
         squishes.set(i, new SquishyBody(this, world, random(width), random(height)));
      }
      squash=squishes.get(0);
   }


   public static void main(String _args[]) {
      PApplet.main(new String[]{squishyaquarium.SquishyAquarium.class.getName()});
   }


}