package squishyaquarium;

import processing.core.PApplet;
import toxi.physics2d.VerletPhysics2D;

import java.util.*;

/**
 * Created by Adam on 5/15/2016.
 */
public class SquishyBody {

   int minNodes = 5;
   int nodesRange = 3;
   int maxNodes = minNodes + nodesRange;

   float maxBoneLength = 75;
   float minBoneLength = 50;
   float branchingProb = 0.4f;

   int minStrokeActions = 10;
   float strokeExtendProb = 0.5f;
   float actionInvolvesNodeProb = 0.5f;

   float boneStr = 0.9f;
   float muscStr = 0.1f;
   float tissStr = 0.01f;

   PApplet p;
   VerletPhysics2D world;

   Set<Node> nodes;
   Set<Spring> springs;
   Set<Spring> muscles;
   Node head;

   Node[] nodesArray;
   Spring[] springsArray;

   int maxStrokeInterval = 10;
   int minStrokeInterval = 2;
   int strokeInterval;

   String strokeBehavior;
   Scanner strokeScan;


   public SquishyBody(PApplet p, VerletPhysics2D world, float headX, float headY) {
      this.p = p;
      this.world = world;
      this.nodes = new LinkedHashSet<>();
      this.springs = new LinkedHashSet<>();
      this.muscles = new LinkedHashSet<>();

      growNodes(nodes);
      head.x = headX;
      head.y = headY;
      ossify(nodes);
      fleshOut(nodes);

      nodes.forEach(node -> node.scaleVelocity(0));

      this.nodesArray = nodes.toArray(new Node[0]);
      this.springsArray = muscles.toArray(new Spring[0]);

      this.strokeBehavior = makeStroke();
      this.strokeScan = new Scanner(strokeBehavior);
   }

   public SquishyBody(PApplet p, VerletPhysics2D world) {
      this(p, world, p.width / 2.0f, p.height / 2.0f);
   }

   private void growNodes(Set<Node> nodes) {
      int numNodes = (int) (p.random(minNodes, maxNodes + 1)); //range: [minNodes, maxNodes]
      int fill= p.color(
            (int) p.random(255),
            (int) p.random(255),
            (int) p.random(255));
      for (int i = 0; i < numNodes; i++) {
         Node n = new Node(i, i, fill, p);   //set pos to (i,i) as placeholder so nodes aren't all stacked
         if (i == 0) {
            setAsHead(n);
         }
         nodes.add(n);
         world.addParticle(n);
      }
   }

   private void setAsHead(Node head) {
      if (head != null) {
         head.isHead = false;
      }
      this.head = head;
      head.isHead = true;
      head.x = p.width / 2.0f;
      head.y = p.height / 2.0f;
   }

   private void ossify(Set<Node> nodes) {
      PriorityQueue<Node> unconnected = new PriorityQueue<>(nodes);
      PriorityQueue<Node> connected = new PriorityQueue<>();

      connected.add(head);
      unconnected.remove(head);

      while (!connected.isEmpty()) {
         Node conn = connected.remove();
         if (!unconnected.isEmpty()) {
            do {
               Node unconn = unconnected.remove();
               float r = p.random(minBoneLength, maxBoneLength);
               do {
                  float theta = p.random(p.PI * 2);
                  unconn.x = conn.x + r * p.cos(theta);
                  unconn.y = conn.y + r * p.sin(theta);
               } while (springsDoIntersect(unconn.x, unconn.y, conn.x, conn.y));
               growBone(conn, unconn, r);
               connected.add(unconn);
            } while (Math.random() <= branchingProb && !unconnected.isEmpty());
         }
      }
   }

   private void fleshOut(Set<Node> nodes) {
      nodes.forEach(thisNode -> {
         Node[] bonyNeighborsArr = thisNode.boneNeighbors.toArray(new Node[0]);
         for (int i = 0; i < bonyNeighborsArr.length; i++) {
            for (int j = 0; j < i; j++) {
               float len = p.random(51, 100);
               attachMuscle(bonyNeighborsArr[i], bonyNeighborsArr[j], bonyNeighborsArr[i].distanceTo
                     (bonyNeighborsArr[j]));
            }
         }
         nodes.forEach(thatNode -> {
            if (thisNode != thatNode) {
               if (!thisNode.neighbors.contains(thatNode)) {
                  connectTissue(thisNode, thatNode, thisNode.distanceTo(thatNode));
               }
            }
         });
      });
   }

   private void connectTissue(Node from, Node to, float len) {
      Spring s = new Spring(from, to, Spring.Type.TISSUE, len, len, tissStr, p);
      springs.add(s);
      world.addSpring(s);
      from.addTissueNeighbor(to);
      to.addTissueNeighbor(from);
   }

   private void attachMuscle(Node from, Node to, float len) {
      Spring s = new Spring(from, to, Spring.Type.MUSCLE, len - len * 0.5f, len + len * 0.5f, muscStr, p);
      springs.add(s);
      muscles.add(s);
      world.addSpring(s);
      from.addMuscleNeighbor(to);
      to.addMuscleNeighbor(from);
   }

   public void growBone(Node from, Node to, float len) {
      Spring s = new Spring(from, to, Spring.Type.BONE, len, len, boneStr, p);
      springs.add(s);
      world.addSpring(s);
      from.addBoneNeighbor(to);
      to.addBoneNeighbor(from);
   }

   private String makeStroke() {
      strokeInterval=(int)p.random(minStrokeInterval, maxStrokeInterval);
      String newStroke = "";
      ArrayList<String> actions = new ArrayList<>(1);
      for (int i = 0; i < minStrokeActions || p.random(1) < strokeExtendProb; i++) {
         if (p.random(1) < actionInvolvesNodeProb) {
            int index = (int) (p.random(1) * nodesArray.length);
            actions.add("n " + index + " " + p.random(1) + " ");
            actions.add("n " + index + " " + p.random(1) + " ");
         } else {
            int index = (int) (p.random(1) * springsArray.length);
            actions.add("s " + index + " " + p.random(1) + " ");
            actions.add("s " + index + " " + p.random(1) + " ");
         }
      }
      while (!actions.isEmpty()) {
         newStroke += actions.remove((int) p.random(actions.size()));
      }
      return newStroke;
   }

   public void display() {
      springs.forEach(spring -> spring.display());
      nodes.forEach(node -> node.display());
   }

   public void stroke() {
      if (!strokeScan.hasNext()) {
         strokeScan = new Scanner(strokeBehavior);
      }
      char objType = strokeScan.next().charAt(0);
      int objIndex = Integer.parseInt(strokeScan.next());
      float state = Float.parseFloat(strokeScan.next());

//      System.out.println(objType);
//      System.out.println(objIndex);
//      System.out.println(state);

      switch (objType) {
         case 'n':
            nodesArray[objIndex].setState(state);
            break;
         case 's':
            springsArray[objIndex].changeLength(state);
            break;
      }
   }

   public boolean ccw(float Ax, float Ay, float Bx, float By, float Cx, float Cy) {
      return (Cy - Ay) * (Bx - Ax) > (By - Ay) * (Cx - Ax);
   }

   public boolean intersect(float Ax, float Ay, float Bx, float By, float Cx, float Cy, float Dx, float Dy) {
      return ccw(Ax, Ay, Cx, Cy, Dx, Dy) != ccw(Bx, By, Cx, Cy, Dx, Dy) && ccw(Ax, Ay, Bx, By, Cx, Cy) != ccw(Ax, Ay, Bx, By, Dx, Dy);
   }

   public boolean intersect(float ax, float ay, float bx, float by, Spring s) {
      return intersect(ax, ay, bx, by, s.a.x, s.a.y, s.b.x, s.b.y);
   }

   private boolean springsDoIntersect(float ax, float ay, float bx, float by) {
      Iterator<Spring> i = springs.iterator();
      while (i.hasNext()) {
         Spring s = i.next();
         if (intersect(ax, ay, bx, by, s)) {
            return true;
         }
      }
      return false;
   }

}
