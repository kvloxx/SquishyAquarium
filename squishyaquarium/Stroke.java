package squishyaquarium;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Adam on 6/27/2016.
 */
public class Stroke {
   public static final int FORWARD = 0;
   public static final int ROTATE_POSITIVE = 1;
   public static final int ROTATE_NEGATIVE = 2;
   public List<List<StrokeAction>> actionsList;
   public int[] intervals;
   private int currentBehavior = 0;
   private int[] currentActionIndices;

   public Stroke(List<StrokeAction> forwardActions, List<StrokeAction> rotatePosActions, List<StrokeAction> rotateNegActions, int forwardInterval, int rotPosInterval, int rotNegInterval) {
      actionsList = new ArrayList<>(3);
      actionsList.add(FORWARD, forwardActions);
      actionsList.add(ROTATE_POSITIVE, rotatePosActions);
      actionsList.add(ROTATE_NEGATIVE, rotateNegActions);
      this.intervals = new int[]{forwardInterval, rotPosInterval, rotNegInterval};
      this.currentActionIndices = new int[]{0, 0, 0};
   }

   public Stroke(List<List<StrokeAction>> actionsList, int[] intervals) {
      this.actionsList = actionsList;
      this.intervals = intervals;
      this.currentActionIndices = new int[]{0, 0, 0};
   }

   public void strokeIfReady(int frameCount) {
      if (frameCount % intervals[currentBehavior] == 0) {
         executeNextStrokeAction();
      }
   }

   public void executeNextStrokeAction() {
      executeNextStrokeAction(getCurrentBehavior());
   }

   public void executeNextStrokeAction(int behavior) {
      List<StrokeAction> actions = actionsList.get(behavior);
      int currentStrokeAction = currentActionIndices[behavior];
      if (actions != null && actions.size() > currentStrokeAction && actions.get(currentStrokeAction) != null) {
         actions.get(currentStrokeAction).executeAction();
         currentActionIndices[behavior] = (currentStrokeAction + 1) % actions.size();
      }
   }

   public List<StrokeAction> getActionsContainedIn(int behavior, Set<Node> nodeSet, boolean fullyContained) {
      List<StrokeAction> actions = actionsList.get(behavior);
      return actions.stream()
            .filter(strokeAction -> strokeAction.actionIsContainedIn(nodeSet, fullyContained))
            .collect(Collectors.toCollection(ArrayList::new));
   }

   String stringify(Map<SquishyBodyPart, String> names) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < actionsList.size(); i++) {
         List<StrokeAction> actions = actionsList.get(i);
         sb.append("[ ")
         .append(i).append(" : ").append(intervals[i]).append(" ] ")
               .append(" { ");
         for (StrokeAction action : actions) {
            sb
                  .append("( ")
                  .append(names.get(action.obj))
                  .append(" ")
                  .append(action.state)
                  .append(" ) \n");
         }
         sb.append(" } ");
      }
      return sb.toString();
   }

   public void blendWith(Stroke stroke) {
      if (stroke == null) {
         return;
      }
      for (int i = 0; i < actionsList.size(); i++) {
         blendActionsWith(i, stroke.actionsList.get(i));
         intervals[i] = (stroke.intervals[i] + this.intervals[i]) / 2;
      }
   }

   public void blendActionsWith(int behavior, List<StrokeAction> newActions) {
      List<StrokeAction> thisActions = actionsList.get(behavior);
      Iterator<StrokeAction> actionIterator1 = thisActions.iterator();
      Iterator<StrokeAction> actionIterator2 = newActions.iterator();
      List<StrokeAction> newStrokeActionList = new ArrayList<>(thisActions.size() + newActions.size());
      while (actionIterator1.hasNext() && actionIterator2.hasNext()) {
         double r = Math.random();
         newStrokeActionList.add(r < 0.5 ? actionIterator1.next() : actionIterator2.next());
      }
      if (actionIterator1.hasNext()) {
         actionIterator1.forEachRemaining(newStrokeActionList::add);
      } else if (actionIterator2.hasNext()) {
         actionIterator2.forEachRemaining(newStrokeActionList::add);
      }
      this.actionsList.set(behavior, newStrokeActionList);
      this.currentActionIndices[behavior] = 0;
   }

   public void removeActions(int behavior, int count) {
      List<StrokeAction> actions = actionsList.get(behavior);
      while (count-- > 0) {
         int randIdx = (int) (Math.random() * actions.size());
         actions.remove(randIdx);
      }
   }

   public void randomlySwap(int behavior, int indexOfAction) {
      List<StrokeAction> actions = actionsList.get(behavior);
      if (indexOfAction >= actions.size()) {
         return;
      }
      StrokeAction action = actions.get(indexOfAction);
      int idx2 = (indexOfAction + actions.size() - 1) % actions.size();
      actions.set(indexOfAction, actions.get(idx2));
      actions.set(idx2, action);
   }

   public void shuffle(int behavior) {
      List<StrokeAction> actions = actionsList.get(behavior);
      Collections.shuffle(actions);
   }

   public Stroke getStrokeContainedIn(Set<Node> nodeSet, boolean fullyContained) {
      List<List<StrokeAction>> newActionsList = new ArrayList<>(3);
      for (int behavior = 0; behavior < 3; behavior++) {
         newActionsList.add(behavior, getActionsContainedIn(behavior, nodeSet, fullyContained));
      }
      return new Stroke(newActionsList, this.intervals);
   }

   public int getCurrentBehavior() {
      return currentBehavior;
   }

   public void setCurrentBehavior(int currentBehavior) {
      for (StrokeAction action : actionsList.get(currentBehavior)) {
         action.obj.reset();
      }
      this.currentBehavior = currentBehavior;
      this.currentActionIndices[currentBehavior] = 0;
   }
}
