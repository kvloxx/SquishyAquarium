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

   private int minNodes = 7;
   private int nodesRange = 5;
   private int maxNodes = minNodes + nodesRange;

   private float minBoneLength = 50;
   private float maxBoneLength = 75;
   private float branchingProb = 0.5f;

   private int minStrokeActions = 10;
   private float strokeExtendProb = 0.5f;
   private float actionInvolvesNodeProb = 0.5f;

   private float boneStr = 0.9f;
   private float muscStr = 0.2f;
   private float tissStr = 0.01f;

   private PApplet p;
   private VerletPhysics2D world;

   private Set<Node> nodes;
   private Set<Spring> springs;
   private Set<Spring> muscles;
   Node head;

   private Node[] nodesArray;
   private Spring[] musclesArray;

   private int maxStrokeInterval = 10;
   private int minStrokeInterval = 2;
   int strokeInterval;

   private List<StrokeAction> stroke;
   private int currentStrokeAction;


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
      this.musclesArray = muscles.toArray(new Spring[0]);

      this.stroke = makeStroke();
      assignSubtreeStrokes(head);
      this.currentStrokeAction = 0;
//      System.out.println(encodeBody());
   }

   SquishyBody(PApplet p, VerletPhysics2D world, String code) {
      this.p = p;
      this.world = world;
      this.nodes = new LinkedHashSet<>();
      this.springs = new LinkedHashSet<>();
      this.muscles = new LinkedHashSet<>();
      HashMap<String, Node> newNodes = new HashMap<>();
      HashMap<String, Spring> newSprings = new HashMap<>();

      Scanner scan = new Scanner(code);
      System.out.println(scan.next());   //"NODES"
      String s;
      while (!(s = scan.next()).equals("SPRINGS")) {
         Node n = new Node(p);
         newNodes.put(s, n);
         n.x = Float.parseFloat(scan.next());
         n.y = Float.parseFloat(scan.next());
         n.w = Float.parseFloat(scan.next());
         n.changeState(Float.parseFloat(scan.next()));
         n.isHead = Boolean.parseBoolean(scan.next());
         n.fill = Integer.parseInt(scan.next());
         n.d = Float.parseFloat(scan.next());
         n.subtreeStroke = new ArrayList<>();
         n.subStrokeStrList = new ArrayList<>();
         while (!(s = scan.next()).equals("_END_")) {
            n.subStrokeStrList.add(s);
         }
         nodes.add(n);
         world.addParticle(n);
      }
      while (true) {
         s = scan.next();
         if (s.equals("DATA")) {
            break;
         }
         Spring sp = connect(
               Spring.Type.valueOf(scan.next()),
               newNodes.get(scan.next()),
               newNodes.get(scan.next()),
               Float.parseFloat(scan.next()));
         newSprings.put(s, sp);
      }
      this.minNodes = Integer.parseInt(scan.next());
      this.nodesRange = Integer.parseInt(scan.next());
      this.maxNodes = Integer.parseInt(scan.next());
      this.minBoneLength = Float.parseFloat(scan.next());
      this.maxBoneLength = Float.parseFloat(scan.next());
      this.branchingProb = Float.parseFloat(scan.next());
      this.minStrokeActions = Integer.parseInt(scan.next());
      this.strokeExtendProb = Float.parseFloat(scan.next());
      this.actionInvolvesNodeProb = Float.parseFloat(scan.next());
      this.boneStr = Float.parseFloat(scan.next());
      this.muscStr = Float.parseFloat(scan.next());
      this.tissStr = Float.parseFloat(scan.next());
      this.maxStrokeInterval = Integer.parseInt(scan.next());
      this.minStrokeInterval = Integer.parseInt(scan.next());
      this.strokeInterval = Integer.parseInt(scan.next());

      nodes.forEach(node -> {
         if (node.isHead) {
            this.head = node;
         }
         for (int i = 0; i + 2 < node.subStrokeStrList.size(); i += 3) {
            node.subtreeStroke.add(
                  new StrokeAction(
                        (node.subStrokeStrList.get(i).equals("n") ?
                              newNodes.get(node.subStrokeStrList.get(i + 1)) :
                              newSprings.get(node.subStrokeStrList.get(i + 1))),
                        Float.parseFloat(node.subStrokeStrList.get(i + 2))));
         }
      });
      this.stroke = head.subtreeStroke;
      scan.close();
   }

   public SquishyBody(PApplet p, VerletPhysics2D world) {
      this(p, world, p.width / 2.0f, p.height / 2.0f);
   }

   public SquishyBody(Set<Node> nodes, Set<Spring> springs, PApplet p, VerletPhysics2D world) {
      this.p = p;
      this.world = world;
      this.nodes = nodes;
      this.springs = springs;
      this.muscles = new LinkedHashSet<>();
      this.muscles.addAll(springs.stream().filter(spring -> spring.type == Spring.Type.MUSCLE).collect(Collectors.toList()));
      Iterator<Node> nodeIterator = nodes.iterator();
      head = nodeIterator.next();
      head.isHead = true;
      head.boneParent = null;
      this.nodesArray = nodes.toArray(new Node[0]);
      this.musclesArray = muscles.toArray(new Spring[0]);
      this.stroke = makeStroke();
      assignSubtreeStrokes(head);
      this.currentStrokeAction = 0;

   }

   public String stringify() {
      StringBuilder sb = new StringBuilder();
      sb.append("[\n#nodes ")
            .append(nodes.size())
            .append("\n#springs ")
            .append(springs.size())
            .append("\n#strokes ")
            .append(stroke.size())
            .append("\nstrength [")
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
            .append(head.stringifySubtree(true))
            .append("\n]");

      return sb.toString();
   }

   public String encodeBody() {
      StringBuilder sb = new StringBuilder();
      sb.append("NODES \n");
      nodes.forEach(node -> {
         sb.append(node.toString() + "\n");
      });
      sb.append("SPRINGS \n");
      springs.forEach(spring -> {
         sb.append(spring.toString() + "\n");
      });
      sb.append("DATA \n");
      sb.append(" " + minNodes + " ");
      sb.append(" " + nodesRange + " ");
      sb.append(" " + maxNodes + " ");
      sb.append(" " + minBoneLength + " ");
      sb.append(" " + maxBoneLength + " ");
      sb.append(" " + branchingProb + " ");
      sb.append(" " + minStrokeActions + " ");
      sb.append(" " + strokeExtendProb + " ");
      sb.append(" " + actionInvolvesNodeProb + " ");
      sb.append(" " + boneStr + " ");
      sb.append(" " + muscStr + " ");
      sb.append(" " + tissStr + " ");
      sb.append(" " + maxStrokeInterval + " ");
      sb.append(" " + minStrokeInterval + " ");
      sb.append(" " + strokeInterval + " ");
      sb.append(" " + head.stringifySubtreeStroke() + " ");

      return sb.toString();
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
               connect(Spring.Type.BONE, conn, unconn, r);
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
               connect(Spring.Type.MUSCLE,
                     bonyNeighborsArr[i],
                     bonyNeighborsArr[j],
                     bonyNeighborsArr[i].distanceTo(bonyNeighborsArr[j]));
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

/*   private void connectTissue(Node from, Node to, float len) {
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

   public void growBone(Node parent, Node child, float len) {

      springs.add(s);
      world.addSpring(s);
      parent.addBoneChild(child);
      child.addBoneParent(parent);
   }*/

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
      if (stroke != null && stroke.get(currentStrokeAction) != null) {
         stroke.get(currentStrokeAction).executeAction();
         currentStrokeAction = (currentStrokeAction + 1) % stroke.size();
      }
   }

//   public SquishyBody mateWith(SquishyBody mate) {
//      SquishyBody ret = new SquishyBody(this.world);
//   }

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
