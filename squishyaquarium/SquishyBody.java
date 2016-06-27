package squishyaquarium;

import processing.core.PApplet;
import toxi.physics2d.VerletPhysics2D;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by kvloxx
 */
public class SquishyBody {
   //   @formatter:off
   private int minNodes                 = 7;
   private int nodesRange               = 5;
   private int maxNodes                 = minNodes + nodesRange;

   private float minBoneLength          = 25;
   private float maxBoneLength          = 75;
   private float branchingProb          = 0.5f;

   private int minStrokeActions         = 10;
   private float strokeExtendProb       = 0.5f;
   private float actionInvolvesNodeProb = 0.5f;

   private float boneStr                = 0.9f;
   private float muscStr                = 0.2f;
   private float tissStr                = 0.01f;

   private PApplet p;
   private VerletPhysics2D world;

   private Set<Node> nodes;
   private Set<Spring> springs;
   private Set<Spring> muscles;
   Node head;

   private Node[] nodesArray;
   private Spring[] musclesArray;

   private int maxStrokeInterval        = 10;
   private int minStrokeInterval        = 2;
   int strokeInterval;

   private List<StrokeAction> stroke;
   private int currentStrokeAction;
   public Tree tree;
//   @formatter:on

   public void setConstants(float boneStr, float muscStr, float tissStr, float minBoneLength, float maxBoneLength,
                            float branchingProb, float strokeExtendProb, float actionInvolvesNodeProb, int interval) {
      this.boneStr = boneStr;
      this.muscStr = muscStr;
      this.tissStr = tissStr;
      this.minBoneLength = minBoneLength;
      this.maxBoneLength = maxBoneLength;
      this.branchingProb = branchingProb;
      this.strokeExtendProb = strokeExtendProb;
      this.actionInvolvesNodeProb = actionInvolvesNodeProb;
      this.strokeInterval = interval;
   }

   public static SquishyBody[] mate(SquishyBody mate1, SquishyBody mate2, PApplet p, VerletPhysics2D world) {
      Spring bone1 = mate1.getRandomBone();
      Spring bone2 = mate2.getRandomBone();
//      Set<Node> limb1Nodes = bone1.getB().getSubtreeNodes();
//      Set<Node> body1Nodes = mate1.nodes
//            .parallelStream()
//            .filter(node -> !limb1Nodes.contains(node))
//            .collect(Collectors.toCollection(LinkedHashSet::new));
//
//      String limb1String = bone1.getB().stringifySubtreeBonesAndMuscle(mate1.getStrokeSubset(limb1Nodes), null);
//      String body1String = mate1.head.stringifySubtree(body1Nodes, mate1.getBonesAndMuscles(limb1Nodes), mate1
//            .getStrokeSubset(body1Nodes));

      Tree limb1 = mate1.tree.copySubtreeAt(bone1.getB(), p, world);
//      Tree body1 = mate1.tree.copyTreeMinusBranchAt(bone1.getB(), p, world);
      System.out.println("limb1String = \n" + limb1.toString());
//      System.out.println("body1String = \n" + body1.toString());

//      Tree limb2 = mate2.tree.copySubtreeAt(bone2.getB(), p, world);
//      Tree body2 = mate2.tree.copyTreeMinusBranchAt(bone2.getB(), p, world);
//      System.out.println("limb2String = \n" + limb2.toString());
//      System.out.println("body2String = \n" + body2.toString());

//      SquishyBody[] mates = {new SquishyBody(limb1, p, world), new SquishyBody(body1, p, world)};
      SquishyBody[] mates = {new SquishyBody(limb1, p, world)};

//      mates = {};
      System.out.println("limb1 = " + limb1);
//      System.out.println("body1 = " + body1);

      bone1.getA().d = 20;
      bone1.getB().d = 20;

      return mates;

//      SquishyBody offspring1 = copySkeleton(mate1, p, world);
//      SquishyBody offspring2 = copySkeleton(mate2, p, world);
//
//      world.removeSpring(bone1);
//      world.removeSpring(bone2);
//      offspring1.springs.remove(bone1);
//      offspring2.springs.remove(bone2);
//      Set<Node> subNodes1 = bone1.getB().getSubtreeNodes();
//      Set<Node> subNodes2 = bone2.getB().getSubtreeNodes();
//      offspring1.nodes.removeAll(subNodes1);
//      offspring2.nodes.removeAll(subNodes2);
//      bone1.getA().boneChildren.remove(bone1.getB());
//      bone1.getA().neighbors.remove(bone1.getB());
//      bone2.getA().boneChildren.remove(bone2.getB());
//      bone2.getA().neighbors.remove(bone2.getB());
//      bone1.getB().boneParent = null;
//      bone1.getB().neighbors.remove(bone1.getA());
//      bone2.getB().boneParent = null;
//      bone2.getB().neighbors.remove(bone2.getA());
//
//      Node b2=bone2.getB();
//      Node a2=bone2.getA();
//      Vec2D span2 = b2.sub(a2);
//      Node a1=bone1.getA();
//      Vec2D newLoc = a1.add(span2);
//      Vec2D diff = newLoc.sub(a2);
//      for (Node node : subNodes2) {
//         node.set(node.normal.add(diff));
//         node.recordNormal();
//         offspring1.nodes.add(node);
//      }
//
//      offspring1.connect(Spring.Type.BONE, bone1.getA(), bone2.getB(), a1.distanceTo(b2));
//      Node b1=bone1.getB();
////      Vec2D span1 = b1.sub(a1);
////      newLoc = a2.add(span1);
////      diff = newLoc.sub(a1);
////      for (Node node : subNodes2) {
////         node.set(node.normal.add(diff));
////         node.recordNormal();
////         offspring2.nodes.add(node);
////      }
//      offspring2.connect(Spring.Type.BONE, bone2.getA(), bone1.getB(), a2.distanceTo(b1));
//      offspring2.nodes.addAll(subNodes1);
//      for (Node node : offspring1.nodes) {
//         node.scaleVelocity(0);
//         node.fill = p.color(00, 200, 100);
//      }
//      for (Node node : offspring2.nodes) {
//         node.scaleVelocity(0);
//         node.fill = p.color(200, 100, 50);
//      }
//
//      bone1.getA().fill = p.color(200, 0, 0);
//      bone1.getB().fill = p.color(200, 0, 0);
//      bone2.getA().fill = p.color(0, 0, 200);
//      bone2.getB().fill = p.color(0, 0, 200);
//
//      offspring1.fleshOut(offspring1.nodes);
//      offspring2.fleshOut(offspring2.nodes);
//
//      SquishyBody[] ret = {offspring1, offspring2};
//      return ret;
   }
/*
   public static SquishyBody[] mate(SquishyBody mate1, SquishyBody mate2, PApplet p, VerletPhysics2D world) {
      SquishyBody offspring1 = copySkeleton(mate1, p, world);
      SquishyBody offspring2 = copySkeleton(mate2, p, world);
      Spring bone1 = offspring1.getRandomBone();
      Spring bone2 = offspring2.getRandomBone();

      world.removeSpring(bone1);
      world.removeSpring(bone2);
      offspring1.springs.remove(bone1);
      offspring2.springs.remove(bone2);
      Set<Node> subNodes1 = bone1.getB().getSubtreeNodes();
      Set<Node> subNodes2 = bone2.getB().getSubtreeNodes();
      offspring1.nodes.removeAll(subNodes1);
      offspring2.nodes.removeAll(subNodes2);
      bone1.getA().boneChildren.remove(bone1.getB());
      bone1.getA().neighbors.remove(bone1.getB());
      bone2.getA().boneChildren.remove(bone2.getB());
      bone2.getA().neighbors.remove(bone2.getB());
      bone1.getB().boneParent = null;
      bone1.getB().neighbors.remove(bone1.getA());
      bone2.getB().boneParent = null;
      bone2.getB().neighbors.remove(bone2.getA());

      Node b2=bone2.getB();
      Node a2=bone2.getA();
      Vec2D span2 = b2.sub(a2);
      Node a1=bone1.getA();
      Vec2D newLoc = a1.add(span2);
      Vec2D diff = newLoc.sub(a2);
      for (Node node : subNodes2) {
         node.set(node.normal.add(diff));
         node.recordNormal();
         offspring1.nodes.add(node);
      }

      offspring1.connect(Spring.Type.BONE, bone1.getA(), bone2.getB(), a1.distanceTo(b2));
      Node b1=bone1.getB();
//      Vec2D span1 = b1.sub(a1);
//      newLoc = a2.add(span1);
//      diff = newLoc.sub(a1);
//      for (Node node : subNodes2) {
//         node.set(node.normal.add(diff));
//         node.recordNormal();
//         offspring2.nodes.add(node);
//      }
      offspring2.connect(Spring.Type.BONE, bone2.getA(), bone1.getB(), a2.distanceTo(b1));
      offspring2.nodes.addAll(subNodes1);
      for (Node node : offspring1.nodes) {
         node.scaleVelocity(0);
         node.fill = p.color(00, 200, 100);
      }
      for (Node node : offspring2.nodes) {
         node.scaleVelocity(0);
         node.fill = p.color(200, 100, 50);
      }

      bone1.getA().fill = p.color(200, 0, 0);
      bone1.getB().fill = p.color(200, 0, 0);
      bone2.getA().fill = p.color(0, 0, 200);
      bone2.getB().fill = p.color(0, 0, 200);

      offspring1.fleshOut(offspring1.nodes);
      offspring2.fleshOut(offspring2.nodes);

      SquishyBody[] ret = {offspring1, offspring2};
      return ret;
   }
*/

//   public static SquishyBody copySkeleton(SquishyBody orig, PApplet p, VerletPhysics2D world) {
//      String skeleString = orig.head.stringifySubtreeSkeleton();
//      SquishyBody ret = new TreeParser(skeleString, p, world).parseSubtree();
//     /* System.out.println("------------Parsed Result------------");
//      System.out.println(ret.stringify());
//      System.out.println("------------End Parsed Result------------");*/
//      return ret;
//   }

   public SquishyBody(Tree tree, PApplet p, VerletPhysics2D world) {
      this(tree.nodes, tree.springs, tree.stroke, p, world);
   }

   public SquishyBody(PApplet p, VerletPhysics2D world, float headX, float headY) {
      this.p = p;
      this.world = world;
      this.nodes = new LinkedHashSet<>();
      this.springs = new LinkedHashSet<>();
      this.muscles = new LinkedHashSet<>();

//      System.out.println("growin nodes.");
      growNodes(nodes);
//      System.out.println("Dun growin nodes.");
      head.x = headX;
      head.y = headY;
      System.out.println("ossify start");
      ossify(nodes);
      System.out.println("ossify ended");
//      System.out.println("start 2 fleshout");
      fleshOut(nodes);
//      System.out.println("and done fleshing out.");

//      System.out.println("scaling velocity");
      nodes.forEach(node -> node.scaleVelocity(0));
//      System.out.println("done scaling.");

      this.nodesArray = nodes.toArray(new Node[0]);
      this.musclesArray = muscles.toArray(new Spring[0]);

      System.out.println("calling make stroke");
      this.stroke = makeStroke();
//      System.out.println("make stroke exited");
      assignSubtreeStrokes(head);
      this.currentStrokeAction = 0;
//      System.out.println("make the tree ");
      this.tree = new Tree(head, nodes, springs, stroke);
//      System.out.println("now it's done.");
   }

   public static SquishyBody parse(String code, PApplet p, VerletPhysics2D world) {
      TreeParser tp = new TreeParser(code, p, world);
      return tp.parseSquishyBody();
   }

   public SquishyBody(PApplet p, VerletPhysics2D world) {
      this(p, world, p.width / 2.0f, p.height / 2.0f);
   }

   public SquishyBody(Set<Node> nodes, Set<Spring> springs, PApplet p, VerletPhysics2D world) {
      this(nodes, springs, null, p, world);
   }

   public SquishyBody(Set<Node> nodes, Set<Spring> springs, List<StrokeAction> stroke, PApplet p, VerletPhysics2D
         world) {
      this.p = p;
      this.world = world;
      this.nodes = nodes;
      this.springs = springs;
      this.muscles = new LinkedHashSet<>();
      this.muscles.addAll(springs
            .stream()
            .filter(Spring::isMuscle)
            .collect(Collectors.toList()));
      Iterator<Node> nodeIterator = nodes.iterator();
      head = nodeIterator.next();
      head.isHead = true;
      head.boneParent = null;
      this.nodesArray = nodes.toArray(new Node[0]);
      this.musclesArray = muscles.toArray(new Spring[0]);
      if (stroke != null) {
         this.stroke = stroke;
      } else {
         this.stroke = makeStroke();
         assignSubtreeStrokes(head);
      }
      this.strokeInterval = ((int) p.random(minStrokeInterval, maxStrokeInterval));
      this.currentStrokeAction = 0;
      this.tree = new Tree(head, nodes, springs, stroke);
   }

   public String stringify() {
      return new StringBuilder()
            .append("{\n#nodes ")
            .append(nodes.size())
            .append("\n#springs ")
            .append(springs.size())
            .append("\n#strokes ")
            .append(stroke.size())
            .append("\nstrength [ ")
            .append(boneStr)
            .append(" ")
            .append(muscStr)
            .append(" ")
            .append(tissStr)
            .append(" ]\nbone-length [ ")
            .append(minBoneLength)
            .append(" ")
            .append(maxBoneLength)
            .append(" ]\nbranching-prob ")
            .append(branchingProb)
            .append("\nstroke-extend-prob ")
            .append(strokeExtendProb)
            .append("\ninvolves-node-prob ")
            .append(actionInvolvesNodeProb)
            .append("\nstroke-interval ")
            .append(strokeInterval)
            .append("\n")
            .append(tree.toString())
            .append("\n}")
            .toString();
   }

   private void growNodes(Set<Node> nodes) {
      int numNodes = (int) (p.random(minNodes, maxNodes + 1)); //range: [minNodes, maxNodes]
      int fill = p.color(
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
//      System.out.println("inside ossify");
      PriorityQueue<Node> unconnected = new PriorityQueue<>(nodes);
      PriorityQueue<Node> connected = new PriorityQueue<>();

//      System.out.println("con.add head");
      connected.add(head);
//      System.out.println("done. now uncon.remove(head)");
      unconnected.remove(head);
//      System.out.println("done removing. now recording normal");
      head.recordNormal();
//      System.out.println("normal recorded. now we start the while loop.");

      while (!connected.isEmpty()) {
//      System.out.println("entering while loop. connected is not empty. State: "+connected);
         Node conn = connected.remove();
//         System.out.println("removed node " + conn);
         if (!unconnected.isEmpty()) {
//            System.out.println("uncon isn't empty. state: " + unconnected);
            do {
//            System.out.println("begin do loop.");
               Node unconn = unconnected.remove();
//            System.out.println("removed "+unconn+" from set unconnected");
               float r = p.random(minBoneLength, maxBoneLength);
//            System.out.println("random r="+r);
               int count = 0;
               do {
            System.out.println("enter sub do loop");
                  float theta = p.random(p.PI * 2);
//            System.out.println("random theta = "+theta);
                  unconn.x = conn.x + r * p.cos(theta);
                  unconn.y = conn.y + r * p.sin(theta);
                  count++;
               } while (springsDoIntersect(unconn.x, unconn.y, conn.x, conn.y) && count <20);
               System.out.println("count was "+count);
               connect(Spring.Type.BONE, conn, unconn, r);
               connected.add(unconn);
               unconn.recordNormal();
            } while (Math.random() <= branchingProb && !unconnected.isEmpty());
         }
      }
   }

   private void fleshOut(Set<Node> nodes) {
      nodes.forEach(thisNode -> {
         Node[] boneChildrenArray = thisNode.boneChildren.toArray(new Node[0]);
         for (int i = 0; i < boneChildrenArray.length; i++) {
            Node daddy = thisNode.boneParent;
            if (daddy != null) {
               connect(Spring.Type.MUSCLE, daddy, boneChildrenArray[i], daddy.distanceTo(boneChildrenArray[i]));
            }
            for (int j = 0; j < i; j++) {
               connect(Spring.Type.MUSCLE,
                     boneChildrenArray[i],
                     boneChildrenArray[j],
                     boneChildrenArray[i].distanceTo(boneChildrenArray[j]));
            }
         }
      });
      nodes.forEach(thisNode ->
            nodes.forEach(thatNode -> {
               if (thisNode != thatNode) {
                  if (!thisNode.neighbors.contains(thatNode)) {
                     connect(Spring.Type.TISSUE,
                           thisNode,
                           thatNode,
                           thisNode.distanceTo(thatNode));
                  }
               }
            })
      );
   }

   private Spring connect(Spring.Type type, Node parent, Node child, float len) {
      Spring s = null;
      switch (type) {
         case BONE:
            s = new Spring(parent, child, Spring.Type.BONE, len, len, boneStr, p);
            parent.addBoneChild(child);
            child.addBoneParent(parent);
            parent.attachSpring(s);
            break;
         case MUSCLE:
            s = new Spring(parent, child, Spring.Type.MUSCLE, len - len * 0.5f, len + len * 0.5f, muscStr, p);
            muscles.add(s);
            parent.addMuscleNeighbor(child);
            parent.attachSpring(s);
            child.addMuscleNeighbor(parent);
            child.attachSpring(s);
            break;
         case TISSUE:
            s = new Spring(parent, child, Spring.Type.TISSUE, len, len, tissStr, p);
            parent.addTissueNeighbor(child);
            parent.attachSpring(s);
            child.addTissueNeighbor(parent);
            child.attachSpring(s);
            break;
      }
      springs.add(s);
      world.addSpring(s);
      return s;
   }

   private ArrayList<StrokeAction> makeStroke() {
      System.out.println("making stroke");
      strokeInterval = (int) p.random(minStrokeInterval, maxStrokeInterval);
      ArrayList<StrokeAction> tmpActions = new ArrayList<>();
      for (int i = 0; i < minStrokeActions || p.random(1) < strokeExtendProb; i++) {
         if (p.random(1) < actionInvolvesNodeProb) {
            if (nodesArray.length == 0) {
               break;
            }
            int index = (int) (p.random(nodesArray.length));
            for (int j = 0; j < 2 || p.random(1) < strokeExtendProb; j++) {   //at least 2 actions per body part
               tmpActions.add(new StrokeAction(nodesArray[index], p.random(1)));
            }
         } else {
            if (musclesArray.length == 0) {
               break;
            }
            int index = (int) (p.random(musclesArray.length));
            for (int j = 0; j < 2 || p.random(1) < strokeExtendProb; j++) {
               tmpActions.add(new StrokeAction(musclesArray[index], p.random(1)));
            }
         }
      }

      /*Future self-- we're doing this part to randomize the order of all actions in the
      * stroke. Obviously this is not the most efficient way to perform this operation,
      * but it saves us the hassle of shuffling a list in place and only needs
      * to be performed once for the lifetime of any creature, so the impact is fairly minimal.*/
      ArrayList<StrokeAction> retActions = new ArrayList<>();
      while (!tmpActions.isEmpty()) {
         retActions.add(tmpActions.remove((int) p.random(tmpActions.size())));
      }

//      System.out.println("done making stroke");
      return retActions;
   }

   public void assignSubtreeStrokes(Node n) {
      if (n.boneParent == null) {
         n.subtreeStroke = stroke;
      } else {
         stroke.forEach(action -> {
            if (action.isNode()) {
               if (action.obj == n || n.boneChildren.contains(action.obj)) {
                  n.subtreeStroke.add(action);
               }
            } else {
               Node nodeA = (Node) (((Spring) (action.obj)).a);
               Node nodeB = (Node) (((Spring) (action.obj)).b);
               if ((n == nodeA || n.boneChildren.contains(nodeA)) &&
                     (n == nodeB || n.boneChildren.contains(nodeB))) {
                  n.subtreeStroke.add(action);
               }
            }
         });
      }
      if (n.boneChildren != null) {
         n.boneChildren.forEach(boneChild -> {
            assignSubtreeStrokes(boneChild);
         });
      }
   }

   public void display() {
      springs.forEach(Spring::display);
      nodes.forEach(Node::display);
   }

   public void stroke() {
      if (stroke != null && stroke.size() > currentStrokeAction && stroke.get(currentStrokeAction) != null) {
         stroke.get(currentStrokeAction).executeAction();
         currentStrokeAction = (currentStrokeAction + 1) % stroke.size();
      }
   }

   public Set<Spring> getBonesAndMuscles(Set<Node> dontInclude) {
      return springs
            .parallelStream()
            .filter(spring -> spring.isBone() || spring.isMuscle())
            .filter(spring -> !(dontInclude.contains(spring.getA()) || dontInclude.contains(spring.getB())))
            .collect(Collectors.toCollection(LinkedHashSet::new));
   }

   public List<Spring> getBones() {
      return springs
            .parallelStream()
            .filter(Spring::isBone)
            .collect(Collectors.toCollection(ArrayList::new));
   }

   public Spring getRandomBone() {
      ArrayList<Spring> s = springs.parallelStream().filter(Spring::isBone).collect(Collectors.toCollection
            (ArrayList::new));
      return s.get(((int) p.random(s.size())));
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
