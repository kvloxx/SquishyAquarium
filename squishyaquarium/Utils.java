package squishyaquarium;

import processing.core.PConstants;
import toxi.geom.Vec2D;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by Adam on 7/2/2016.
 */
public class Utils {
   public static boolean ccw(float Ax, float Ay, float Bx, float By, float Cx, float Cy) {
      return (Cy - Ay) * (Bx - Ax) > (By - Ay) * (Cx - Ax);
   }

   public static boolean intersect(float Ax, float Ay, float Bx, float By, float Cx, float Cy, float Dx, float Dy) {
      return ccw(Ax, Ay, Cx, Cy, Dx, Dy) != ccw(Bx, By, Cx, Cy, Dx, Dy) && ccw(Ax, Ay, Bx, By, Cx, Cy) != ccw(Ax, Ay, Bx, By, Dx, Dy);
   }

   public static boolean intersect(float ax, float ay, float bx, float by, Spring s) {
      return intersect(ax, ay, bx, by, s.a.x, s.a.y, s.b.x, s.b.y);
   }

   public static boolean springsDoIntersect(float ax, float ay, float bx, float by, Set<Spring> springs) {
      Iterator<Spring> i = springs.iterator();
      while (i.hasNext()) {
         Spring s = i.next();
         if (intersect(ax, ay, bx, by, s)) {
            return true;
         }
      }
      return false;
   }

   public static float positiveAngleBetween(Vec2D v1, Vec2D v2) {
      float theta1, theta2;
      theta1 = (theta1 = ((float) Math.atan2(v1.y, v1.x))) < 0 ? PConstants.TWO_PI + theta1 : theta1;
      theta2 = (theta2 = ((float) Math.atan2(v2.y, v2.x))) < 0 ? PConstants.TWO_PI + theta2 : theta2;
      if (theta1 > theta2) {
         theta2 += PConstants.TWO_PI;
      }
      return theta2 - theta1;
   }
}
