package squishyaquarium;

import processing.core.PApplet;
import toxi.geom.Vec2D;
import toxi.physics2d.VerletParticle2D;

import java.util.*;

import static processing.core.PApplet.map;

class Node extends VerletParticle2D implements SquishyBodyPart {
   private final String ID = UUID.randomUUID().toString();

   private PApplet p;

   float w;
   private float state;
   boolean isHead = false;
   int fill;
   float d = 10;

   Node boneParent;
   Set<Node> neighbors;
   Set<Node> boneChildren;
   Set<Node> boneNeighbors;
   Set<Node> muscleNeighbors;

   Set<Spring> springsAttached;

   List<StrokeAction> subtreeStroke;
   List<String> subStrokeStrList;

   Node(float x, float y, int fill, PApplet p) {
      super(new Vec2D(x, y));
      this.p = p;
      this.fill = fill;
      this.w = getWeight();
      this.neighbors = new LinkedHashSet<>();
      this.boneChildren = new LinkedHashSet<>();
      this.boneNeighbors = new LinkedHashSet<>();
      this.muscleNeighbors = new LinkedHashSet<>();
      this.state = 0.5f;
      this.subtreeStroke = new ArrayList<>();
      this.springsAttached = new LinkedHashSet<>();
   }

   private Node(float x, float y, PApplet p) {
      this(x, y, 255, p);
   }

   Node(PApplet p) {
      this(0, 0, p);
   }

   void display() {
      p.noStroke();
      p.fill(
            map(state, 0, 1, 255, p.red(fill)),
            map(state, 0, 1, 255, p.blue(fill)),
            map(state, 0, 1, 255, p.green(fill)));

      if (isHead) {
         p.ellipse(x, y, d + 5, d + 5);
      } else {
         p.ellipse(x, y, d, d);
      }
   }

   void addBoneParent(Node n) {
      if (boneParent != null) {
         System.out.println(this + " already has boneParent " + boneParent + "; cannot add " + n + " as boneParent");
         return;
      }
      boneParent = n;
      neighbors.add(n);
      boneNeighbors.add(n);
   }

   void addBoneChild(Node n) {
      neighbors.add(n);
      boneNeighbors.add(n);
      boneChildren.add(n);
   }

   void addMuscleNeighbor(Node n) {
      neighbors.add(n);
      muscleNeighbors.add(n);
   }

   void addTissueNeighbor(Node n) {
      neighbors.add(n);
   }

   public Set<Node> getSubtreeNodes() {
      LinkedHashSet<Node> ret = new LinkedHashSet<>();
      ret.add(this);
      for (Node child : boneChildren) {
         ret.addAll(child.getSubtreeNodes());
      }
      return ret;
   }

   public String stringifySubtree() {
      return stringifySubtree(false);
   }

   public String stringifySubtree(boolean includeStroke) {
      StringBuilder sb = new StringBuilder();
      Set<Node> subtreeNodes = getSubtreeNodes();
      Set<Spring> subtreeSprings = getSubtreeSprings(subtreeNodes);
      Map<SquishyBodyPart, String> names = new HashMap<>();
//      Map<? extends SquishyBodyPart, String> names = new HashMap<>();

      int i = 0;
      names.put(this, "@" + i++);
      for (Node n : subtreeNodes) {
         names.put(((SquishyBodyPart) n), "@" + i++);
      }

      int bCount = 0;
      int mCount = 0;
      int tCount = 0;

      for (Spring s : subtreeSprings) {
         names.put(((SquishyBodyPart) s),
               "$" + (s.type == Spring.Type.BONE ?
                     ("B" + bCount++) :
                     (s.type == Spring.Type.MUSCLE ?
                           ("M" + mCount++) :
                           ("T" + tCount++))));
      }

      sb.append("{ ")
            .append(stringifySubtreeNodeSet(names))
            .append(" }\n{ ")
            .append(stringifySubtreeSpringSet(subtreeSprings, names))
            .append(" }");

      if (includeStroke) {
         sb.append("\n{ ");
         for (StrokeAction strokeAction : subtreeStroke) {
            sb.append("( ")
            .append(names.get(strokeAction.obj))
            .append(" ")
            .append(strokeAction.state)
            .append(" ) ");
         }
         sb.append("} ");
      }
      return sb.toString();
   }

   private Set<Spring> getSubtreeSprings(Set<Node> subtreeNodes) {
      Set<Spring> ret = new LinkedHashSet<>();
      ret.addAll(springsAttached);

      for (Node subtreeNode : subtreeNodes) {
         ret.addAll(subtreeNode.springsAttached);
      }
      return ret;
   }

   private String stringifySubtreeNodeSet(Map<SquishyBodyPart, String> nodeNames) {
      StringBuilder sb = new StringBuilder();

      sb.append(nodeNames.get(this))
            .append(" ")
            .append(getDataString())
            .append(" { ");
      for (Node child : this.boneChildren) {
         sb.append(child.stringifySubtreeNodeSet(nodeNames));
      }
      sb.append(" } ");
      return sb.toString();
   }

   private String stringifySubtreeSpringSet(Set<Spring> springs, Map<SquishyBodyPart, String> names) {
      StringBuilder sb = new StringBuilder();

      for (Spring spring : springs) {
         sb.append(names.get(spring))
               .append(" ( ")
               .append(names.get(spring.a))
               .append(" : ")
               .append(names.get(spring.b))
               .append(" ) [ ")
               .append(spring.minLen)
               .append(" ")
               .append(spring.maxLen)
               .append(" ")
               .append(spring.str)
               .append(" ] ");
      }

      return sb.toString();
   }

   private String getDataString() {
      return "[ " + x + " " + y + " " + w + " ]";
   }

   String stringifySubtreeStroke() {
      StringBuilder sb = new StringBuilder();
      subtreeStroke.forEach(strokeAction -> sb.append(strokeAction.toString()));
      return sb.toString();
   }

   @Override
   public void changeState(float state) {
      this.state = state;
      this.setWeight(map(state, 0, 1, 0, w));
   }

   @Override
   public String getID() {
      return ID;
   }

   @Override
   public String toString() {
      return " " + ID +
            " " + x +
            " " + y +
            " " + w +
            " " + state +
            " " + isHead +
            " " + fill +
            " " + d +
            " " + stringifySubtreeStroke() + "_END_ ";
   }

   public void attachSpring(Spring s) {
      springsAttached.add(s);
   }
}
  