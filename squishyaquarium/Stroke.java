package squishyaquarium;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Adam on 6/27/2016.
 */
public class Stroke {
   public List<StrokeAction> actions;
   public int                interval;
   private int currentActionIndex     = 0;
   private int updatesSinceLastStroke = 0;

   public Stroke(List<StrokeAction> actions, int interval) {
      this.actions = actions;
      this.interval = interval;
   }

   public int getCurrentActionIndex() {
      return currentActionIndex;
   }

   public void update() {
      if (updatesSinceLastStroke == interval) {
         stroke();
         updatesSinceLastStroke = 0;
      } else {
         updatesSinceLastStroke++;
      }

   }

   public void stroke() {
      if (actions != null && actions.size() > currentActionIndex && actions.get(currentActionIndex) != null) {
         actions.get(currentActionIndex).executeAction();
         currentActionIndex = (currentActionIndex + 1) % actions.size();
      }
   }

   String stringify(Map<SquishyBodyPart, String> names) {
      StringBuilder sb = new StringBuilder();
      sb.append("{ ");
      for (StrokeAction action : actions) {
         sb.append("( ")
               .append(names.get(action.obj))
               .append(" ")
               .append(action.state)
               .append(" ) ");
      }
      sb.append(" } ");
      return sb.toString();
   }

   public void blendWith(Stroke stroke) {
      if (stroke == null) {
         return;
      }
      blendActionsWith(stroke.actions);
      interval = (stroke.interval + interval / 2);
   }

   public void blendActionsWith(List<StrokeAction> newActions) {
      Iterator<StrokeAction> actionIterator1 = actions.iterator();
      Iterator<StrokeAction> actionIterator2 = newActions.iterator();
      List<StrokeAction> newStrokeActionList = new ArrayList<>(actions.size() + newActions.size());
      while (actionIterator1.hasNext() && actionIterator2.hasNext()) {
         double r = Math.random();
         newStrokeActionList.add(r < 0.5 ? actionIterator1.next() : actionIterator2.next());
      }
      if (actionIterator1.hasNext()) {
         actionIterator1.forEachRemaining(newStrokeActionList::add);
      } else if (actionIterator2.hasNext()) {
         actionIterator2.forEachRemaining(newStrokeActionList::add);
      }
      this.actions = newStrokeActionList;
      this.currentActionIndex = 0;
   }

   public void removeActions(int count) {
      while (count-- > 0) {
         int randIdx = (int) (Math.random() * actions.size());
         actions.remove(randIdx);
      }
   }

   public void randomlySwap(StrokeAction action) {
      if (!actions.contains(action)) {
         return;
      }
      int idx1 = actions.indexOf(action);
      int idx2 = (idx1 + actions.size() - 1) % actions.size();
      actions.set(idx1, actions.get(idx2));
      actions.set(idx2, action);
   }

   public void shuffle() {
      Collections.shuffle(actions);
   }

   public void prime() {
      for (StrokeAction action : actions) {
         action.executeAction();
      }
   }

   public void resetActionObjectStates() {
      for (StrokeAction action : actions) {
         action.obj.resetState();
      }
   }

   public Stroke getStrokeContainedIn(Set<Node> nodeSet, boolean fullyContained) {
      return new Stroke(getActionsContainedIn(nodeSet, fullyContained), interval);
   }

   public List<StrokeAction> getActionsContainedIn(Set<Node> nodeSet, boolean fullyContained) {
      return actions.stream()
            .filter(strokeAction -> strokeAction.actionIsContainedIn(nodeSet, fullyContained))
            .collect(Collectors.toCollection(ArrayList::new));
   }
}
