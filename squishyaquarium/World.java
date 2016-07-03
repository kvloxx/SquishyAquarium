package squishyaquarium;

import toxi.geom.Vec2D;
import toxi.physics2d.VerletParticle2D;
import toxi.physics2d.VerletPhysics2D;
import toxi.physics2d.behaviors.ParticleBehavior2D;

import java.util.ArrayList;

/**
 * Created by Adam on 7/1/2016.
 */
public class World extends VerletPhysics2D {

   public ArrayList<SquishyBody> bodies;

   public World(Vec2D o, int i, float drag, int i1) {
      super(o, i, drag, i1);
      this.bodies = new ArrayList<>();
   }

   public void addBody(SquishyBody body){
      bodies.add(body);
   }

   @Override
   protected void updateParticles() {
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
         body.M =0;
         body.COM.set(0, 0);
         for (Node node : body.nodes) {
            node.scaleVelocity(drag);
            node.update();

            float m;
            body.COM.addSelf(node.scale((m=node.getWeight())));
            body.M+=m;
         }
         body.COM.scaleSelf(1 / body.M);
         body.updateHeading();
         body.recordDisplacementAndRotation();
      }
   }
}
