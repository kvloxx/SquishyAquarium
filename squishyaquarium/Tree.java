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
   World   world;

   Set<Node>    nodes;
   Set<Spring>  springs;
   List<Stroke> strokes;
   Node         root;

   public Tree(PApplet p, World world) {
      this.p = p;
      this.world = world;
      nodes = new LinkedHashSet<>();
      springs = new LinkedHashSet<>();
      strokes = null;
      root = null;
   }

   public Tree(Set<Node> nodes, Set<Spring> springs, List<Stroke> strokes, PApplet p, World world) {
      this(nodes.iterator().next(), nodes, springs, strokes, p, world);
   }

   public Tree(Node root, Set<Node> nodes, Set<Spring> springs, List<Stroke> strokes, PApplet p, World world) {
      this.p = p;
      this.world = world;
      this.root = root;
      this.nodes = nodes;
      this.springs = springs;
      this.strokes = strokes;
   }

   public Tree(Node root, Set<Node> nodes, Set<Spring> springs, PApplet p, World world) {
      this(root, nodes, springs, null, p, world);
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
      List<Stroke> st = getStrokesContainedIn(n, true);
      return new Tree(subtreeRoot, n, s, st, p, world);
   }

   private List<Stroke> getStrokesContainedIn(Set<Node> nodeSet, boolean fullyContained) {
      List<Stroke> ret = new ArrayList<>();
      for (int i = 0; i < strokes.size(); i++) {
         ret.add(strokes.get(i).getStrokeContainedIn(nodeSet, fullyContained));
      }
      return ret;
   }

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
            .append(" { ")
            .append(includeStroke ? stringifyStrokes(names) : "")
            .append(" } ");

      return sb.toString();
   }

   String stringifyNodes(Map<SquishyBodyPart, String> names) {
      return root.stringifySubtreeNodeSet(names);
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

   String stringifyStrokes(Map<SquishyBodyPart, String> names) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < strokes.size(); i++) {
         Stroke stroke = strokes.get(i);
         sb.append("[ ")
               .append(i).append(" : ").append(stroke.interval).append(" ] ")
               .append(stroke.stringify(names));
      }
      return sb.toString();
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

   String stringifySprings(Map<SquishyBodyPart, String> names) {
      return stringifySprings(names, null);
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
    * Removes all tree-level members of (<tt>{@link #nodes}</tt>, <tt>{@link #springs}</tt>, <tt>{@link #strokes}</tt>,
    * <tt>{@link #world}</tt>) and node-level members of (<tt>{@link Node#neighbors neighbors}</tt>,
    * <tt>{@link Node#boneChildren boneChildren}</tt>, <tt>{@link Node#boneParent boneParent}</tt>,
    * <tt>{@link Node#springsAttached springsAttached}</tt>) references to elements in
    * <tt>nodeSubset</tt> and springs with these elements as endpoints.
    *
    * @param nodeSubset    The set of nodes to remove from this tree's references
    * @param properSubtree mark true to enable optimizations ONLY IF the nodes in <tt>nodeSubset</tt>
    *                      represent a <i>complete</i> tree rooted at the first node in the set returned by
    *                      <tt>nodeSubset.iterator().next()</tt>
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

      for (Stroke stroke : strokes) {
         List<StrokeAction> subtreeActions = stroke.getActionsContainedIn(nodeSubset, false);
         stroke.actions.removeAll(subtreeActions);
      }
   }

   /**
    * Returns the Tree at <tt>subtreeRoot</tt> with all references to elements it contains removed from this
    * tree's <tt>{@link Tree#nodes}</tt>, <tt>{@link Tree#springs}</tt>, and <tt>{@link Tree#strokes}</tt>.
    * The returned subtree has a reference to the same <tt>{@link Tree#world}</tt> as this tree. The only elements
    * removed from this world are springs with exactly 1 endpoint in the returned subtree.
    *
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
      List<Stroke> fullyContainedStrokesList = new ArrayList<>(3);
      for (Stroke stroke : strokes) {
         List<StrokeAction> subtreeActions = stroke.getActionsContainedIn(nodeSubset, false);
         stroke.actions.removeAll(subtreeActions);
         fullyContainedStrokesList.add(
               new Stroke(
                     subtreeActions
                           .stream()
                           .filter(action -> action.actionIsContainedIn(nodeSubset, true))
                           .collect(Collectors.toCollection(ArrayList::new))
                     , stroke.interval));
      }
      return new Tree(nodeSubset, springSubset, fullyContainedStrokesList, p, world);
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

   /**
    * Iterates through this object's <tt>strokes</tt>, invoking the element at each index's
    * {@link Stroke#blendWith blendWith} method, passing as an argument the correspondingly indexed element in
    * <tt>newStrokesList</tt>.
    * If the length of <tt>newStrokesList</tt> is greater than the length of this <tt>strokes</tt>, any element in
    * <tt>newStrokesList</tt> whose index is greater than <tt>strokes.length - 1</tt> is appended unchanged to the
    * end of <tt>strokes</tt>.
    * If the length of <tt>newStrokesList</tt> is less than the length of this <tt>strokes</tt>, any elements in
    * <tt>strokes</tt> whose index is greater than <tt>newStrokesList.length - 1</tt> is left unchanged.
    *
    * @param newStrokesList the list of Stroke objects to be blended with the strokes in this object's <tt>strokes</tt>
    */
   protected void blendStrokeListWith(List<Stroke> newStrokesList) {
      int i;
      for (i = 0; i < strokes.size(); i++) {
         strokes.get(i).blendWith(newStrokesList.get(i));
      }
      for (; i < newStrokesList.size(); i++) {
         strokes.add(newStrokesList.get(i));
      }
   }
}
