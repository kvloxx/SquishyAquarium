package squishyaquarium;

import processing.core.PApplet;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Adam on 6/25/2016.
 */
public class Tree {
   PApplet p;
   World world;

   Set<Node> nodes;
   Set<Spring> springs;
   Stroke stroke;
   Node root;

   public Tree(PApplet p, World world) {
      this.p = p;
      this.world = world;
      nodes = new LinkedHashSet<>();
      springs = new LinkedHashSet<>();
      stroke = null;
      root = null;
   }

   public Tree(Set<Node> nodes, Set<Spring> springs, Stroke stroke, PApplet p, World world) {
      this(nodes.iterator().next(), nodes, springs, stroke, p, world);
   }

   public Tree(Node root, Set<Node> nodes, Set<Spring> springs, Stroke stroke, PApplet p, World world) {
      this.p = p;
      this.world = world;
      this.root = root;
      this.nodes = nodes;
      this.springs = springs;
      this.stroke = stroke;
   }

   public Tree(Node root, Set<Node> nodes, Set<Spring> springs, PApplet p, World world) {
      this(root, nodes, springs, null, p, world);
   }

   /**
    * Returns the {@link Tree} object whose root is the specified {@link Node}.
    * Tree elements are NOT copied; a statement like getSubtreeAt(n).root == n will return true
    * The tree's {@code nodes} field is the subset of this tree's nodes that are descendants of subtreeRoot.
    * The tree's springs field is the subset of the caller's springs whose endpoints a and b are
    * both members of the nodes set.
    * The actions field of the tree's {@link Stroke} object lists the subset of {@link StrokeAction}s
    * whose objects are members of the nodes or springs set, preserving their original ordering
    *
    * @param subtreeRoot the root of the returned Tree
    * @return the Tree rooted at the specified node
    */
   public Tree getSubtreeAt(Node subtreeRoot) {
      Set<Node> n = getSubtreeNodes(subtreeRoot);
      Set<Spring> s = getSpringsContainedIn(n);
      Stroke a = new Stroke(stroke.getActionsContainedIn(n, true), this.stroke.interval);
      return new Tree(subtreeRoot, n, s, a, p, world);
   }

   /**
    * Returns the {@link Tree} consisting of elements (nodes, springs, strokeActions) that are
    * members of this tree but not the tree rooted at the specified node.
    * Tree elements are NOT copied; a statement like getSubtreeAt(n).root == n will return true
    * The returned tree is rooted at this tree's root.
    * The result for Node n can be thought of as the relative complement of the tree returned by getSubtreeAt(n)
    * in this tree.
    *
    * @param subtreeRoot the root of the tree whose elements will not be included in the result
    * @return the tree that contains (elements of this)/(elements of the tree rooted at the specified node)
    */


   /**
    * Returns a deep copy of getSubtreeAt(subtreeRoot) with elements added to the specified verlet physics world
    *
    * @param subtreeRoot the root of the subtree to be copied
    * @return A deep copy of the subtree at subtreeRoot with elements added to the specified world
    */
   public Tree copySubtreeAt(Node subtreeRoot) {
      String encoding = getSubtreeAt(subtreeRoot).stringifyTree(null, true);
      System.out.println("0000000000Encoding0000000000");
      System.out.println(encoding);

      TreeParser tp = new TreeParser(encoding, p, world);
      return tp.parseTree(false);
   }

   public Tree copyTreeMinusBranchAt(Node subtreeRoot) {
      Tree t = copySubtreeAt(root);
      t.removeSubtreeAt(subtreeRoot);

      for (Node node : t.nodes) {
         node.fill = 0xffffff00;
      }
      String encoding = t.stringifyTree(null, true);
      System.out.println("encoding = " + encoding);
      TreeParser tp = new TreeParser(encoding, p, world);
      return tp.parseTree(false);
   }

   public static Set<Node> getSubtreeNodes(Node subtreeRoot) {   //set includes current root node
      if (subtreeRoot.boneChildren == null) {
         Set<Node> s = new LinkedHashSet<>();
         s.add(subtreeRoot);
         return s;
      }
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
         return springs
               .stream()
               .filter(springFilter)
               .collect(Collectors.toCollection(LinkedHashSet::new));
      }
   }

   public static Set<Spring> getSpringsContainedIn(Set<Node> nodeSet, Predicate<? super Spring> springFilter) {
      return getSpringsContainedIn(nodeSet)
            .stream()
            .filter(springFilter)
            .collect(Collectors.toCollection(LinkedHashSet::new));
   }

   public static Set<Spring> getSpringsContainedIn(Set<Node> nodeSet) {
      return getSpringsContainedIn(nodeSet, true);
   }

   public static Set<Spring> getSpringsContainedIn(Set<Node> nodeSet, boolean fullyContained) {
      Set<Spring> ret = new LinkedHashSet<>();
      for (Node node : nodeSet) {
         ret.addAll(node.springsAttached
               .stream()
               .filter(spring -> spring.isContainedIn(nodeSet, fullyContained))
               .collect(Collectors.toList()));
      }
      return ret;
   }

   public Set<Spring> getOwnSpringsContainedIn(Set<Node> nodeSet) {
      return this.springs.stream()
            .filter(spring -> spring.isContainedIn(nodeSet))
            .collect(Collectors.toCollection(LinkedHashSet::new));
   }

   @Override
   public String toString() {
      return stringifyTree(null, true);
   }

   public String stringifyTree(Predicate<? super Spring> springFilter, boolean includeStroke) {
      StringBuilder sb = new StringBuilder();
      Map<SquishyBodyPart, String> names = new HashMap<>();

      int i = 1;

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

      sb.append(nodes.size())
            .append(" { ")
            .append(stringifyNodes(names))
            .append(" }\n")
            .append(springs.size())
            .append(" { ")
            .append(stringifySprings(names, springFilter))
            .append(" }\n")
            .append(stroke.actions.size())
            .append(" : ")
            .append(stroke.interval)
            .append(" { ")
            .append(includeStroke ? stroke.stringify(names) : "")
            .append(" } ");

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

   public Spring getRandomSpring(Predicate<? super Spring> springFilter) {
      ArrayList<Spring> l = springs
            .stream()
            .filter(springFilter)
            .collect(Collectors.toCollection(ArrayList::new));
      return l.get((int) (Math.random() * l.size()));
   }

   public void removeSubtreeAt(Node subtreeRoot) {
      Set<Node> subtreeNodes = getSubtreeNodes(subtreeRoot);

      removeReferencesTo(subtreeNodes, true);
   }

   /**
    * Removes all tree-level (<tt>{@link #nodes}</tt>, <tt>{@link #springs}</tt>, <tt>{@link #stroke}</tt>,
    * <tt>{@link #world}</tt>) and node-level (<tt>{@link Node#neighbors neighbors}</tt>,
    * <tt>{@link Node#boneChildren boneChildren}</tt>, <tt>{@link Node#boneParent boneParent}</tt>,
    * <tt>{@link Node#springsAttached springsAttached}</tt>) references to elements in
    * <tt>nodeSubset</tt> and springs with these elements as endpoints.
    * @param nodeSubset          The set of nodes to remove from this tree's references
    * @param properSubtree       mark true to enable optimizations ONLY IF the nodes in <tt>nodeSubset</tt>
    *                            represent a <i>complete</i> tree rooted at the first node in the set returned by
    *                            <tt>nodeSubset.iterator().next()</tt>
    */
   private void removeReferencesTo(Set<Node> nodeSubset, boolean properSubtree) {

      Set<Spring> springSubset = getSpringsContainedIn(nodeSubset, false);

      springs.removeAll(springSubset);
      nodes.removeAll(nodeSubset);

      if (properSubtree) {
         Node subtreeRoot = nodeSubset.iterator().next();
         if (subtreeRoot.boneParent != null) {
         subtreeRoot.boneParent.boneChildren.remove(subtreeRoot);
         }
      }

      for (Node nodeNotInSubset : nodes) {
         for (Node nodeInSubset : nodeSubset) {
            nodeNotInSubset.neighbors.remove(nodeInSubset);
            if (!properSubtree) { //if proper subtree, this only needs to be done for the parent of the subtree root
               nodeNotInSubset.boneChildren.remove(nodeInSubset);
            }
         }
         nodeNotInSubset.springsAttached.removeAll(springSubset);
      }

      springSubset.forEach(world::removeSpring);
      nodeSubset.forEach(world::removeParticle);

      List<StrokeAction> subtreeActions = stroke.getActionsContainedIn(nodeSubset, false);
      stroke.actions.removeAll(subtreeActions);
   }

   /**
    * Returns the Tree at <tt>subtreeRoot</tt> with all references to elements it contains removed from this
    * tree's <tt>{@link Tree#nodes}</tt>, <tt>{@link Tree#springs}</tt>, and <tt>{@link Tree#stroke}</tt>.
    * The returned subtree has a reference to the same <tt>{@link Tree#world}</tt> as this tree. The only elements
    * removed from this world are springs with exactly 1 endpoint in the returned subtree.
    * @param subtreeRoot the root of the tree to be separated
    * @return the separated Tree at <tt>subtreeRoot</tt>
    */
   Tree splitAbove(Node subtreeRoot) {
      Set<Node> nodeSubset = getSubtreeNodes(subtreeRoot);
      Set<Spring> springSubset = getSpringsContainedIn(nodeSubset, false);
      springs.removeAll(springSubset);
      nodes.removeAll(nodeSubset);
      if (subtreeRoot.boneParent != null) {
         System.out.println("lalala =" + subtreeRoot.boneParent.boneChildren.remove(subtreeRoot));
      }
      subtreeRoot.boneParent = null;

      for (Node nodeNotInSubtree : nodes) {
         for (Node nodeInSubtree : nodeSubset) {
            nodeNotInSubtree.neighbors.remove(nodeInSubtree);
            nodeInSubtree.neighbors.remove(nodeNotInSubtree);
         }
         nodeNotInSubtree.springsAttached.removeAll(springSubset);
      }
      Set<Spring> halfContainedSprings =
            springSubset.stream()
                  .filter(spring -> !spring.isContainedIn(nodeSubset, true))
                  .collect(Collectors.toSet());
      for (Node subNode : nodeSubset) {
         subNode.springsAttached.removeAll(halfContainedSprings);
      }
      for (Spring hcs : halfContainedSprings) {
         world.removeSpring(hcs);
      springSubset.remove(hcs);
      }
      List<StrokeAction> subtreeActions = stroke.getActionsContainedIn(nodeSubset, false);
      stroke.actions.removeAll(subtreeActions);
      List<StrokeAction> fullyContainedActions = subtreeActions.stream()
            .filter(strokeAction -> strokeAction.actionIsContainedIn(nodeSubset, true))
            .collect(Collectors.toCollection(ArrayList::new));
      return new Tree(nodeSubset, springSubset, new Stroke(fullyContainedActions, stroke.interval), p, world);
   }
}
