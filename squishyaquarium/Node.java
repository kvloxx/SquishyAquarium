package squishyaquarium;

import processing.core.PApplet;
import sun.reflect.generics.tree.Tree;
import toxi.geom.Vec2D;
import toxi.physics2d.VerletParticle2D;
import toxi.physics2d.VerletPhysics2D;

import java.util.*;
import java.util.stream.Collectors;

import static processing.core.PApplet.map;
import static processing.core.PConstants.Z;

public class Node extends VerletParticle2D implements SquishyBodyPart {

   private PApplet p;

   float w;
   private float state;
   boolean isHead = false;
   int fill;
   float d = 10;
   Vec2D normal;

   Node boneParent;
   Set<Node> boneChildren;
   Set<Node> muscleNeighbors;
   Set<Node> neighbors;

   Set<Spring> springsAttached;

   List<StrokeAction> subtreeStroke;
   List<String> subStrokeStrList;

   Node(float x, float y, int fill, PApplet p) {
      super(new Vec2D(x, y));
      this.normal = null;
      this.p = p;
      this.fill = fill;
      this.w = getWeight();
      this.state = 1.0f;
      this.neighbors = new LinkedHashSet<>();
      this.boneChildren = new LinkedHashSet<>();
      this.muscleNeighbors = new LinkedHashSet<>();
      this.springsAttached = new LinkedHashSet<>();
      this.subtreeStroke = new ArrayList<>();
   }

   Node(float x, float y, PApplet p) {
      this(x, y, p.color(255), p);
   }

   Node(PApplet p) {
      this(0, 0, p);
   }

   public void updateData(float x, float y, float w) {   //"updata"
      this.x=x;
      this.y=y;
      recordNormal();
      this.w=w;
      this.changeState(this.state);
   }

   public void updateData(float x, float y, float w, int fill) {
      this.fill=fill;
      updateData(x, y, w);
   }

   void display() {
      p.noStroke();
      p.fill(
            fill);
//            map(state, 0, 1, 255, p.red(fill)),
//            map(state, 0, 1, 255, p.blue(fill)),
//            map(state, 0, 1, 255, p.green(fill)));

      if (isHead) {
         p.ellipse(x, y, d + 5, d + 5);
      } else {
         p.ellipse(x, y, d, d);
      }
   }
   public void recordNormal(){
      this.normal = new Vec2D(this.x, this.y);
   }

   void addBoneParent(Node n) {
      if (boneParent != null) {
         System.out.println(this + " already has boneParent " + boneParent + "; cannot add " + n + " as boneParent");
         return;
      }
      boneParent = n;
      neighbors.add(n);
   }

   void addBoneChild(Node n) {
      neighbors.add(n);
      boneChildren.add(n);
   }

   void addMuscleNeighbor(Node n) {
      neighbors.add(n);
      muscleNeighbors.add(n);
   }

   void addTissueNeighbor(Node n) {
      neighbors.add(n);
   }

   String stringifySubtreeNodeSet(Map<SquishyBodyPart, String> nodeNames) {
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

   Set<Node> getSubtreeNodeSet(){
      Set<Node> ret = new LinkedHashSet<>();
      ret.add(this);
      for (Node child : this.boneChildren) {
         ret.addAll(child.getSubtreeNodeSet());
      }
      return ret;
   }

   String getDataString() {
      p.alpha(fill);
      return "[ " + normal.x + " " + normal.y + " " + w + " " + Integer.toHexString(fill) + " ]";
   }

   public Node[] getBoneNeighborsArray(){
      Node[] ret = boneChildren.toArray(new Node[boneChildren.size() + 1]);
      ret[ret.length - 1]=boneParent;
      return ret;
   }

   @Override
   public void changeState(float state) {
      this.state = state;
      this.setWeight(map(state, 0, 1, 0, w));
   }

   @Override
   public boolean isContainedIn(Set<Node> nodeSet) {
      return nodeSet.contains(this);
   }

   @Override
   public String toString() {
      return "Node{"+x+", "+y+", : "+w+"}";
   }

   public void attachSpring(Spring s) {
      springsAttached.add(s);
   }

}
  