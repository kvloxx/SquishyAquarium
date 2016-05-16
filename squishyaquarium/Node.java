package squishyaquarium;

import processing.core.PApplet;
import toxi.physics2d.*;
import toxi.geom.*;

import java.util.LinkedHashSet;
import java.util.Set;

class Node extends VerletParticle2D {
   PApplet p;
   int fill;
   float w;
   float state;
   boolean isHead = false;
   float d = 10;

   Set<Node> neighbors;
   Set<Node> boneNeighbors;
   Set<Node> muscleNeighbors;

   Node(float x, float y, int fill, PApplet p) {
      super(new Vec2D(x, y));
      this.p = p;
      this.fill=fill;
      this.w = getWeight();
      this.neighbors = new LinkedHashSet<>();
      this.boneNeighbors = new LinkedHashSet<>();
      this.muscleNeighbors = new LinkedHashSet<>();
      this.state = 0.5f;
   }

   Node(float x, float y, PApplet p) {
      this(x, y, 255, p);
   }

   void display() {
      p.noStroke();
      p.fill(
            p.map(state, 0, 1, 255, p.red(fill)),
            p.map(state, 0, 1, 255, p.blue(fill)),
            p.map(state, 0, 1, 255, p.green(fill)));

      if (isHead) {
         p.ellipse(x, y, d + 5, d + 5);
      }
      else {
         p.ellipse(x, y, d, d);
      }
   }

   public void setState(float s) {
      this.state = s;
      this.setWeight(p.map(s, 0, 1, 0, w));
   }

   @Override
   public String toString() {
      return "{x:" + x + " y:" + y + " w: " + w + "}";
   }

   void addBoneNeighbor(Node n) {
      neighbors.add(n);
      boneNeighbors.add(n);
   }

   void addMuscleNeighbor(Node n) {
      neighbors.add(n);
      muscleNeighbors.add(n);
   }

   void addTissueNeighbor(Node n) {
      neighbors.add(n);
   }
}
  