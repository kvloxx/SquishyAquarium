package squishyaquarium;

import processing.core.PApplet;
import toxi.geom.Vec2D;
import toxi.physics2d.VerletParticle2D;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static processing.core.PApplet.map;

public class Node extends VerletParticle2D implements SquishyBodyPart {
   private final String nodeID = UUID.randomUUID().toString();
   float w;
   boolean isHead = false;
   int fill;
   float d = 10;
   Vec2D normal;
   Node boneParent;
   Set<Node> boneChildren;
   Set<Node> neighbors;
   Set<Spring> springsAttached;
   private PApplet p;
   private float state;


   Node(PApplet p) {
      this(0, 0, p);
   }

   Node(float x, float y, PApplet p) {
      this(x, y, p.color(p.random(256), p.random(256), p.random(256)), p);
   }

   Node(float x, float y, int fill, PApplet p) {
      super(new Vec2D(x, y), 1.0f);
      recordNormal();
      this.p = p;
      this.fill = fill;
      this.w = getWeight();
      this.state = 1.0f;
      this.neighbors = new LinkedHashSet<>();
      this.boneChildren = new LinkedHashSet<>();
      this.springsAttached = new LinkedHashSet<>();
   }

   public void recordNormal() {
      this.normal = new Vec2D(x, y);
   }

   public void updateData(float x, float y, float w, int fill) {
      this.fill = fill;
      updateData(x, y, w);
   }

   public void updateData(float x, float y, float w) {   //"updata"
      this.x=x;
      this.y=y;
      recordNormal();
      this.w=w;
      changeState(state);
   }

   @Override
   public void changeState(float state) {
      this.state = state;
      setWeight(map(state, 0, 1, 0, w));
   }

   @Override
   public boolean isContainedIn(Set<Node> nodeSet, boolean fullyContained) {
      return nodeSet.contains(this);
   }

   @Override
   public void resetState() {
      this.weight = 1.9f;
      this.state = 1.0f;
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

   void addNeighbor(Node n) {
      neighbors.add(n);
   }

   String stringifySubtreeNodeSet(Map<SquishyBodyPart, String> nodeNames) {
      StringBuilder sb = new StringBuilder();
      String thisString = nodeNames.get(this);

      if (thisString == null) {  //assume if node can't be found in table, doesn't belong
         return "";
      }

      sb.append(nodeNames.get(this))
            .append(" ")
            .append(getDataString())
            .append(" { ");
      for (Node child : boneChildren) {
         sb.append(child.stringifySubtreeNodeSet(nodeNames));
      }
      sb.append(" } ");
      return sb.toString();
   }

   Set<Node> getSubtreeNodeSet(){
      Set<Node> ret = new LinkedHashSet<>();
      ret.add(this);
      for (Node child : boneChildren) {
         ret.addAll(child.getSubtreeNodeSet());
      }
      return ret;
   }

   String getDataString() {
      String hex = Integer.toHexString(fill);
      if (hex.length() == 8 && hex.substring(0, 2).equalsIgnoreCase("ff")) {
         hex = hex.substring(2, 8);
      }
      return "[ " + normal.x + " " + normal.y + " " + w + " " + hex + " ]";
   }

   public Node[] getBoneNeighborsArray(){
      Node[] ret = boneChildren.toArray(new Node[boneChildren.size() + 1]);
      ret[ret.length - 1]=boneParent;
      return ret;
   }

   public void rotateAbout(Vec2D pivot, float theta) {
      /*
      Rotating a point from position (o.x, o.y) to position (oR.x, oR.y)
      through an angle "theta" about pivot point (oP.x, oP.y)

      oR.x = oP.x + (o.x - oP.x) * cos(theta) - (o.y - oP.y) * sin(theta)
      oR.y = oP.y + (o.x - oP.x) * sin(theta) + (o.y - oP.y) * cos(theta)
      */
      x = pivot.x + (x - pivot.x) * PApplet.cos(theta) - (y - pivot.y) * PApplet.sin(theta);
      y = pivot.y + (x - pivot.x) * PApplet.sin(theta) + (y - pivot.y) * PApplet.cos(theta);
      recordNormal();
   }

   public void attachSpring(Spring s) {
      springsAttached.add(s);
   }

   public boolean hasAsAncestor(Node subtreeRoot) {
      if (boneParent == null) {
         return false;
      }
      if (boneParent == subtreeRoot) {
         return true;
      } else return boneParent.hasAsAncestor(subtreeRoot);
   }

   @Override
   public boolean equals(Object obj) {
      return (obj instanceof Node && ((Node) obj).nodeID.equals(nodeID));
   }

   @Override
   public int hashCode() {
      return nodeID.hashCode();
   }

   @Override
   public String toString() {
      return "Node{" + x + ", " + y + ", : " + w + "}";
   }

   public void resetPositionAndVelocity() {
      set(normal);
      scaleVelocity(0);
   }
}
  