package squishyaquarium;

import processing.core.PApplet;
import toxi.geom.Vec2D;
import toxi.physics2d.VerletParticle2D;

import java.util.*;

import static processing.core.PApplet.map;

class Node extends VerletParticle2D implements SquishyBodyPart{
   private final String ID=UUID.randomUUID().toString();

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
   private Set<Node> muscleNeighbors;

   List<StrokeAction> subtreeStroke;
   List<String> subStrokeStrList;

   Node(float x, float y, int fill, PApplet p) {
      super(new Vec2D(x, y));
      this.p = p;
      this.fill=fill;
      this.w = getWeight();
      this.neighbors = new LinkedHashSet<>();
      this.boneChildren = new LinkedHashSet<>();
      this.boneNeighbors = new LinkedHashSet<>();
      this.muscleNeighbors = new LinkedHashSet<>();
      this.state = 0.5f;
      this.subtreeStroke=new ArrayList<>();
   }

   private Node(float x, float y, PApplet p) {
      this(x, y, 255, p);
   }
   Node(PApplet p) {
      this(0,0,p);
   }

   void display() {
      p.noStroke();
      p.fill(
            map(state, 0, 1, 255, p.red(fill)),
            map(state, 0, 1, 255, p.blue(fill)),
            map(state, 0, 1, 255, p.green(fill)));

      if (isHead) {
         p.ellipse(x, y, d + 5, d + 5);
      }
      else {
         p.ellipse(x, y, d, d);
      }
   }

   void addBoneParent(Node n) {
      if(boneParent != null){
         System.out.println(this+" already has boneParent "+ boneParent +"; cannot add "+n+" as boneParent");
         return;
      }
      boneParent =n;
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

   private Set<Node> getSubtree(Node root){
      LinkedHashSet<Node> subtree=new LinkedHashSet<>();
      subtree.add(root);
      boneChildren.forEach(child -> subtree.addAll(getSubtree(child)));
      return subtree;
   }

   String stringifySubtreeStroke(){
      StringBuilder sb=new StringBuilder();
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
      return " "+ ID +
            " " + x +
            " " + y +
            " " + w +
            " " + state +
            " " + isHead +
            " " + fill +
            " " + d +
            " " + stringifySubtreeStroke() +"_END_ ";
   }
}
  