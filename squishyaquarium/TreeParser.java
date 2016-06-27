package squishyaquarium;

import processing.core.PApplet;
import toxi.physics2d.VerletPhysics2D;

import java.util.*;

import static squishyaquarium.TreeParser.TokenType.HEX;

/**
 * Created by kvloxx 44
 */
public class TreeParser {
   PApplet p;
   VerletPhysics2D world;
   String input;
   ArrayList<Token> tokens;
   Token currentToken;
   Iterator<Token> tokenIterator;
   HashMap<String, SquishyBodyPart> hashedSquishyParts;
   Set<Node> globalNodeSet;
   Set<Spring> globalSpringSet;
   List<StrokeAction> globalStrokeList;

   enum TokenType {
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

   TreeParser(String input, PApplet p, VerletPhysics2D world) {
      this.p = p;
      this.world = world;
      this.input = input;
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

   SquishyBody parseSquishyBody() {
      float bonestr, musclestr, tissuestr, minlen, maxlen, branchp, extp, invnodep;
      int numnodes, numsprings, numstrokes, interval;
      match(TokenType.LEFT_CURLY);
      match(TokenType.NUM_NODES);
      numnodes = Integer.parseInt(currentToken.data, 10);
      match(TokenType.FLOAT);
      match(TokenType.NUM_SPRINGS);
      numsprings = Integer.parseInt(currentToken.data, 10);
      match(TokenType.FLOAT);
      match(TokenType.NUM_STROKES);
      numstrokes = Integer.parseInt(currentToken.data, 10);
      match(TokenType.FLOAT);
      match(TokenType.STR);
      match(TokenType.LEFT_SQUARE);
      bonestr = Float.parseFloat(currentToken.data);
      match(TokenType.FLOAT);
      musclestr = Float.parseFloat(currentToken.data);
      match(TokenType.FLOAT);
      tissuestr = Float.parseFloat(currentToken.data);
      match(TokenType.FLOAT);
      match(TokenType.RIGHT_SQUARE);
      match(TokenType.LEN);
      match(TokenType.LEFT_SQUARE);
      minlen = Float.parseFloat(currentToken.data);
      match(TokenType.FLOAT);
      maxlen = Float.parseFloat(currentToken.data);
      match(TokenType.FLOAT);
      match(TokenType.RIGHT_SQUARE);
      match(TokenType.BRANCH_P);
      branchp = Float.parseFloat(currentToken.data);
      match(TokenType.FLOAT);
      match(TokenType.EXTEND_P);
      extp = Float.parseFloat(currentToken.data);
      match(TokenType.FLOAT);
      match(TokenType.INV_NODE_P);
      invnodep = Float.parseFloat(currentToken.data);
      match(TokenType.FLOAT);
      match(TokenType.INTERVAL);
      interval = Integer.parseInt(currentToken.data, 10);
      match(TokenType.FLOAT);
      Tree tree = parseTree();
      match(TokenType.RIGHT_CURLY);
      match(TokenType.EOF);
      SquishyBody ret = new SquishyBody(tree, p, world);
      ret.setConstants(bonestr, musclestr, tissuestr, minlen, maxlen, branchp, extp, invnodep, interval);
      return ret;
   }

   Tree parseTree() {
      parseNodeSet();
      parseSpringSet();
      parseStrokeSet();
      for (Node node : globalNodeSet) {
         world.addParticle(node);
      }
      for (Spring spring : globalSpringSet) {
         world.addSpring(spring);
      }
      return new Tree(globalNodeSet, globalSpringSet, globalStrokeList);
   }

   private void parseStrokeSet() {
      switch (currentToken.type) {
         case EOF:
            //epsilon production
            break;
         case LEFT_CURLY:
            match(TokenType.LEFT_CURLY);
            parseStrokeList();
            match(TokenType.RIGHT_CURLY);
            break;
         default:
            System.out.println("Something went wrong in parseStrokeSet");
            break;
      }
   }

   private void parseStrokeList() {
      switch (currentToken.type) {
         case RIGHT_CURLY:
            //epsilon production
            break;
         case LEFT_ROUND:
            match(TokenType.LEFT_ROUND);
            parseStroke();
            match(TokenType.RIGHT_ROUND);
            parseStrokeList();
            break;
         default:
            System.out.println("uh parseStrokeList() something bad happened you know the drill");
            break;
      }
   }

   private StrokeAction parseStroke() {
      SquishyBodyPart part = null;
      switch (currentToken.type) {
         case MUSCLE_NAME:
            part = hashedSquishyParts.get(currentToken.data);
            match(TokenType.MUSCLE_NAME);
            break;
         case NODE_NAME:
            part = hashedSquishyParts.get(currentToken.data);
            match(TokenType.NODE_NAME);
            break;
         default:
            System.out.println("AH WHAT HAPPENED parseStroke()");
            break;
      }
      float state = Float.parseFloat(currentToken.data);
      match(TokenType.FLOAT);
      StrokeAction ret = new StrokeAction(part, state);
      globalStrokeList.add(ret);
      return ret;
   }

   private Set<Spring> parseSpringSet() {
      match(TokenType.LEFT_CURLY);
      parseSpringList();
      match(TokenType.RIGHT_CURLY);
      return globalSpringSet;
   }

   private void parseSpringList() {
      switch (currentToken.type) {
         case RIGHT_CURLY:
            //epsilon production
            break;
         case BONE_NAME:
         case MUSCLE_NAME:
         case TISSUE_NAME:
            globalSpringSet.add(parseSpring());
            parseSpringList();
            break;
         default:
            System.out.println("Parse error in parseSpringList() lol");
            break;
      }
   }

   private Spring parseSpring() {
      Token t = currentToken;
      switch (t.type) {
         case BONE_NAME:
            match(TokenType.BONE_NAME);
            break;
         case MUSCLE_NAME:
            match(TokenType.MUSCLE_NAME);
            break;
         case TISSUE_NAME:
            match(TokenType.TISSUE_NAME);
            break;
         default:
            System.out.println("Parse error: " + currentToken + " aint even a spring name yo. Love, parseSpring()");
            break;
      }

      match(TokenType.LEFT_ROUND);
      Node a = ((Node) hashedSquishyParts.get(currentToken.data));
      match(TokenType.NODE_NAME);
      match(TokenType.COLON);
      Node b = ((Node) hashedSquishyParts.get(currentToken.data));
      match(TokenType.NODE_NAME);
      match(TokenType.RIGHT_ROUND);

      float[] constants = parseSpringData();

      Spring parsedSpring = null;

      switch (t.type) {
         case BONE_NAME:
            parsedSpring = new Spring(a, b, Spring.Type.BONE,
                  constants[0], constants[1], constants[2], p);
            a.addBoneChild(b);
            a.attachSpring(parsedSpring);
            b.addBoneParent(a);
            break;
         case MUSCLE_NAME:
            parsedSpring = new Spring(a, b, Spring.Type.MUSCLE,
                  constants[0], constants[1], constants[2], p);
            a.addMuscleNeighbor(b);
            a.attachSpring(parsedSpring);
            b.addMuscleNeighbor(a);
            b.attachSpring(parsedSpring);
            break;
         case TISSUE_NAME:
            parsedSpring = new Spring(a, b, Spring.Type.TISSUE,
                  constants[0], constants[1], constants[2], p);
            a.addTissueNeighbor(b);
            a.attachSpring(parsedSpring);
            b.addTissueNeighbor(a);
            b.attachSpring(parsedSpring);
            break;
         default:
            break;
      }
      hashedSquishyParts.put(t.data, parsedSpring);
      return parsedSpring;
   }

   private float[] parseSpringData() {
      float[] ret = new float[3];
      match(TokenType.LEFT_SQUARE);
      ret[0] = Float.parseFloat(currentToken.data);
      match(TokenType.FLOAT);
      ret[1] = Float.parseFloat(currentToken.data);
      match(TokenType.FLOAT);
      ret[2] = Float.parseFloat(currentToken.data);
      match(TokenType.FLOAT);
      match(TokenType.RIGHT_SQUARE);
      return ret;
   }

   private Set<Node> parseNodeSet() {
      return parseNodeSet(new LinkedHashSet<>());
   }

   private Set<Node> parseNodeSet(Set<Node> currentNodeSet) {
      match(TokenType.LEFT_CURLY);
      parseNodeList(currentNodeSet);
      match(TokenType.RIGHT_CURLY);
      return currentNodeSet;
   }

   private Set<Node> parseNodeList(Set<Node> currentNodeSet) {
      switch (currentToken.type) {
         case RIGHT_CURLY:
            //epsilon production
            break;
         case NODE_NAME:
            currentNodeSet.add(parseNode());
            parseNodeList(currentNodeSet);
            break;
         default:
            System.out.println("Parse error all up in parseNodeList()");
            break;
      }
      return currentNodeSet;
   }

   private Node parseNode() {
      Token t = currentToken;
      if (match(TokenType.NODE_NAME)) {
         Node parsedNode = new Node(p);
         hashedSquishyParts.put(t.data, parsedNode);
         globalNodeSet.add(parsedNode);
         parseAndSetNodeData(parsedNode);
         parsedNode.boneChildren = parseNodeSet();
         System.out.println("parsedNode = " + parsedNode);
         return parsedNode;
      } else {
         System.out.println("Parse error: " + currentToken + " aint even a node name yo. Love, parseNode()");
         return null;
      }

   }

   private void parseAndSetNodeData(Node parsedNode) {
      match(TokenType.LEFT_SQUARE);
      String sX, sY, sW, fill;
      sX = currentToken.data;
      match(TokenType.FLOAT);
      sY = currentToken.data;
      match(TokenType.FLOAT);
      sW = currentToken.data;
      match(TokenType.FLOAT);
      switch (currentToken.type) {
         case FLOAT:
         case HEX:
            fill = currentToken.data;
            match(TokenType.HEX);
            break;
         case RIGHT_SQUARE:
            fill = "";
            break;
         default:
            fill=null;
            System.out.println("Why is " + currentToken.data + " being read by parseAndSetNodeData()?");
            break;
      }
      match(TokenType.RIGHT_SQUARE);
      if (fill.isEmpty()) {
         parsedNode.updateData(Float.parseFloat(sX), Float.parseFloat(sY), Float.parseFloat(sW));
      } else {
         parsedNode.updateData(Float.parseFloat(sX), Float.parseFloat(sY),
               Float.parseFloat(sW), Integer.parseUnsignedInt(fill, 16));
      }
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
      if (expectedType != TokenType.EOF) {
         currentToken = tokenIterator.next();
      }
      return true;
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
               return TokenType.LEFT_CURLY;
            case "}":
               return TokenType.RIGHT_CURLY;
            case "[":
               return TokenType.LEFT_SQUARE;
            case "]":
               return TokenType.RIGHT_SQUARE;
            case "(":
               return TokenType.LEFT_ROUND;
            case ":":
               return TokenType.COLON;
            case ")":
               return TokenType.RIGHT_ROUND;
            case "EOF": //eof must be passed in as literally "EOF"
               return TokenType.EOF;
            case "#nodes":
               return TokenType.NUM_NODES;
            case "#springs":
               return TokenType.NUM_SPRINGS;
            case "#strokes":
               return TokenType.NUM_STROKES;
            case "strength":
               return TokenType.STR;
            case "branching-prob":
               return TokenType.BRANCH_P;
            case "bone-length":
               return TokenType.LEN;
            case "stroke-extend-prob":
               return TokenType.EXTEND_P;
            case "involves-node-prob":
               return TokenType.INV_NODE_P;
            case "stroke-interval":
               return TokenType.INTERVAL;
         }
         if (data.matches("@\\d+")) { //node prefix @, followed by digits
            return TokenType.NODE_NAME;
         }
         if (data.matches("\\$B\\d+")) {
            return TokenType.BONE_NAME;
         }
         if (data.matches("\\$M\\d+")) {
            return TokenType.MUSCLE_NAME;
         }
         if (data.matches("\\$T\\d+")) {
            return TokenType.TISSUE_NAME;
         }
         if (data.matches("-?(\\d*\\.)?\\d+((e|E)-?\\d+)?")) {
            return TokenType.FLOAT;
         }
         if (data.matches("(0(x|X))?(\\p{XDigit})+")) {
            return TokenType.HEX;
         }
         return TokenType.UNRECOGNIZED;
      }

      @Override
      public String toString() {
         return "{" + type + " \"" + data + "\"}";
      }
   }
}
