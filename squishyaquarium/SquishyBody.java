package squishyaquarium;

import processing.core.PApplet;
import processing.core.PConstants;
import toxi.geom.Vec2D;
import toxi.physics2d.VerletSpring2D;

import java.util.*;

/**
 * Created by kvloxx
 */
public class SquishyBody extends Tree {
   public Vec2D normalCOM;
   /**
    * Center of mass- weighted average of all node positions
    */
   public Vec2D COM;
   /**
    * Vector whose direction points from <tt>COM</tt> to <tt>root</tt>
    */
   public Vec2D headingVec;
   /**
    * Sum of all node weights
    */
   public float M = 0;
   public float normalM = 0;
   public float rotation = 0;
   float normalHeading;
   //@formatter:off
   int minNodes                  = 7;
   int nodesRange                = 5;
   int maxNodes                  = minNodes + nodesRange;
   float minBoneLength           = 25;
   float maxBoneLength           = 75;
   float branchingProb           = 0.4f;
   int minStrokeActions          = 10;
   float strokeExtendProb        = 0.5f;
   float actionInvolvesNodeProb  = 0.4f;
   //@formatter:on
   float boneStr = 0.9f;
   float muscStr = 0.2f;
   float tissStr = 0.01f;
   int maxStrokeInterval = 10;
   int minStrokeInterval = 2;
   private Vec2D maxDisp = new Vec2D(0, 0);
   private float maxDist = 0;
   private float maxRotation = 0;
   private Vec2D normalHeadingVec;
   private Vec2D disp = new Vec2D(0, 0);
   private boolean angB = true;
   private boolean angR = true;
   private int revs = 0;
   private boolean showDebug = false;

   public SquishyBody(Tree tree, PApplet p, World world) {
      this(tree.nodes, tree.springs, tree.stroke, p, world);
   }

   public SquishyBody(PApplet p, World world, float headX, float headY) {
      this(p, world, headX, headY, p.color(p.random(256), p.random(256), p.random(256)));
   }

   public SquishyBody(PApplet p, World world, float headX, float headY, int fill) {
      super(p, world);

      setAsRoot(growNodes(nodes, fill));
      root.x = headX;
      root.y = headY;
      root.recordNormal();

      springs.addAll(ossify(nodes));
      springs.addAll(fleshOut(nodes));
      stroke = makeRandomStroke();

      computeNormals();
      this.COM = new Vec2D(normalCOM.x, normalCOM.y);
      this.headingVec = root.sub(COM);
      this.normalHeadingVec = headingVec;
      this.normalHeading = headingVec.heading();

   }

   public SquishyBody(PApplet p, World world) {
      this(p, world, p.width / 2.0f, p.height / 2.0f);
   }

   public SquishyBody(PApplet p, World world, int fill) {
      this(p, world, p.width / 2.0f, p.height / 2.0f, fill);
   }

   public SquishyBody(Set<Node> nodes, Set<Spring> springs, PApplet p, World world) {
      this(nodes, springs, null, p, world);
   }

   public SquishyBody(Set<Node> nodes, Set<Spring> springs, Stroke stroke, PApplet p, World
         world) {
      super(p, world);
      this.nodes = nodes;
      this.springs = springs;

      setAsRoot(nodes.iterator().next());

      if (stroke != null) {
         this.stroke = stroke;
      } else {
         this.stroke = makeRandomStroke();
      }
      computeNormals();
      this.COM = new Vec2D(normalCOM.x, normalCOM.y);
      this.headingVec = root.sub(COM);
      this.normalHeadingVec = headingVec;
      this.normalHeading = headingVec.heading();
   }

   public static SquishyBody[] mate(SquishyBody mate1, SquishyBody mate2, PApplet p, World world) {
      SquishyBody offspring1 = mate1.copy();
      SquishyBody offspring2 = mate2.copy();
      Node n1 = offspring1.getRandomNode(false);
      Node n2 = offspring2.getRandomNode(false);
      Node n1Parent = n1.boneParent;
      Node n2Parent = n2.boneParent;
      Tree limb1 = offspring1.splitAbove(n1);
      Tree limb2 = offspring2.splitAbove(n2);

      offspring1.graft(limb2, n1Parent, 20);
      offspring2.graft(limb1, n2Parent, 20);

      SquishyBody[] ret = {offspring1, offspring2};
      return ret;
   }

   //Returns body in trees[0], limb in trees[1]
   private static Tree[] breakBoneAndCopySubtrees(SquishyBody mate, PApplet p, World world) {
      Spring bone = mate.getRandomSpring(Spring::isBone);
      Tree limb = mate.copySubtreeAt(bone.getB());
      Tree body = mate.copyTreeMinusBranchAt(bone.getB());
      Tree[] ret = {body, limb};
      return ret;
   }

   public static SquishyBody parse(String code, PApplet p, World world) {
      TreeParser tp = new TreeParser(code, p, world);
      return tp.parseSquishyBody(false);
   }

   private void computeNormals() {
      normalCOM = new Vec2D(0, 0);
      normalM = 0;
      float m;
      for (Node node : nodes) {
         normalCOM.addSelf(node.normal.scale((m = node.getWeight())));
         normalM += m;
      }
      normalCOM.scaleSelf(1 / normalM);
   }

   public SquishyBody copy() {
      return new TreeParser(stringify(), p, world).parseSquishyBody(false);
   }

   public void setConstants(float boneStr, float muscStr, float tissStr, float minBoneLength, float maxBoneLength,
                            float branchingProb, float strokeExtendProb, float actionInvolvesNodeProb) {
      this.boneStr = boneStr;
      this.muscStr = muscStr;
      this.tissStr = tissStr;
      this.minBoneLength = minBoneLength;
      this.maxBoneLength = maxBoneLength;
      this.branchingProb = branchingProb;
      this.strokeExtendProb = strokeExtendProb;
      this.actionInvolvesNodeProb = actionInvolvesNodeProb;
   }

   void calm() {
      nodes.forEach(node -> node.scaleVelocity(0));
   }

   Node getRandomNode() {
      return getRandomNode(true);
   }

   Node getRandomNode(boolean includeRoot) {
      ArrayList<Node> nerds = new ArrayList<>(nodes);
      if (!includeRoot) {
         nerds.remove(root);
      }
      return nerds.get(((int) p.random(nerds.size())));
   }

   public void graft(Tree limb, Node position, int settleIterations) {
      nodes.addAll(limb.nodes);
      springs.addAll(limb.getSprings(Spring::isBone));
      float l = p.random(minBoneLength, maxBoneLength);
      Vec2D rootspot = position.normal.add(Vec2D.fromTheta(p.random(PConstants.TWO_PI)).scale(l));
      Vec2D diff = rootspot.sub(limb.root.normal);

      limb.nodes.stream()
            .filter(node -> node != limb.root)
            .forEach(node -> {
               Vec2D diffRoot = node.normal.sub(limb.root.normal);
               node.set(node.normal.add(diff.add(diffRoot)));
               node.recordNormal();
               node.scaleVelocity(0);
            });
      limb.root.set(limb.root.normal.add(diff));
      limb.root.recordNormal();
      limb.root.scaleVelocity(0);

      springs.add(connect(Spring.Type.BONE, position, limb.root, l, boneStr));
      springs.addAll(fleshOut(nodes, settleIterations));
      stroke.blendWith(limb.stroke);
   }

   public String stringify() {
      return new StringBuilder()
            .append("{")
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
            .append("\nexecuteNextStrokeAction-extend-prob ")
            .append(strokeExtendProb)
            .append("\ninvolves-node-prob ")
            .append(actionInvolvesNodeProb)
            .append("\n")
            .append(stringifyTree(null, true))
            .append("\n}")
            .toString();
   }

   //Returns head
   private Node growNodes(Set<Node> nodes, int fill) {
      int numNodes = (int) (p.random(minNodes, maxNodes + 1)); //range: [minNodes, maxNodes]
      Node toBeHead = null;
      for (int i = 0; i < numNodes; i++) {
         Node n = new Node(i, i, fill, p);   //set pos to (i,i) as placeholder so nodes aren't all stacked
         if (i == 0) {
            toBeHead = n;
         }
         nodes.add(n);
         world.addParticle(n);
      }
      return toBeHead;
   }

   private void setAsRoot(Node r) {
      if (r != null) {
         r.isHead = false;
      }
      root = r;
      r.isHead = true;
      r.boneParent = null;
   }

   private Set<Spring> ossify(Set<Node> nodes) {
      Set<Spring> springs = new LinkedHashSet<>();
      PriorityQueue<Node> unconnected = new PriorityQueue<>(nodes);
      PriorityQueue<Node> connected = new PriorityQueue<>();

      connected.add(root);
      unconnected.remove(root);
      root.recordNormal();

      while (!connected.isEmpty()) {
         Node conn = connected.remove();
         conn.scaleVelocity(0);
         if (!unconnected.isEmpty()) {
            do {
               Node unconn = unconnected.remove();
               float r = p.random(minBoneLength, maxBoneLength);
               int count = 0;
               do {
                  float theta = p.random(PConstants.PI * 2);
                  unconn.x = conn.x + r * p.cos(theta);
                  unconn.y = conn.y + r * p.sin(theta);
                  count++;
               } while (Utils.springsDoIntersect(unconn.x, unconn.y, conn.x, conn.y, springs) && count < 20);
               connected.add(unconn);
               unconn.recordNormal();
               springs.add(connectStandard(Spring.Type.BONE, conn, unconn));
            } while (Math.random() <= branchingProb && !unconnected.isEmpty());
         }
      }
      return springs;
   }

   private Set<Spring> fleshOut(Set<Node> nodeSet) {
      return fleshOut(nodeSet, 10);
   }

   private Set<Spring> fleshOut(Set<Node> nodes, int settleIterations) {
      boolean settle = settleIterations > 0;
      Set<Spring> newSprings = new LinkedHashSet<>();
      for (Node thisNode : nodes) {
         Node[] boneChildrenArray = thisNode.boneChildren.toArray(new Node[0]);
         for (int i = 0; i < boneChildrenArray.length; i++) {
            Node daddy = thisNode.boneParent;
            if (daddy != null) {
               Spring s = connectStandard(Spring.Type.MUSCLE, daddy, boneChildrenArray[i]);
               newSprings.add(s);
               if (settle) {
                  s.useAsPositioningForce(minBoneLength + daddy.distanceTo(boneChildrenArray[i]), tissStr);
               }
            }
            for (int j = 0; j < i; j++) {
               Spring s = connectStandard(Spring.Type.MUSCLE, boneChildrenArray[i], boneChildrenArray[j]);
               newSprings.add(s);
               if (settle) {
                  s.useAsPositioningForce(minBoneLength + boneChildrenArray[i].distanceTo(boneChildrenArray[j]), tissStr);
               }
            }
         }
      }
      for (Node thisNode : nodes) {
         for (Node thatNode : nodes) {
            if (thisNode != thatNode) {
               if (!thisNode.neighbors.contains(thatNode)) {
                  Spring s = connectStandard(Spring.Type.TISSUE, thisNode, thatNode);
                  newSprings.add(s);
                  if (settle) {
                     s.useAsPositioningForce(minBoneLength + thatNode.distanceTo(thisNode), tissStr);
                  }
               }
            }
         }
      }
      if (settle) {
         for (Node node : nodes) {
            node.scaleVelocity(0);
         }
         for (int i = 0; i < settleIterations; i++) {
            world.update();
         }
         for (Node node : nodes) {
            node.recordNormal();
            node.scaleVelocity(0);
         }
         for (VerletSpring2D spring : world.springs) {
            ((Spring) spring).setCurrentLengthAsRestLength();
            ((Spring) spring).restore();
         }
      }
      return newSprings;
   }

   void settle(int steps) {
      if (steps <= 0) {
         return;
      }
      for (Spring spring : getSprings(s -> !s.isBone())) {
         spring.useAsPositioningForce(spring.getRestLength() + minBoneLength, tissStr);
      }
      for (; steps >= 0; steps--) {
         world.update();
      }
      for (Node node : nodes) {
         node.recordNormal();
         node.scaleVelocity(0);
      }
      for (VerletSpring2D spring : world.springs) {
         ((Spring) spring).setCurrentLengthAsRestLength();
         ((Spring) spring).restore();
      }
   }

   public void duplicateSubtreeAt(Node node) {
      if (node.boneParent == null) {
         System.out.println("Can't duplicate subtree from root " + node);
         return;
      }
      Vec2D pivot = node.boneParent.normal;
      Tree t = copySubtreeAt(node);
      for (Node tNode : t.nodes) {
         tNode.rotateAbout(pivot, PConstants.HALF_PI);
      }
      graft(t, node.boneParent, 30);
   }

   private Spring connectStandard(Spring.Type type, Node parent, Node child) {
      Spring s = null;
      float l = parent.normal.distanceTo(child.normal);
      switch (type) {
         case BONE:
            s = new Spring(parent, child, Spring.Type.BONE, l, l, boneStr, p);
            break;
         case MUSCLE:
            s = new Spring(parent, child, Spring.Type.MUSCLE, l * 0.5f, l * 1.5f, muscStr, p);
            break;
         case TISSUE:
            s = new Spring(parent, child, Spring.Type.TISSUE, l, l, tissStr, p);
            break;
      }
      s.completeConnection();
      world.addSpring(s);
      return s;
   }

   private Spring connect(Spring.Type type, Node parent, Node child, float len, float strength) {
      Spring s;
      if (type == Spring.Type.MUSCLE) {
         s = new Spring(parent, child, type, len * 0.5f, len * 1.5f, strength, p);
      } else {
         s = new Spring(parent, child, type, len, len, strength, p);
      }
      s.completeConnection();
      world.addSpring(s);
      return s;
   }

   Stroke makeRandomStroke() {
      return new Stroke(makeStrokeActionList(), makeStrokeActionList(), makeStrokeActionList(), (int) p.random(minStrokeInterval, maxStrokeInterval), (int) p.random(minStrokeInterval, maxStrokeInterval), (int) p.random(minStrokeInterval, maxStrokeInterval));
   }

   public void setBehavior(int behavior) {
      stroke.setCurrentBehavior(behavior);
      nodes.forEach(Node::reset);
   }

   List<StrokeAction> makeStrokeActionList(int length) {
      Node[] nodesArray = nodes.toArray(new Node[0]);
      Spring[] musclesArray = getSprings(Spring::isMuscle).toArray(new Spring[0]);
      ArrayList<StrokeAction> tmpActions = new ArrayList<>();
      for (int i = 0; i < length; i++) {
         if (p.random(1) < actionInvolvesNodeProb && nodesArray.length > 0) {
            int index = (int) (p.random(nodesArray.length));
            for (int j = 0; j < 2 && i < length; j++, i++) {   //at least 2 actions per body part
               tmpActions.add(new StrokeAction(nodesArray[index], p.random(1)));
            }
         } else if (musclesArray.length > 0) {
            int index = (int) (p.random(musclesArray.length));
            for (int j = 0; j < 2 && i < length; j++, i++) {   //at least 2 actions per body part
               tmpActions.add(new StrokeAction(musclesArray[index], p.random(1)));
            }
         }
      }
      return tmpActions;
   }

   private List<StrokeAction> makeStrokeActionList() {
      Node[] nodesArray = nodes.toArray(new Node[0]);
      Spring[] musclesArray = getSprings(Spring::isMuscle).toArray(new Spring[0]);

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
      * executeNextStrokeAction. Obviously this is not the most efficient way to perform this operation,
      * but it saves us the hassle of shuffling a list in place and only needs
      * to be performed once for the lifetime of any creature, so the impact is fairly minimal.*/
      ArrayList<StrokeAction> retActions = new ArrayList<>();
      while (!tmpActions.isEmpty()) {
         retActions.add(tmpActions.remove((int) p.random(tmpActions.size())));
      }

      return retActions;
   }

   public void display() {
      for (Spring spring : springs) {
         spring.display();
      }
      for (Node node : nodes) {
         node.display();
      }
      if (showDebug) {
         p.noStroke();
         p.fill(0x88888888);
         if (maxRotation > 0) {
            p.arc(COM.x, COM.y, 40, 40, normalHeading, normalHeading + maxRotation, PConstants.PIE);
         } else {
            p.arc(COM.x, COM.y, 40, 40, normalHeading - PConstants.TWO_PI + maxRotation, normalHeading, PConstants.PIE);

         }
         p.fill(0x88FF18DE);
         if (rotation > 0) {
            p.arc(COM.x, COM.y, 40, 40, normalHeading, normalHeading + Utils.positiveAngleBetween(normalHeadingVec,
                  headingVec), PConstants.PIE);
         } else {
            p.arc(COM.x, COM.y, 40, 40, normalHeading - PConstants.TWO_PI + Utils.positiveAngleBetween(normalHeadingVec,
                  headingVec), normalHeading, PConstants.PIE);

         }

         p.fill(0xffFF18DE);
         p.text(String.format("%.2f", rotation) + " : " + String.format("%.2f", maxRotation), COM.x + 15, COM.y + 30);
         p.stroke(0xff00E414);
         p.fill(0xff00E414);
         p.arc(COM.x, COM.y, 15, 15, 0, PConstants.HALF_PI);
         p.arc(COM.x, COM.y, 15, 15, PConstants.PI, PConstants.HALF_PI * 3);
         p.noFill();
         p.arc(COM.x, COM.y, 15, 15, PConstants.HALF_PI, PConstants.PI);
         p.arc(COM.x, COM.y, 15, 15, PConstants.HALF_PI * 3, PConstants.TWO_PI);
         p.text(String.format("%.2f", M) + " : " + String.format("%.2f", root.getWeight()) + " : " + String.format("%.2f",
               maxDist), COM.x + 15, COM.y + 10);
         p.stroke(0xffFF18DE);
         p.line(root.x, root.y, root.x + headingVec.x, root.y + headingVec.y);
         p.stroke(0xff0e829f);
         p.fill(0xff0e829f);
         p.text(String.format("%.2f", ((float) stroke.getCurrentBehavior())), COM.x +
               15, COM.y + 50);
         p.line(root.x, root.y, root.x + normalHeadingVec.x, root.y + normalHeadingVec.y);
         p.stroke(0xff00ffff);
         Vec2D disp = COM.sub(normalCOM).getNormalizedTo(maxDist);
         float a, b, c, d, e, f;
         p.line(a = normalCOM.x, b = normalCOM.y, c = a + disp.x, d = b + disp.y);
         p.stroke(100);
         p.line(c, d, COM.x, COM.y);
      }
   }

   @Override
   public String toString() {
      return stringify();
   }

   public void addGrowthTo(Node node) {
      Vec2D v = node.normal.add(Vec2D.fromTheta(p.random(p.TWO_PI)).scale(p.random(minBoneLength, maxBoneLength)));
      Node newNode = new Node(v.x, v.y, p);
      world.addParticle(newNode);
      Set<Node> newSet = new LinkedHashSet<>();
      newSet.add(newNode);
      graft(new Tree(newNode, newSet, new LinkedHashSet<>(), null, p, world), node, 0);
   }

   public void randomlyShiftSubtreeAt(Node subtreeRoot) {
      Tree subtree = splitAbove(subtreeRoot);
      Node newPosition = getRandomNode();
      graft(subtree, newPosition, 10);
   }

   /**
    * Display center of mass and headingVec
    */
   public void showDebug() {
      showDebug = true;
   }

   /**
    * Hide center of mass and headingVec
    */
   public void hideDebug() {
      showDebug = false;
   }

   void updateHeading() {
      headingVec = root.sub(COM);
   }

   public void update() {
      updateHeading();
      recordDisplacementAndRotation();
      stroke.strokeIfReady(p.frameCount);
   }

   void recordDisplacementAndRotation() {
      disp.set(COM.sub(normalCOM));
      float m;
      if ((m = disp.magnitude()) > maxDist) {
         maxDist = m;
         maxDisp = disp;
      }
      float positiveArc = Utils.positiveAngleBetween(normalHeadingVec, headingVec);
      boolean tmpAngB = positiveArc < PConstants.PI && positiveArc > 0;
      boolean tmpAngR = !(positiveArc > PConstants.HALF_PI && positiveArc < 3 * PConstants.HALF_PI);
      if (tmpAngB != angB && (angR && tmpAngR)) {
         if (tmpAngB) {
            revs++;
         } else {
            revs--;
         }
      }
      angB = tmpAngB;
      angR = tmpAngR;
      rotation = positiveArc + revs * PConstants.TWO_PI;
      if (PApplet.abs(rotation) > PApplet.abs(maxRotation)) {
         maxRotation = rotation;
      }
   }
}
