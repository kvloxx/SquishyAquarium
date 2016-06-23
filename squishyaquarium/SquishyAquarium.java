package squishyaquarium;

import processing.core.*;
import processing.event.KeyEvent;
import toxi.physics2d.*;
import toxi.geom.*;

import java.util.ArrayList;


public class SquishyAquarium extends PApplet {
   VerletPhysics2D world;
   float DRAG = 0.01f;

   SquishyBody squash, squesh;
   ArrayList<SquishyBody> squishes;
   ArrayList<SquishyBody> squoshes;
   int squashIdx = 0;
   int numSquishes = 5;

   public void settings() {
      size(1000, 800, P2D);
   }

   public void setup() {
      background(0);
      surface.setResizable(true);

      world = new VerletPhysics2D(null, 1, DRAG, 1);
      world.setWorldBounds(new Rect(0, 0, width, height));

      squishes = new ArrayList<>(numSquishes);
      squash = new SquishyBody(this, world);
      squishes.add(squash);

//      System.out.println("Subtree Stringification: ");
//      String s=squash.head.stringifySubtree();
//      System.out.println("s = " + s);
//      System.out.println("\nReconstruction:");
      for (Node node : squash.head.getSubtreeNodes()) {
         if (!node.boneChildren.isEmpty()) {
            TreeParser tp = new TreeParser(node.stringifySubtree(), this, world);
            squishes.add(tp.parseSubtree());
         }
      }


      /*squoshes = new ArrayList<>(numSquishes);
      for (int i = 0; i < numSquishes; i++) {
         squash = new SquishyBody(this, world, random(width), random(height));
         String encoding = squash.encodeBody();
         squishes.add(squash);
         squesh = new SquishyBody(this, world, encoding);
         squoshes.add(squesh);
      }
      squash = squishes.get(squashIdx);
      squesh = squoshes.get(squashIdx);*/

   }

   public void draw() {

      background(0);
      world.update();

               if (squishes != null) {
      squishes.forEach(squish -> {

                  if (frameCount % squish.strokeInterval == 0) {
                     squish.stroke();
                  }
                  squish.display();
               });
            }

      /*squoshes.forEach(squosh -> {
         if (frameCount % squosh.strokeInterval == 0) {
            squosh.stroke();
         }
         squosh.display();
      });*/

      if (mousePressed) {

         if (mouseButton == LEFT) {
            squash.head.lock();
            squash.head.x = mouseX;
            squash.head.y = mouseY;
            squash.head.unlock();
         }
//         if (mouseButton == RIGHT) {
//            squesh.head.lock();
//            squesh.head.x = mouseX;
//            squesh.head.y = mouseY;
//            squesh.head.unlock();
//         }
      }
   }

   //   public void keyReleased() {
//      for (int i = 0; i < squishes.size(); i++) {
//         squishes.set(i, new SquishyBody(this, world, random(width), random(height)));
//      }
//      squash=squishes.get(0);
//   }


   @Override
   public void keyReleased(KeyEvent event) {
      if (event.getKeyCode() == LEFT) {
         squashIdx++;
         squashIdx %= numSquishes;
         squash = squishes.get(squashIdx);
//         squesh = squoshes.get(squashIdx);
         return;
      }
      if (event.getKeyCode() == RIGHT) {
         if (--squashIdx < 0) {
            squashIdx = numSquishes - 1;
         }
         squash = squishes.get(squashIdx);
//         squesh = squoshes.get(squashIdx);
         return;
      }

      world = new VerletPhysics2D(null, 1, DRAG, 1);
      squishes = new ArrayList<>(numSquishes);
      squash = new SquishyBody(this, world);
      squishes.add(squash);
      System.out.println("lol");
      System.out.println(squash.stringify());
//      for (Node node : squash.head.getSubtreeNodes()) {
//         if (!node.boneChildren.isEmpty()) {
//            TreeParser tp = new TreeParser(node.stringifySubtree(true), this, world);
//            squishes.add(tp.parseSubtree());
//         }
//      }

      /*squishes = new ArrayList<>(numSquishes);
      squoshes = new ArrayList<>(numSquishes);
      for (int i = 0; i < numSquishes; i++) {
         squash = new SquishyBody(this, world, random(width), random(height));
         String encoding = squash.encodeBody();
         squishes.add(squash);
         squesh = new SquishyBody(this, world, encoding);
         squoshes.add(squesh);
      }
      squash = squishes.get(squashIdx);
      squesh = squoshes.get(squashIdx);*/
   }

   public static void main(String _args[]) {
      PApplet.main(new String[]{squishyaquarium.SquishyAquarium.class.getName()});
   }

}