package squishyaquarium;

import processing.core.PApplet;
import toxi.physics2d.VerletPhysics2D;

import java.util.*;

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


   enum TokenType {
      LEFT_CURLY, RIGHT_CURLY,
      LEFT_SQUARE, RIGHT_SQUARE,
      LEFT_ROUND, RIGHT_ROUND,
      COLON,
      NODE_NAME,
      BONE_NAME, MUSCLE_NAME, TISSUE_NAME,
      FLOAT, EOF,
      UNRECOGNIZED
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

   }

   SquishyBody parseSubtree() {
      Set<Node> nset = parseNodeSet();
      Set<Spring> sset=parseSpringSet();
      match(TokenType.EOF);
      for (Node node : nset) {
         world.addParticle(node);
      }
      for (Spring spring : sset) {
         world.addSpring(spring);
      }
      return new SquishyBody(nset, sset, p, world);
   }

   private Set<Spring> parseSpringSet() {
      match(TokenType.LEFT_CURLY);
      Set<Spring> ret = new LinkedHashSet<>();
      parseSpringList(ret);
      match(TokenType.RIGHT_CURLY);
      return ret;
   }

   private void parseSpringList(Set<Spring> currentSpringSet) {

      switch (currentToken.type) {
         case RIGHT_CURLY:
            //epsilon production
            break;
         case BONE_NAME:
         case MUSCLE_NAME:
         case TISSUE_NAME:
            currentSpringSet.add(parseSpring());
            parseSpringList(currentSpringSet);
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
      hashedSquishyParts.put(t.data, parsedSpring);

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
      }
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
      match(TokenType.LEFT_CURLY);
      Set<Node> ret = new LinkedHashSet<>();
      parseNodeList(ret);

      match(TokenType.RIGHT_CURLY);
      return ret;
   }

   private void parseNodeList(Set<Node> currentNodeSet) {
      switch (currentToken.type) {
         case RIGHT_CURLY:
            //epsilon production
            break;
         case NODE_NAME:
            Node parsedNode=parseNode();
            currentNodeSet.add(parsedNode);
            currentNodeSet.addAll(parsedNode.boneChildren);
            parseNodeList(currentNodeSet);
            break;
         default:
            System.out.println("Parse error all up in parseNodeList()");
            break;
      }
   }

   private Node parseNode() {
      Token t = currentToken;
      if (match(TokenType.NODE_NAME)) {
         Node parsedNode = new Node(p);
         hashedSquishyParts.put(t.data, parsedNode);
         parseNodeData(parsedNode);
         parsedNode.boneChildren = parseNodeSet();
         return parsedNode;
      } else {
         System.out.println("Parse error: " + currentToken + " aint even a node name yo. Love, parseNode()");
         return null;
      }

   }

   private void parseNodeData(Node parsedNode) {
      match(TokenType.LEFT_SQUARE);
      String sX, sY, sW;
      sX = currentToken.data;
      match(TokenType.FLOAT);
      sY = currentToken.data;
      match(TokenType.FLOAT);
      sW = currentToken.data;
      match(TokenType.FLOAT);
      match(TokenType.RIGHT_SQUARE);
      parsedNode.updateData(Float.parseFloat(sX), Float.parseFloat(sY), Float.parseFloat(sW));
   }

   boolean match(TokenType expectedType) {
      // TODO: make this little booger throw a real exception for incorrect type
      if (currentToken == null) {
         System.out.println("ERROR: null current token");
         return false;
      }
      if (expectedType != currentToken.type) {
         System.out.println("PARSE ERROR: was expecting " + expectedType + " but recieved a big ol buttload of " + currentToken);
         return false;
      }
      if (expectedType!=TokenType.EOF)
         currentToken = tokenIterator.next();

      return true;
   }


   class Token {
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
         if (data.matches("(\\d+\\.)?\\d+")) {
            return TokenType.FLOAT;
         }
         return TokenType.UNRECOGNIZED;
      }

      @Override
      public String toString() {
         return "{" + type + " \"" + data + "\"}";
      }
   }
}
