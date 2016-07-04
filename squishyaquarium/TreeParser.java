package squishyaquarium;

import processing.core.PApplet;
import toxi.geom.Rect;

import java.util.*;

import static squishyaquarium.TreeParser.TokenType.*;

/**
 * Created by kvloxx 44
 */
public class TreeParser {

   //Mutation Odds
   private final float duplication = 1 / 50f;
   private final float translocation = 1 / 45f;
   private final float deletion = 1 / 25f;
   private final float addition = 1 / 25f;
   private final float relaxation = 1 / 20f;
   private final float discover = 1 / 20f;
   private final float forget = 1 / 20f;
   private final float swap = 1 / 30f;
   private final float permutation = 1 / 40f;
   private Random rand;
   private PApplet p;
   private World world;
   private String input;
   private ArrayList<Token> tokens;
   private Token currentToken;
   private Iterator<Token> tokenIterator;
   private HashMap<String, SquishyBodyPart> hashedSquishyParts;
   private Set<Node> globalNodeSet;
   private Set<Spring> globalSpringSet;
   private List<StrokeAction> globalStrokeList;
   private Stroke globalStroke;

   private float mutationRate = 0;

   TreeParser(String input, PApplet p, World world) {
      this.rand = new Random();
      this.p = p;
      this.world = world;
      this.input = input;
      String[] stringTokens = input.split("\\s+");
      tokens = new ArrayList<>(stringTokens.length + 1); //EOF tok will be added
      for (String dataString : stringTokens) {
         tokens.add(new Token(dataString));
      }
      tokens.add(new Token("EOF"));
      hashedSquishyParts = new HashMap<>();
      globalNodeSet = new LinkedHashSet<>();
      globalSpringSet = new LinkedHashSet<>();
      globalStrokeList = new ArrayList<>();
   }

   private void reset() {
      String[] stringTokens = input.split("\\s+");
      tokens = new ArrayList<>(stringTokens.length + 1); //EOF tok will be added
      for (String dataString : stringTokens) {
         tokens.add(new Token(dataString));
      }
      tokens.add(new Token("EOF"));
      tokenIterator = tokens.iterator();
      currentToken = tokenIterator.next();
      hashedSquishyParts = new HashMap<>();
      globalNodeSet = new LinkedHashSet<>();
      globalSpringSet = new LinkedHashSet<>();
      globalStrokeList = new ArrayList<>();
   }

   SquishyBody sloppyParseSquishyBody(float mutationRate) {
      this.reset();
      this.mutationRate = mutationRate;
      SquishyBody sb = parseSquishyBody(true);
      mutate(sb);
      return sb;
   }

   private void mutate(SquishyBody sb) {
      //TODO: one of these is probably causing abnormal behavior in the physics sim; probably forgot to remove a
      // spring from the world or something.
      float delRate = mutationRate * deletion;
      float addRate = mutationRate * addition;
      float dupRate = mutationRate * duplication;
      float tlcRate = mutationRate * translocation;
      float rlxRate = mutationRate * relaxation;
      ArrayList<Node> del = null;
      ArrayList<Node> add = null;
      ArrayList<Node> dup = null;
      ArrayList<Node> tlc = null;
      int relaxCount = 0;

      for (Node node : sb.nodes) {
         if (r() < delRate) {
            if (del == null) del = new ArrayList<>();
            del.add(node);
         }
         if (r() < addRate) {
            if (add == null) add = new ArrayList<>();
            add.add(node);
         }
         if (r() < dupRate && node != sb.root) {
            if (dup == null) dup = new ArrayList<>();
            dup.add(node);
         }
         if (r() < tlcRate) {
            if (tlc == null) tlc = new ArrayList<>();
            tlc.add(node);
         }
         if (r() < rlxRate) {
            relaxCount++;
         }
      }

      float swpRate = mutationRate * swap;
      float pmtRate = mutationRate * permutation;
      float dscRate = mutationRate * discover;
      float fgtRate = mutationRate * forget;
      ArrayList<ArrayList<Integer>> swp = null;
      boolean[] permute = {false, false, false};
      int[] discoverCount = {0, 0, 0};
      int[] forgetCount = {0, 0, 0};
      for (int behavior = 0; behavior < 3; behavior++) {
         for (int actionIndex = 0; actionIndex < sb.stroke.actionsList.get(behavior).size(); actionIndex++) {
            if (r() < swpRate) {
               if (swp == null) {
                  swp = new ArrayList<>();
                  for (int i = 0; i < 3; i++) {
                     swp.add(new ArrayList<>());
                  }
               }
               swp.get(behavior).add(actionIndex);
            }
            if (r() < pmtRate) {
               permute[behavior] = true;
            }
            if (r() < dscRate) {
               discoverCount[behavior]++;
            }
            if (r() < fgtRate) {
               forgetCount[behavior]++;
            }
         }
      }
      /*
      deletion
      addition
      duplication
      shift
      relaxation
      discover
      forget
      swap
      permutation
      */
      if (del != null) {
         System.out.println("del");
         for (Node node : del) {
            sb.removeSubtreeAt(node);
         }
      }
      if (add != null) {
         System.out.println("add");
         for (Node node : add) {
            sb.addGrowthTo(node);
         }
      }
      if (dup != null) {
         System.out.println("dup");
         for (Node node : dup) {
            sb.duplicateSubtreeAt(node);
         }
      }
      if (tlc != null) {
         System.out.println("tlc");
         for (Node node : tlc) {
            sb.randomlyShiftSubtreeAt(node);
         }
      }
      if (relaxCount > 0) {
         sb.settle(relaxCount);
      }
      for (int behavior = 0; behavior < 3; behavior++) {
         if (discoverCount[behavior] > 0) {
            sb.stroke.blendActionsWith(behavior, sb.makeStrokeActionList(discoverCount[behavior]));
         }
         if (forgetCount[behavior] > 0) {
            sb.stroke.removeActions(behavior, forgetCount[behavior]);
         }
         if (permute[behavior]) {
            sb.stroke.shuffle(behavior);
         } else if (swp != null) {
            for (int j = 0; j < 3; j++) {
               List<Integer> swpj = swp.get(j);
               for (Integer actionIndex : swpj) {
                  sb.stroke.randomlySwap(behavior, actionIndex);
               }
            }
         }
      }
   }

   private float r() {
      return rand.nextFloat();
   }

   private float zop(float input, float scale) {
      if (r() < mutationRate) return input - scale + r() * 2 * scale;
      else return input;
   }

   private float zap(float input, float minValue) {
      if (input < 0) {
         System.out.println("won't zap a negative number with a minValue: " + input);
         return input;
      }
      minValue = minValue == 0 ? input / 100f : minValue;

      if (r() < mutationRate) {
         float x = (r() * 2 * input);
         return x < minValue ? x + minValue : x;
      } else return input;
   }

   private float zap(float input, float minValue, float maxValue) {
      if (input < 0) {
         System.out.println("won't zap a negative number with a minValue: " + input);
         return input;
      }
      if (maxValue <= minValue) {
         System.out.println("can't zap when max<=min: " + maxValue + "<=" + minValue);
         return input;
      }
      minValue = minValue == 0 ? input / 100f : minValue;
      if (r() < mutationRate) {
         float x = (r() * 2 * input);
         int count = 0;
         while (x > maxValue || x < minValue || count > 10) {
            if (x > maxValue) {
               x -= maxValue;
            } else {
               x += minValue;
            }
            count++;
         }
         return x;
      } else return input;
   }

   private float zap(float input, float absMin, float absMax, float scale) {
      if (input < 0) {
         System.out.println("won't zap a negative number with a minValue: " + input);
         return input;
      }
      if (absMax <= absMin) {
         System.out.println("can't zap when max<=min: " + absMax + "<=" + absMin);
         return input;
      }
      absMin = absMin == 0 ? input / 100f : absMin;
      if (r() < mutationRate) {
         float x = input + (p.random(2 * scale) - scale);
         if (x > absMax) {
            x = absMax;
         } else if (x < absMin) {
            x = absMin;
         }
         return x;
      } else return input;
   }

   public SquishyBody parseSquishyBody(boolean sloppy) {
      this.reset();
      float bstr, mstr, tstr, minl, maxl, branchp, extp, invnodep;
      match(LEFT_CURLY);
      match(STR);
      match(LEFT_SQUARE);
      bstr = Float.parseFloat(currentToken.data);
      match(FLOAT);
      mstr = Float.parseFloat(currentToken.data);
      match(FLOAT);
      tstr = Float.parseFloat(currentToken.data);
      match(FLOAT);
      match(RIGHT_SQUARE);
      match(LEN);
      match(LEFT_SQUARE);
      minl = Float.parseFloat(currentToken.data);
      match(FLOAT);
      maxl = Float.parseFloat(currentToken.data);
      match(FLOAT);
      match(RIGHT_SQUARE);
      match(BRANCH_P);
      branchp = Float.parseFloat(currentToken.data);
      match(FLOAT);
      match(EXTEND_P);
      extp = Float.parseFloat(currentToken.data);
      match(FLOAT);
      match(INV_NODE_P);
      invnodep = Float.parseFloat(currentToken.data);
      match(FLOAT);
      Tree tree = parseTreeNoReset(sloppy);
      match(RIGHT_CURLY);
      match(EOF);
      SquishyBody ret = new SquishyBody(tree, p, world);
      if (sloppy) {
         minl = zap(minl, 5);
         maxl = zap(maxl, minl);
         ret.setConstants(
               zap(bstr, mstr, 1), zap(mstr, tstr, bstr), zap(tstr, 0, mstr),
               minl, maxl,
               zap(branchp, 0), zap(extp, 0), zap(invnodep, 0));
      } else {
         ret.setConstants(bstr, mstr, tstr, minl, maxl, branchp, extp, invnodep);
      }
      return ret;
   }

   Tree parseTree(boolean sloppy) {
      this.reset();
      return parseTreeNoReset(sloppy);
   }

   private Tree parseTreeNoReset(boolean sloppy) {
      match(FLOAT);
      parseNodeSet(sloppy);
      match(FLOAT);
      parseSpringSet(sloppy);
      parseStroke(sloppy);
      for (Node node : globalNodeSet) {
         world.addParticle(node);
         node.scaleVelocity(0);
      }
      for (Spring spring : globalSpringSet) {
         world.addSpring(spring);
      }
      return new Tree(globalNodeSet, globalSpringSet, globalStroke, p, world);
   }

   private void parseStroke(boolean sloppy) {
      match(LEFT_CURLY);
      List<List<StrokeAction>> strokeActionList = new ArrayList<>();
      int[] intervals = new int[3];
      for (int i = 0; i < 3; i++) {
         globalStrokeList = new LinkedList<>();
         match(LEFT_SQUARE);
         int strkLsti = Integer.parseInt(currentToken.data);
         match(FLOAT);
         match(COLON);
         int interval = Integer.parseInt(currentToken.data);
         if (sloppy) {
            interval = (int) zap(interval, 2);
         }
         intervals[strkLsti] = interval;
         match(FLOAT);
         match(RIGHT_SQUARE);
         parseStrokeSet(sloppy);
         strokeActionList.add(strkLsti, globalStrokeList);
      }
      globalStroke = new Stroke(strokeActionList, intervals);
      match(RIGHT_CURLY);
   }

   private void parseStrokeSet(boolean sloppy) {
      switch (currentToken.type) {
         case EOF:
            //epsilon production
            break;
         case LEFT_CURLY:
            match(LEFT_CURLY);
            parseStrokeList(sloppy);
            match(RIGHT_CURLY);
            break;
         default:
            System.out.println("Something went wrong in parseStrokeSet");
            break;
      }
   }

   private void parseStrokeList(boolean sloppy) {
      switch (currentToken.type) {
         case RIGHT_CURLY:
            //epsilon production
            break;
         case LEFT_ROUND:
            match(LEFT_ROUND);
            parseStrokeAction(sloppy);
            match(RIGHT_ROUND);
            parseStrokeList(sloppy);
            break;
         default:
            System.out.println("uh parseStrokeList() something bad happened you know the drill");
            break;
      }
   }

   private StrokeAction parseStrokeAction(boolean sloppy) {
      SquishyBodyPart part = null;
      switch (currentToken.type) {
         case MUSCLE_NAME:
            part = hashedSquishyParts.get(currentToken.data);
            match(MUSCLE_NAME);
            break;
         case NODE_NAME:
            part = hashedSquishyParts.get(currentToken.data);
            match(NODE_NAME);
            break;
         default:
            System.out.println("AH WHAT HAPPENED parseStrokeAction()");
            break;
      }
      float state = Float.parseFloat(currentToken.data);
      if (sloppy) {
         state = zap(state, 0, 1);
      }
      match(FLOAT);
      StrokeAction ret = new StrokeAction(part, state);
      globalStrokeList.add(ret);
      return ret;
   }

   private Set<Spring> parseSpringSet(boolean sloppy) {
      match(LEFT_CURLY);
      parseSpringList(sloppy);
      match(RIGHT_CURLY);
      return globalSpringSet;
   }

   private void parseSpringList(boolean sloppy) {
      switch (currentToken.type) {
         case RIGHT_CURLY:
            //epsilon production
            break;
         case BONE_NAME:
         case MUSCLE_NAME:
         case TISSUE_NAME:
            globalSpringSet.add(parseSpring(sloppy));
            parseSpringList(sloppy);
            break;
         default:
            System.out.println("Parse error in parseSpringList() lol");
            break;
      }
   }

   private Spring parseSpring(boolean sloppy) {
      Token t = currentToken;
      switch (t.type) {
         case BONE_NAME:
            match(BONE_NAME);
            break;
         case MUSCLE_NAME:
            match(MUSCLE_NAME);
            break;
         case TISSUE_NAME:
            match(TISSUE_NAME);
            break;
         default:
            System.out.println("Parse error: " + currentToken + " aint even a spring name yo. Love, parseSpring()");
            break;
      }

      match(LEFT_ROUND);
      Node a = ((Node) hashedSquishyParts.get(currentToken.data));
      match(NODE_NAME);
      match(COLON);
      Node b = ((Node) hashedSquishyParts.get(currentToken.data));
      match(NODE_NAME);
      match(RIGHT_ROUND);

      float[] constants = parseSpringData(sloppy);

      Spring parsedSpring = null;
      float l = a.normal.distanceTo(b.normal);

      switch (t.type) {
         case BONE_NAME:
            parsedSpring = new Spring(a, b, Spring.Type.BONE,
                  l, l, constants[2], p);
            break;
         case MUSCLE_NAME:
            parsedSpring = new Spring(a, b, Spring.Type.MUSCLE,
                  l * 0.5f, l * 1.5f, constants[2], p);
            break;
         case TISSUE_NAME:
            parsedSpring = new Spring(a, b, Spring.Type.TISSUE,
                  l, l, constants[2], p);
            break;
         default:
            break;
      }
      hashedSquishyParts.put(t.data, parsedSpring);
      parsedSpring.completeConnection();
      world.addSpring(parsedSpring);
      return parsedSpring;
   }

   private float[] parseSpringData(boolean sloppy) {
      float[] ret = new float[3];
      match(LEFT_SQUARE);
      ret[0] = Float.parseFloat(currentToken.data);
      match(FLOAT);
      ret[1] = Float.parseFloat(currentToken.data);
      match(FLOAT);
      ret[2] = Float.parseFloat(currentToken.data);
      match(FLOAT);
      match(RIGHT_SQUARE);
      return ret;
   }

   private Set<Node> parseNodeSet(boolean sloppy) {
      return parseNodeSet(new LinkedHashSet<>(), sloppy);
   }

   private Set<Node> parseNodeSet(Set<Node> currentNodeSet, boolean sloppy) {
      match(LEFT_CURLY);
      parseNodeList(currentNodeSet, sloppy);
      match(RIGHT_CURLY);
      return currentNodeSet;
   }

   private Set<Node> parseNodeList(Set<Node> currentNodeSet, boolean sloppy) {
      switch (currentToken.type) {
         case RIGHT_CURLY:
            //epsilon production
            break;
         case NODE_NAME:
            currentNodeSet.add(parseNode(sloppy));
            parseNodeList(currentNodeSet, sloppy);
            break;
         default:
            System.out.println("Parse error all up in parseNodeList()");
            break;
      }
      return currentNodeSet;
   }

   private Node parseNode(boolean sloppy) {
      Token t = currentToken;
      if (match(NODE_NAME)) {
         Node parsedNode = new Node(p);
         hashedSquishyParts.put(t.data, parsedNode);
         globalNodeSet.add(parsedNode);
         parseAndSetNodeData(parsedNode, sloppy);
         parsedNode.boneChildren = parseNodeSet(sloppy);
         return parsedNode;
      } else {
         System.out.println("Parse error: " + currentToken + " aint even a node name yo. Love, parseNode()");
         return null;
      }

   }

   private void parseAndSetNodeData(Node parsedNode, boolean sloppy) {
      match(LEFT_SQUARE);
      String sX, sY, sW, fill;
      sX = currentToken.data;
      match(FLOAT);
      sY = currentToken.data;
      match(FLOAT);
      sW = currentToken.data;
      match(FLOAT);
      switch (currentToken.type) {
         case FLOAT:
            fill = currentToken.data;
            match(FLOAT);
            fill = fill.length() == 6 ? "ff" + fill : fill;
            break;
         case HEX:
            fill = currentToken.data;
            match(HEX);
            fill = fill.length() == 6 ? "ff" + fill : fill;
            break;
         case RIGHT_SQUARE:
            fill = "";
            break;
         default:
            System.out.println("Why is " + currentToken.data + " being read by parseAndSetNodeData()?");
            return;
      }
      match(RIGHT_SQUARE);

      float fX = Float.parseFloat(sX);
      float fY = Float.parseFloat(sY);
      float fW = Float.parseFloat(sW);

      if (sloppy) {
         Rect wb = world.getWorldBounds();
         if (wb != null) {
            fX = zap(fX, wb.getLeft(), wb.getRight(), 10);
            fY = zap(fY, wb.getTop(), wb.getBottom(), 10);
         } else {
            fX = zop(fX, 10);
            fY = zop(fY, 10);
         }
         fW = zap(fW, 0);
      }

      if (fill.isEmpty()) {
         parsedNode.updateData(fX, fY, fW);
      } else {
         parsedNode.updateData(fX, fY, fW, Integer.parseUnsignedInt(fill, 16));
      }

      /*
      if (fill.isEmpty()) {
         if (sloppy) {
            float fX = Float.parseFloat(sX);
            float fY = Float.parseFloat(sY);
            float fW = Float.parseFloat(sW);
            Rect wb =world.getWorldBounds();
            if (wb != null) {
               fX = zap(fX, wb.getLeft(), wb.getRight(), 10);
               fY = zap(fY, wb.getTop(), wb.getBottom(), 10);
            } else {
               fX = zop(fX, 10);
               fY = zop(fY, 10);
            }
               fW = zap(fW, 0);
            parsedNode.updateData(fX, fY, fW);
         } else {
            parsedNode.updateData(Float.parseFloat(sX), Float.parseFloat(sY), Float.parseFloat(sW));
         }
      } else {
         if (fill.length() == 6) {
            fill = "ff" + fill;
         }
         if (sloppy) {
            float fX = Float.parseFloat(sX);
            float fY = Float.parseFloat(sY);
            float fW = Float.parseFloat(sW);
            Rect wb =world.getWorldBounds();
            if (wb != null) {
               fX = zap(fX, wb.getLeft(), wb.getRight(), 10);
               fY = zap(fY, wb.getTop(), wb.getBottom(), 10);
            } else {
               fX = zop(fX, 10);
               fY = zop(fY, 10);
            }
            fW = zap(fW, 0);
            parsedNode.updateData(fX, fY, fW, Integer.parseUnsignedInt(fill, 16));
         } else {
            parsedNode.updateData(Float.parseFloat(sX), Float.parseFloat(sY),
                  Float.parseFloat(sW), Integer.parseUnsignedInt(fill, 16));
         }
      }
      */
   }

   private boolean match(TokenType expectedType) {
      // TODO: make this little booger throw a real exception for incorrect type
      if (currentToken == null) {
         System.out.println("ERROR: null current token");
         return false;
      }
      if (expectedType != currentToken.type) {
         System.out.println("PARSE ERROR: was expecting " + expectedType + " but recieved a big ol buttload of " + currentToken);
         return false;
      }
      if (expectedType != EOF) {
         currentToken = tokenIterator.next();
      }
      return true;
   }

   enum TokenType { //"Token Type"
      LEFT_CURLY, RIGHT_CURLY,
      LEFT_SQUARE, RIGHT_SQUARE,
      LEFT_ROUND, RIGHT_ROUND,
      COLON,
      NODE_NAME,
      BONE_NAME, MUSCLE_NAME, TISSUE_NAME,
      FLOAT, HEX,
      NUM_NODES, NUM_SPRINGS, NUM_STROKES,
      STR, LEN,
      BRANCH_P, EXTEND_P, INV_NODE_P,
      INTERVAL,
      UNRECOGNIZED, EOF
   }

   private class Token {
      String data;
      TokenType type;

      Token(String data) {
         this.data = data;
         this.type = determineVal(data);
      }

      private TokenType determineVal(String data) {
         switch (data) {
            case "{":
               return LEFT_CURLY;
            case "}":
               return RIGHT_CURLY;
            case "[":
               return LEFT_SQUARE;
            case "]":
               return RIGHT_SQUARE;
            case "(":
               return LEFT_ROUND;
            case ":":
               return COLON;
            case ")":
               return RIGHT_ROUND;
            case "EOF": //eof must be passed in as literally "EOF"
               return EOF;
            case "#nodes":
               return NUM_NODES;
            case "#springs":
               return NUM_SPRINGS;
            case "#strokes":
               return NUM_STROKES;
            case "strength":
               return STR;
            case "branching-prob":
               return BRANCH_P;
            case "bone-length":
               return LEN;
            case "executeNextStrokeAction-extend-prob":
               return EXTEND_P;
            case "involves-node-prob":
               return INV_NODE_P;
            case "executeNextStrokeAction-interval":
               return INTERVAL;
         }
         if (data.matches("@\\d+")) { //node prefix @, followed by digits
            return NODE_NAME;
         }
         if (data.matches("\\$B\\d+")) {
            return BONE_NAME;
         }
         if (data.matches("\\$M\\d+")) {
            return MUSCLE_NAME;
         }
         if (data.matches("\\$T\\d+")) {
            return TISSUE_NAME;
         }
         if (data.matches("-?(\\d*\\.)?\\d+((e|E)-?\\d+)?")) {
            return FLOAT;
         }
         if (data.matches("(0(x|X))?(\\p{XDigit})+")) {
            return HEX;
         }
         return UNRECOGNIZED;
      }

      @Override
      public String toString() {
         return "{" + type + " \"" + data + "\"}";
      }
   }
}
