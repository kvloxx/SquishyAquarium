package squishyaquarium;

import processing.core.PApplet;
import sun.security.acl.WorldGroupImpl;
import toxi.physics2d.VerletPhysics2D;

import java.awt.event.ActionEvent;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Adam on 6/25/2016.
 */
public class Tree {
   Set<Node> nodes;
   Set<Spring> springs;
   List<StrokeAction> stroke;
   Node root;

   public Tree(Set<Node> nodes, Set<Spring> springs, List<StrokeAction> stroke) {
      this(nodes.iterator().next(), nodes, springs, stroke);
   }

   public Tree(Node root, Set<Node> nodes, Set<Spring> springs, List<StrokeAction> stroke) {
      this.root = root;
      this.nodes = nodes;
      this.springs = springs;
      this.stroke = stroke;
   }

   public Tree getSubtreeAt(Node subtreeRoot) {
      Set<Node> n = subtreeRoot.getSubtreeNodeSet();
      Set<Spring> s = getOwnSpringsContainedIn(n);
      List<StrokeAction> a = getStrokesContainedIn(n);
      return new Tree(subtreeRoot, n, s, a);
   }

   public Tree getTreeMinusBranchAt(Node subtreeRoot) {
      Set<Node> n = new LinkedHashSet<>(nodes);
      n.removeAll(getSubtreeNodes(subtreeRoot));
      Set<Spring> s = getOwnSpringsContainedIn(n);
      List<StrokeAction> a = getStrokesContainedIn(n);
      return new Tree(root, n, s, a);
   }

   public Tree copySubtreeAt(Node subtreeRoot, PApplet p, VerletPhysics2D world){
      String encoding = getSubtreeAt(subtreeRoot).stringify(null, true);
      System.out.println("0000000000Encoding0000000000");
      System.out.println(encoding);

      TreeParser tp = new TreeParser(encoding, p, world);
      return tp.parseTree();
   }

   public Tree copyTreeMinusBranchAt(Node subtreeRoot, PApplet p, VerletPhysics2D world) {
      String encoding = getTreeMinusBranchAt(subtreeRoot).stringify(null, true);
      TreeParser tp = new TreeParser(encoding, p, world);
      return tp.parseTree();
   }

   public static Set<Node> getSubtreeNodes(Node subtreeRoot) {   //set includes current root node
      Set<Node> ret = new LinkedHashSet<>();
      Queue<Node> Q = new PriorityQueue<>(subtreeRoot.boneChildren);

      ret.add(subtreeRoot);

      while (!Q.isEmpty()) {
         Node curr = Q.remove();
         ret.add(curr);
         Q.addAll(curr.boneChildren);
      }
      return ret;
   }

   public Set<Spring> getSprings(Predicate<? super Spring> springFilter) {
      if (springFilter == null) {
         return this.springs;
      } else {
         return springs.parallelStream().filter(springFilter).collect(Collectors.toCollection(LinkedHashSet::new));
      }
   }

   public static Set<Spring> getSpringsContainedIn(Set<Node> nodeSet, Predicate<? super Spring> springFilter) {
      return getSpringsContainedIn(nodeSet).parallelStream().filter(springFilter).collect(Collectors.toCollection
            (LinkedHashSet::new));
   }

   public static Set<Spring> getSpringsContainedIn(Set<Node> nodeSet) {
      Set<Spring> ret = new LinkedHashSet<>();
      for (Node node : nodeSet) {
         ret.addAll(node.springsAttached.stream()
               .filter(spring -> spring.isContainedIn(nodeSet))
               .collect(Collectors.toList()));
      }
      return ret;
   }

   public Set<Spring> getOwnSpringsContainedIn(Set<Node> nodeSet) {
      return this.springs.parallelStream()
            .filter(spring -> spring.isContainedIn(nodeSet))
            .collect(Collectors.toCollection(LinkedHashSet::new));
   }

   public List<StrokeAction> getStrokesContainedIn(Set<Node> nodeSet) {
      return stroke.stream()
            .filter(strokeAction -> strokeAction.actionIsContainedIn(nodeSet))
            .collect(Collectors.toCollection(ArrayList::new));
   }

   @Override
   public String toString() {
      return stringify(null, true);
   }

   public String stringify(Predicate<? super Spring> springFilter, boolean includeStroke) {
      StringBuilder sb = new StringBuilder();
      Map<SquishyBodyPart, String> names = new HashMap<>();

      int i=1;

      for (Node n : nodes) {
         names.put(n, "@" + i++);
      }

      int bCount = 0;
      int mCount = 0;
      int tCount = 0;

      for (Spring s : springs) {
         names.put(s, "$" + (s.type == Spring.Type.BONE ? ("B" + bCount++) :
               (s.type == Spring.Type.MUSCLE ? ("M" + mCount++) : ("T" + tCount++))));
      }

      sb.append("{ ")
            .append(stringifyNodes(names))
            .append(" }\n{ ")
            .append(stringifySprings(names, springFilter))
            .append(" }\n{ ")
            .append(includeStroke ? stringifyStroke(names) : "")
            .append("} ");

      return sb.toString();
   }
   String stringifyStroke(Map<SquishyBodyPart, String> names){
      StringBuilder sb=new StringBuilder();
      for (StrokeAction strokeAction : stroke) {
         sb.append("( ")
               .append(names.get(strokeAction.obj))
               .append(" ")
               .append(strokeAction.state)
               .append(" ) ");
      }
      return sb.toString();
   }

   String stringifyNodes(Map<SquishyBodyPart, String> names) {
      return root.stringifySubtreeNodeSet(names);
   }

   String stringifySprings(Map<SquishyBodyPart, String> names) {
      return stringifySprings(names, null);
   }

   String stringifySprings(Map<SquishyBodyPart, String> names, Predicate<? super Spring> springFilter) {
      Set<Spring> springSet;
      if (springFilter == null) {
         springSet = this.springs;
      } else {
         springSet = getSprings(springFilter);
      }
      StringBuilder sb = new StringBuilder();

      for (Spring spring : springSet) {
         sb.append(names.get(spring))
               .append(" ( ")
               .append(names.get(spring.a))
               .append(" : ")
               .append(names.get(spring.b))
               .append(" ) ")
               .append(spring.getDataString());
      }
      return sb.toString();
   }

   public Set<Spring> getSubtreeSprings(Set<Node> subtreeNodes) {
      Set<Spring> ret = new LinkedHashSet<>();

      for (Node node : subtreeNodes) {
         for (Spring spring : node.springsAttached) {
            if (spring.isContainedIn(subtreeNodes)) {
               ret.add(spring);
            }
         }
      }

      return ret;
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
}
