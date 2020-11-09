import java.util.*;
import java.io.*;

/**
* A Card Game implementation which uses the outer class CardGame and the nested
* inner classes Player, Card and CardDeck. The static class main takes the
* packTxtFileName and playerNo from the user at the console and uses these to
* start a game.
*
* @075397 and (Ken's candidate number) @21/10/2020
*/
public class CardGame {

  String packTxtFileName;
  int playerNo;
  static boolean gameOver = false;
  static int winningPlayer;

  /**
  * The constructor method for creating a CardGame object.
  *
  * @param packTxtFileName the name of the text file containing the pack data.
  * @param playerNo        the number of players playing the card game.
  */

  public CardGame(String packTxtFileName, int playerNo) {
    this.packTxtFileName = packTxtFileName;
    this.playerNo = playerNo;
  }

  /**
  * The getter method to retrieve the value of packTxtFileName.
  *
  * @return packTxtFileName.
  */

  public String getPackTxtFileName() {
    return packTxtFileName;
  }

  /**
  * The getter method to retrieve the value of playerNo.
  *
  * @return playerNo.
  */

  public int getPlayerNo() {
    return playerNo;
  }

  public static void main(String[] args) {
    // Instantiate necessary object arrays.
    ArrayList<Player> players = new ArrayList<Player>();
    ArrayList<CardDeck> decks = new ArrayList<CardDeck>();
    ArrayList<Card> pack = new ArrayList<Card>();

    boolean isValid = true;
    final int handSize = 4;
    BufferedWriter writer;

    // Take input from the console to retrieve playerNo and packTxtFileName. NOTE:
    // MIGHT WANT TO TRY CATCHING EXCEPTIONS HERE FOR FILE NOT FOUND AND NON-INTEGER
    // ENTERED.
    Scanner scanner = new Scanner(System.in);
    System.out.print("Enter the number of players: ");
    int playerNo = scanner.nextInt();
    System.out.print("Enter the name of the pack text file (excluding the file extension): ");
    String packTxtFileName = scanner.next();

    // Read the pack text file and add each row to an ArrayList.
    try {
      Scanner read = new Scanner(new File(packTxtFileName + ".txt"));
      ArrayList<Card> temp = new ArrayList<Card>();
      while (read.hasNextInt()) {
        Card newCard = new Card(read.nextInt());
        temp.add(newCard);
        pack = temp;
      }
      read.close();
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }

    // Check the ArrayList only has non-negative integers.
    for (int i = 0; i < pack.size(); i++) {
      if (pack.get(i).getValue() < 0) {
        isValid = false;
        break;
      } else {
        continue;
      }
    }

    // Check the ArrayList is of the appropriate size.
    if (pack.size() != 8 * playerNo) {
      isValid = false;
    }

    // Inform the user of import success or failure.
    if (isValid) {
      System.out.println("Pack file is valid and has been imported.");
    } else {
      System.out.println("Pack file is invalid and has not been imported. Please provide a valid pack file.");
      return;
    }

    // Allow the user to shuffle the pack.
    System.out.print("Would you like to shuffle the pack? (y/n): ");
    String shuffle = scanner.next().toLowerCase();
    boolean shouldShuffle = false;

    switch (shuffle) {
      case "y":
      shouldShuffle = true;
      break;
      case "n":
      shouldShuffle = false;
      break;
      case "yes":
      shouldShuffle = true;
      break;
      case "no":
      shouldShuffle = false;
      break;
    }

    if (shouldShuffle == true) {
      Collections.shuffle(pack);
    }

    // Instantiate the deck objects and add them to the decks array.
    for (int i = 0; i < (playerNo); i++) {
      CardDeck newDeck = new CardDeck(i + 1, new ArrayList<Card>());
      decks.add(newDeck);
    }

    // Instantiate the player objects and add them to the players array.
    for (int i = 0; i < (playerNo); i++) {
      if (i == playerNo - 1) {
        Player newPlayer = new Player(i + 1, new ArrayList<Card>(), decks.get(i), decks.get(0));
        players.add(newPlayer);
      } else {
        Player newPlayer = new Player(i + 1, new ArrayList<Card>(), decks.get(i), decks.get(i + 1));
        players.add(newPlayer);
      }
    }

    // Deal the cards from the pack to the players in a round-robin style.
    for (int x = 0; x < handSize; x++) {
      for (int i = 0; i < players.size(); i++) {
        players.get(i).hand.add(pack.get(0));
        pack.remove(0);
      }
    }

    // Deal the remaining cards from the pack to the decks in a round-robin fashion.
    while (!pack.isEmpty()) {
      for (int i = 0; i < decks.size(); i++) {
        decks.get(i).cards.add(pack.get(0));
        pack.remove(0);
      }
    }

    // Create the output files for each player detailing their actions and output
    // their initial hand.
    for (int i = 0; i < players.size(); i++) {
      try {
        File file = new File("player"+ String.valueOf(players.get(i).getPlayerIndex()) + ".txt");
        file.createNewFile();
        ArrayList<Integer> initialHand = new ArrayList<Integer>();
        for (int x = 0; x < players.get(i).getHand().size(); x++) {
          initialHand.add(players.get(i).getHand().get(x).getValue());
        }
        FileWriter wr = new FileWriter("player"+ String.valueOf(players.get(i).getPlayerIndex()) + ".txt");
        writer = new BufferedWriter(wr);
        writer.write("Player " + String.valueOf(players.get(i).getPlayerIndex()) + " initial hand "
        + initialHand.toString());
        writer.close();
      } catch (Exception e) {
        System.out.println("An error occurred.");
        e.printStackTrace();
      }
    }

    // Check for win conditions amongst the player's initial hand.
    for (int i = 0; i < players.size(); i++) {
      if (Player.checkWin(players.get(i))) {
        System.out.println("Player " + String.valueOf(players.get(i).getPlayerIndex()) + " has won!");
        for (int x = 0; x < players.size(); x++) {
          if (x != i) {
            try {
              writer = new BufferedWriter(new FileWriter("player"+ String.valueOf(players.get(i).getPlayerIndex()) + ".txt",true));
              writer.newLine();
              writer.append("Player " + String.valueOf(players.get(i).getPlayerIndex())
              + " has informed player " + String.valueOf(players.get(x).getPlayerIndex())
              + " that player " + String.valueOf(players.get(i).getPlayerIndex())
              + " has won.");
              writer.newLine();
              writer.append("Player " + String.valueOf(players.get(x).getPlayerIndex()) + " exits");
              writer.newLine();
              ArrayList<Integer> playerHand = new ArrayList<Integer>();
              for (int y = 0; y < players.get(x).getHand().size(); y++) {
                playerHand.add(players.get(x).getHand().get(y).getValue());
              }
              writer.append("Player " + String.valueOf(players.get(x).getPlayerIndex())
              + " final hand " + playerHand.toString());
              writer.close();
            } catch (Exception e) {
              System.out.println("An error occurred.");
              e.printStackTrace();
            }
          } else {
            try {
              writer = new BufferedWriter(new FileWriter("player"+ String.valueOf(players.get(i).getPlayerIndex()) + ".txt", true));
              writer.newLine();
              writer.append(
              "Player " + String.valueOf(players.get(x).getPlayerIndex()) + " has won.");
              writer.newLine();
              writer.append("Player " + String.valueOf(players.get(x).getPlayerIndex()) + " exits");
              writer.newLine();
              ArrayList<Integer> playerHand = new ArrayList<Integer>();
              for (int y = 0; y < players.get(x).getHand().size(); y++) {
                playerHand.add(players.get(x).getHand().get(y).getValue());
              }
              writer.append("Player " + String.valueOf(players.get(x).getPlayerIndex())
              + " final hand " + playerHand.toString());
              writer.close();
            } catch (Exception e) {
              System.out.println("An error occurred.");
              e.printStackTrace();
            }
          }
        }
        return;
      } else {
        continue;
      }
    }

    for (int i = 0; i < players.size(); i++) {
      MyThread playerThread = new MyThread(players.get(i));
      playerThread.start();
    }


  }

  /**
  * Defines the object of type Player which represents players participating in
  * the card game which a have unique Index value to identify the player and a
  * Hand which states the current cards in the player's hand. The player's Index
  * is a positive integer and the player's Hand is a list of Card objects.
  *
  * @075397 and (Ken's candidate number) @21/10/2020
  */

  static class Player {

    int playerIndex;
    ArrayList<Card> hand;
    CardDeck drawingDeck;
    CardDeck discardingDeck;

    /**
    * The constructor method for creating a Player object.
    *
    * @param playerIndex, hand, drawingDeck, discardingDeck.
    */

    public Player(int playerIndex, ArrayList<Card> hand, CardDeck drawingDeck, CardDeck discardingDeck) {
      this.playerIndex = playerIndex;
      this.hand = hand;
      this.drawingDeck = drawingDeck;
      this.discardingDeck = discardingDeck;
    }

    /**
    * The setter method to update the value of playerIndex.
    *
    * @param newIndex.
    */

    public void setPlayerIndex(int playerIndex) {
      this.playerIndex = playerIndex;
    }

    /**
    * The setter method to update the value of hand.
    *
    * @param newHand.
    */

    public void setHand(ArrayList<Card> hand) {
      this.hand = hand;
    }

    /**
    * The setter method to update the value of hand.
    *
    * @param newHand.
    */

    public void setDrawingDeck(CardDeck drawingDeck) {
      this.drawingDeck = drawingDeck;
    }

    /**
    * The setter method to update the value of hand.
    *
    * @param newHand.
    */

    public void setDiscardingDeck(CardDeck discardingDeck) {
      this.discardingDeck = discardingDeck;
    }

    /**
    * The getter method to retrieve the value of playerIndex.
    *
    * @return playerIndex.
    */

    public int getPlayerIndex() {
      return playerIndex;
    }

    /**
    * The getter method to retrieve the value of hand.
    *
    * @return hand.
    */

    public ArrayList<Card> getHand() {
      return hand;
    }

    /**
    * The getter method to retrieve the value of hand.
    *
    * @return hand.
    */

    public CardDeck getDrawingDeck() {
      return drawingDeck;
    }

    /**
    * The getter method to retrieve the value of hand.
    *
    * @return hand.
    */

    public CardDeck getDiscardingDeck() {
      return discardingDeck;
    }

    /**
    * Add a card from a deck to player's hand then discard a card from the player's
    * hand to a deck as a single atomic action.
    *
    * @param playerIndex player identifier.
    */
    public synchronized void drawAndDiscard(Player player) {
      int denomination = player.getPlayerIndex();

      Card cardToDraw = player.getDrawingDeck().getCards().get(0);
      player.getHand().add(cardToDraw);

      System.out.println("Player " + player.getPlayerIndex() + " draws a "
      + player.getDrawingDeck().getCards().get(0).getValue() + " from deck "
      + player.getDrawingDeck().getDeckIndex());

      player.getDrawingDeck().getCards().remove(0);

      ArrayList<Card> discardableCards = new ArrayList<Card>();

      for (int i = 0; i < player.getHand().size(); i++) {
        if (player.getHand().get(i).getValue() != denomination) {
          discardableCards.add(player.getHand().get(i));
        } else {
          continue;
        }
      }

      Random rand = new Random();
      Card cardToDiscard = discardableCards.get(rand.nextInt(discardableCards.size()));
      System.out.println("Player " + player.getPlayerIndex() + " discards a " + cardToDiscard.getValue()
      + " to deck " + player.getDiscardingDeck().getDeckIndex());
      player.getDiscardingDeck().getCards().add(cardToDiscard);
      player.getHand().remove(cardToDiscard);

      ArrayList<Integer> playerHand = new ArrayList<Integer>();
      for (int i = 0; i < player.getHand().size(); i++) {
        playerHand.add(player.getHand().get(i).getValue());
      }
      System.out.println("Player " + player.getPlayerIndex() + " current hand is " + playerHand);

      try {
        BufferedWriter writer = new BufferedWriter(new FileWriter("player"+ String.valueOf(player.getPlayerIndex()) + ".txt", true));
        writer.newLine();
        writer.append("Player " + player.getPlayerIndex() + " draws a "
        + cardToDraw.getValue() + " from deck "
        + player.getDrawingDeck().getDeckIndex());
        writer.newLine();
        writer.append("Player " + player.getPlayerIndex() + " discards a " + cardToDiscard.getValue()
        + " to deck " + player.getDiscardingDeck().getDeckIndex());
        writer.newLine();
        writer.append("Player " + player.getPlayerIndex() + " current hand is " + playerHand);
        writer.close();
      } catch (IOException e) {
      }
    }

    /**
    * Check a player's hand for four cards of the same value.
    *
    * @param playerIndex player identifier.
    * @return true if player had won, otherwise false.
    */
    public synchronized static boolean checkWin(Player player) {
      for (int i = 1; i < player.getHand().size(); i++) {
        if (player.getHand().get(0).getValue() != player.getHand().get(i).getValue()) {
          return false;
        } else {
          continue;
        }
      }
      return true;
    }
  }

  /**
  * Defines the object of type Card which represents playing cards of different
  * values which originate from a Pack text file and are distributed to both
  * Players and CardDecks. These values are positive integers and are randomly
  * generated via the Pack text file.
  *
  * @075397 and (Ken's candidate number) @21/10/2020
  */
  static class Card {

    int value;

    /**
    * The constructor method for creating a Card object.
    *
    * @param value.
    */

    public Card(int value) {
      this.value = value;
    }

    /**
    * The setter method to update value.
    *
    * @param value.
    */
    public void setValue(int value) {
      this.value = value;
    }

    /**
    * The getter method to retrieve value.
    *
    * @return value.
    */
    public int getValue() {
      return value;
    }
  }

  /**
  * Defines the object of type CardDeck which represents decks of cards used for
  * drawing and discarding players cards which a have unique Index value to
  * identify the deck and a Cards which states the current cards in the deck. The
  * decks's Index is a positive integer and the deck's Cards is a list of Card
  * objects.
  *
  * @075397 and (Ken's candidate number) @21/10/2020
  */
  static class CardDeck {

    int deckIndex;
    ArrayList<Card> cards;

    /**
    * The constructor method for creating a CardDeck object.
    *
    * @param playerIndex, hand.
    */

    public CardDeck(int deckIndex, ArrayList<Card> cards) {
      this.deckIndex = deckIndex;
      this.cards = cards;
    }

    /**
    * The setter method to update the value of deckIndex.
    *
    * @param deckIndex.
    */
    public void setDeckIndex(int deckIndex) {
      this.deckIndex = deckIndex;
    }

    /**
    * The setter method to update the value of cards.
    *
    * @param cards.
    */
    public void setCards(ArrayList<Card> cards) {
      this.cards = cards;
    }

    /**
    * The getter method to retrieve the value of deckIndex.
    *
    * @return deckIndex.
    */
    public int getDeckIndex() {
      return deckIndex;
    }

    /**
    * The getter method to retrieve the value of cards.
    *
    * @return cards.
    */
    public ArrayList<Card> getCards() {
      return cards;
    }
  }

  static class MyThread extends Thread {

    Player pRef;

    MyThread(Player p) {
      pRef = p;
    }

    @Override
    public void run() {
      try {
        while (gameOver == false) {
          pRef.drawAndDiscard(pRef);
          if (Player.checkWin(pRef) == true) {
            gameOver = true;
            winningPlayer = pRef.getPlayerIndex();
          }
          Thread.sleep(1000);
        }
      } catch (InterruptedException e) {
        System.out.println("InterruptedException occur");
      }
    }
  }
}
