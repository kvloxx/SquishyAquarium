package squishyaquarium;

import processing.core.PApplet;
import processing.event.KeyEvent;
import toxi.geom.Vec2D;

import java.util.ArrayList;


public class SquishyAquarium extends PApplet {
   World world;
   float DRAG = 0.01f;

   SquishyBody squash, squesh;
   ArrayList<SquishyBody> squishes;
   ArrayList<SquishyBody> squoshes;
   int squashIdx = 0;
   int numSquishes = 5;
   Vec2D lol = new Vec2D(1, 0);
   int revolutions = 0;
   int TorB = -1; //Bottom is -1, top is +1
   int LorR = 1;  //Left is -1, right is +1
   float lastArc = 0;
   float clockwiseArc = 0;

   public static void main(String _args[]) {
      PApplet.main(new String[]{squishyaquarium.SquishyAquarium.class.getName()});
   }

   public void settings() {
      size(1000, 800, P2D);
   }

   public void setup() {
      background(0);
      surface.setResizable(true);
      init();

   }

   public void init() {
      world = new World(null, 1, DRAG, 1);
//      world.setWorldBounds(new Rect(0, 0, width, height));

      squishes = new ArrayList<>(numSquishes);
//      squash = new SquishyBody(this, world, 100, 100);
//      TreeParser to = new TreeParser("{\n" +
//
//            "strength [ 0.9 0.2 0.01 ]\n" +
//            "bone-length [ 25.0 75.0 ]\n" +
//            "branching-prob 0.4\n" +
//            "executeNextStrokeAction-extend-prob 0.5\n" +
//            "involves-node-prob 0.5\n" +
//            "7 { @1 [ 509.36438 396.49863 1.0 c6f01e ] { @2 [ 532.4074 453.57666 1.0 c6f01e ] { @3 [ 476.86893 417.91675 1.0 c6f01e ] {  } @4 [ 528.7835 497.08572 1.0 c6f01e ] {  } @5 [ 578.93604 492.7587 1.0 c6f01e ] {  }  } @6 [ 546.4256 371.37738 1.0 c6f01e ] { @7 [ 592.2436 383.562 1.0 c6f01e ] {  }  }  }  }\n" +
//            "21 { $B0 ( @1 : @2 ) [ 61.553905 61.553905 0.9 ] $B1 ( @1 : @6 ) [ 44.77288 44.77288 0.9 ] $B2 ( @6 : @7 ) [ 47.410484 47.410484 0.9 ] $B3 ( @2 : @3 ) [ 66.00115 66.00115 0.9 ] $B4 ( @2 : @4 ) [ 43.65972 43.65972 0.9 ] $B5 ( @2 : @5 ) [ 60.828815 60.828815 0.9 ] $M0 ( @6 : @2 ) [ 41.69302 125.079056 0.2 ] $M1 ( @1 : @3 ) [ 19.459513 58.37854 0.2 ] $M2 ( @1 : @4 ) [ 51.22223 153.66669 0.2 ] $M3 ( @4 : @3 ) [ 47.336166 142.0085 0.2 ] $M4 ( @1 : @5 ) [ 59.384796 178.15439 0.2 ] $M5 ( @5 : @3 ) [ 63.283115 189.84935 0.2 ] $M6 ( @5 : @4 ) [ 25.16942 75.50826 0.2 ] $M7 ( @1 : @7 ) [ 41.941387 125.82416 0.2 ] $T0 ( @2 : @7 ) [ 92.10005 92.10005 0.01 ] $T1 ( @6 : @3 ) [ 83.69016 83.69016 0.01 ] $T2 ( @6 : @4 ) [ 126.94027 126.94027 0.01 ] $T3 ( @6 : @5 ) [ 125.65967 125.65967 0.01 ] $T4 ( @7 : @3 ) [ 120.380905 120.380905 0.01 ] $T5 ( @7 : @4 ) [ 130.05696 130.05696 0.01 ] $T6 ( @7 : @5 ) [ 110.00458 110.00458 0.01 ]  }\n" +
//            "23 : 2 { ( @1 0.5508533 ) ( $M0 0.69540834 ) ( $M1 0.076572 ) ( $M5 0.058719397 ) ( @1 0.17666692 ) ( $M6 0.31214994 ) ( $M0 0.82248557 ) ( $M2 0.7595699 ) ( @1 0.77338225 ) ( $M5 0.85834444 ) ( $M6 0.25341743 ) ( $M5 0.18914849 ) ( @3 0.13213545 ) ( $M2 0.61908174 ) ( @3 0.47565866 ) ( $M2 0.93632734 ) ( $M2 0.91321754 ) ( $M1 0.50061196 ) ( $M2 0.30432367 ) ( @1 0.19906503 ) ( $M2 0.31636977 ) ( $M0 0.04903984 ) ( $M2 0.9210909 )  } \n" +
//            "}", this, world);
      squash = new SquishyBody(this, world);
//      squash = to.parseSquishyBody(false);
      squishes.add(squash);

//      SquishyBody copy = squash.copy();
//      copy.executeNextStrokeAction.currentBehavior=Stroke.ROTATE_NEGATIVE;
//      squishes.add(copy);
//      squash.getRandomNode().fill = 0xff8822ff;

      ArrayList<SquishyBody> bodies = new ArrayList<>();
      for (int i = 0; i < 10; i++) {

      }
//      SquishyBody squoosh = squash.copy();
//      SquishyBody squoosh = new SquishyBody(this, world);
//      squishes.add(squoosh);
      System.out.println(squash.stringify());
//      System.out.println(squoosh.stringify());
//      for (SquishyBody squeshy : SquishyBody.mate(squash, squoosh, this, world)) {
//         squishes.add(squeshy);
//      }
//      System.out.println("squoosh = " + squoosh);
//      Node nond=null;
//      for (Node node : squoosh.nodes) {
//
//         if(node.fill == 0xff8822ff){
//            nond = node;
//         }
//         else
//         node.fill = 0xff00f313;
//      }
//
//      Iterator<Node> nodeIterator = squoosh.nodes.iterator();
//      squoosh.getRandomNode().fill = 0xffff8822;
//      nodeIterator.next();
//      nodeIterator.next();
////      squoosh.removeSubtreeAt(nond);
//      squishes.add(squoosh);
////      squoosh.randomlyShiftSubtreeAt(nond);
//      SquishyBody limmby = new SquishyBody(squoosh.splitAbove(nond), this, world);
//      squishes.add(limmby);
//
//      System.out.println("squoosh = " + squoosh);


      for (int i = 0; i < 10; i++) {
//         System.out.println("i = " + i);
//         SquishyBody sb = tp.sloppyParseSquishyBody(0.1f);
//         sb.calm();
//         squishes.add(sb);

      }
//      nodeIterator = squash.nodes.iterator();
//      nodeIterator.next();
//      nd = nodeIterator.next();
//      nd = nodeIterator.next();
//      nd.d = 20;

//      System.out.println("Squash");
//      System.out.println(squash.stringifyTree());
//      System.out.println("Squoosh");
//      System.out.println(squoosh.stringifyTree());
//      System.out.println("---");
//      System.out.println(squash.stringifyTree());
//
////      TreeParser tp = new TreeParser(s, this, world);
//      TreeParser tp = new TreeParser(squash.stringifyTree(), this, world);
//      System.out.println(s);
//      SquishyBody sb = new SquishyBody(this, world);
//      System.out.println(sb.stringifyTree());
//      squishes.add(sb);
//      squishes.add(tp.parseSquishyBody());

//      SquishyBody s2 = new SquishyBody(this, world);
//      squishes.add(s2);
//      System.out.println(squash.stringifyTree());
//      System.out.println(s2.stringifyTree());
//      SquishyBody[] sbs = SquishyBody.mate(squash, sb,this, world);
//      Collections.addAll(squishes, sbs);
      for (SquishyBody squish : squishes) {
         squish.showDebug();
         world.addBody(squish);
      }

   }

   public void draw() {

      background(0);
      world.update();

      if (squishes != null) {
         for (SquishyBody squish : squishes) {
            squish.display();
         }
      }

      if (mousePressed) {
         if (mouseButton == LEFT) {

            squash.root.lock();
            squash.root.x = mouseX;
            squash.root.y = mouseY;
            squash.root.unlock();
         }
      }
   }

   @Override
   public void keyReleased(KeyEvent event) {
      if (event.getKeyCode() == LEFT) {
         squashIdx++;
         squashIdx %= squishes.size();
         squash = squishes.get(squashIdx);
         return;
      }

      if (event.getKeyCode() == RIGHT) {
         int a;
         squash.stroke.setCurrentBehavior(a = (squash.stroke.getCurrentBehavior() + 1)%3);
         System.out.println(a);
         return;
      } else {
         init();
      }
   }

}