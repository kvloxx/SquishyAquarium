package squishyaquarium;

import toxi.geom.Vec2D;
import toxi.physics2d.VerletParticle2D;
import toxi.physics2d.VerletPhysics2D;
import toxi.physics2d.behaviors.ParticleBehavior2D;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Adam on 7/1/2016.
 */
public class World extends VerletPhysics2D {

   public ArrayList<SquishyBody> bodies;
   public int updateCount = 0;

   public World(Vec2D o, int i, float drag, int i1) {
      super(o, i, drag, i1);
      this.bodies = new ArrayList<>();
   }

   public void addBody(SquishyBody body) {
      bodies.add(body);
   }

   @Override
   public VerletPhysics2D update() {
      updateCount++;
      updateParticlesViaBodies();
      updateSprings();
      applyConstaints();
      updateIndex();
      updateBodies();
      return this;
   }

   protected void updateParticlesViaBodies() {
      for (ParticleBehavior2D b : behaviors) {
         if (index != null && b.supportsSpatialIndex()) {
            b.applyWithIndex(index);
         } else {
            for (VerletParticle2D p : particles) {
               b.apply(p);
            }
         }
      }
      for (SquishyBody body : bodies) {
         body.COM.set(0, 0);
         body.COMUpdated = true;
         for (Node node : body.nodes) {
            node.scaleVelocity(drag);
            node.update();
            body.COM.addSelf(node);
         }
         body.COM.scaleSelf(1 / (((float) body.nodes.size())));
      }
   }

   /** Copy of inherited private method of same name */
   private void updateIndex() {
      if (index != null) {
         index.clear();
         for (VerletParticle2D p : particles) {
            index.index(p);
         }
      }
   }

   protected void updateBodies() {
      for (SquishyBody body : bodies) {
         body.onWorldUpdate();
      }
   }

   public VerletPhysics2D updateOnlyWithoutStroke(SquishyBody body) {
      updateTheseParticles(body.nodes);
      updateTheseSprings(body.springs);
      applyConstaints();
      updateTheseIndexes(body.nodes);
      return this;
   }

   private void updateTheseParticles(Collection<Node> particlesToUpdate) {
      for (ParticleBehavior2D b : behaviors) {
         if (index != null && b.supportsSpatialIndex()) {
            b.applyWithIndex(index);
         } else {
            for (Node p : particlesToUpdate) {
               b.apply(p);
            }
         }
      }
      for (Node p : particlesToUpdate) {
         p.scaleVelocity(drag);
         p.update();
      }
   }

   private void updateTheseSprings(Collection<Spring> springs) {
      if (springs.size() > 0) {
         for (int i = numIterations; i > 0; i--) {
            for (Spring s : springs) {
               s.update(i == 1);
            }
         }
      }
   }

   private void updateTheseIndexes(Collection<Node> nodes) {
      if (index != null) {
         index.clear();
         for (VerletParticle2D p : nodes) {
            index.index(p);
         }
      }
   }

   public VerletPhysics2D updateThese(Collection<Node> nodes, Collection<Spring> springs) {
      updateTheseParticles(nodes);
      updateTheseSprings(springs);
      applyConstaints();
      updateTheseIndexes(nodes);
      return this;
   }
}
