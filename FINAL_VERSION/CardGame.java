import java.util.*;
import java.io.*;

/**
* A Card Game implementation which uses the outer class CardGame and the nested
* inner classes Player, Card, CardDeck and PlayerThread. The static class main takes the
* packTxtFileName and playerNo from the user at the console and uses these to
* start a game.
*
* @075397 and (Ken's candidate number) 
* @21/10/2020
*/
public class CardGame {
  
  public static volatile boolean gameOver = false;
  public static volatile int winningPlayer;

  public static void main(String[] args) {
    
    // Instantiate necessary object arrays.
    ArrayList<Player> players = new ArrayList<Player>();
    ArrayList<CardDeck> decks = new ArrayList<CardDeck>();
    ArrayList<Card> pack = new ArrayList<Card>();
    ArrayList<Thread> playerThreads = new ArrayList<Thread>();
    
    String packTxtFileName;
    int playerNo;
    boolean isValid = true;
    final int handSize = 4;
    BufferedWriter writer;

    // Takes input from the console to retrieve playerNo and packTxtFileName and reads the pack text file adding all data to an ArrayList.
    try {
      Scanner scanner = new Scanner(System.in);
      System.out.print("Enter the number of players: ");
      playerNo = scanner.nextInt();
      if(playerNo <= 1) {
          System.out.println("Number of players must be 2 or more.");
          return;
       }
      System.out.print("Enter the name of the pack text file (excluding the file extension): ");
      packTxtFileName = scanner.next();
        
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
      return;
    }
    
    // Check the pack ArrayList only has non-negative integers.
    for (int i = 0; i < pack.size(); i++) {
      if (pack.get(i).getValue() < 0) {
        isValid = false;
        break;
      } else {
        continue;
      }
    }

    // Check the pack ArrayList is of the appropriate size.
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
    boolean shouldShuffle = false;
    System.out.print("Would you like to shuffle the pack? (y/n): ");
    Scanner scanner = new Scanner(System.in);
    String shuffle = scanner.next().toLowerCase();
    
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

    // Create the output files for each player detailing their actions and output their initial hand.
    for (int i = 0; i < players.size(); i++) {
      try {
        File file = new File("player"+ String.valueOf(players.get(i).getPlayerIndex()) + "_output.txt");
        file.createNewFile();
        ArrayList<Integer> initialHand = new ArrayList<Integer>();
        for (int x = 0; x < players.get(i).getHand().size(); x++) {
          initialHand.add(players.get(i).getHand().get(x).getValue());
        }
        writer = new BufferedWriter(new FileWriter("player"+ String.valueOf(players.get(i).getPlayerIndex()) + "_output.txt"));
        writer.write("Player " + String.valueOf(players.get(i).getPlayerIndex()) + " initial hand " + initialHand.toString());
        writer.close();
      } catch (Exception e) {
        System.out.println("An error occurred.");
        e.printStackTrace();
      }
    }
    
    // Create the output files for each deck.
    for (int i = 0; i < decks.size(); i++) {
      try {
        File file = new File("deck"+ String.valueOf(decks.get(i).getDeckIndex()) + "_output.txt");
        file.createNewFile();
      } catch (Exception e) {
        System.out.println("An error occurred.");
        e.printStackTrace();
      }
    }

    // Check for win conditions amongst the player's initial hand.
    for (int i = 0; i < players.size(); i++) {
      if (Player.checkWin(players.get(i))) {
        winningPlayer = players.get(i).getPlayerIndex();
        gameOver = true;
      } else {
        continue;
      }
    }
    
    // If there is no initial winner, start the threads and find a winner.
    if(gameOver == false){
      for (int i = 0; i < players.size(); i++) {
        PlayerThread playerThread = new PlayerThread(players.get(i));
        playerThreads.add(playerThread);
        playerThread.start();
      }
    }
    
    // Move the Main thread to the waiting state so as to find a winner before proceeding.
    for (int i = 0; i < playerThreads.size(); i++) {
        try{
            playerThreads.get(i).join();
        }catch (InterruptedException e) {
            System.out.println("An interruption error occurred.");
            e.printStackTrace();
        }
    }
    
    // Print the winner to the terminal.
    System.out.println("Player " + String.valueOf(winningPlayer) + " wins");
    
    //Output all players final actions.
    for (int i = 0; i < players.size(); i++) {
      if (players.get(i).getPlayerIndex() != winningPlayer) { // Make the output specific to the players who did not win.
         try {
            writer = new BufferedWriter(new FileWriter("player"+ String.valueOf(players.get(i).getPlayerIndex()) + "_output.txt",true));
            writer.newLine();
            writer.append("Player " + String.valueOf(winningPlayer) + " has informed player " + String.valueOf(players.get(i).getPlayerIndex()) + " that player " + String.valueOf(winningPlayer) + " has won.");
            writer.newLine();
            writer.append("Player " + String.valueOf(players.get(i).getPlayerIndex()) + " exits");
            writer.newLine();
            ArrayList<Integer> playerHand = new ArrayList<Integer>();
            for (int x = 0; x < players.get(i).getHand().size(); x++) {
              playerHand.add(players.get(i).getHand().get(x).getValue());
            }
            writer.append("Player " + String.valueOf(players.get(i).getPlayerIndex()) + " final hand " + playerHand.toString());
            writer.close();
         } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
         }
      } else { // Make the output specific to the player who won.
          try {
            writer = new BufferedWriter(new FileWriter("player"+ String.valueOf(players.get(i).getPlayerIndex()) + "_output.txt", true));
            writer.newLine();
            writer.append("Player " + String.valueOf(players.get(i).getPlayerIndex()) + " has won.");
            writer.newLine();
            writer.append("Player " + String.valueOf(players.get(i).getPlayerIndex()) + " exits");
            writer.newLine();
            ArrayList<Integer> playerHand = new ArrayList<Integer>();
            for (int x = 0; x < players.get(i).getHand().size(); x++) {
              playerHand.add(players.get(i).getHand().get(x).getValue());
            }
            writer.append("Player " + String.valueOf(players.get(i).getPlayerIndex()) + " final hand " + playerHand.toString());
            writer.close();
          } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }
      }
    }
    
    //Output the final contents of each deck.
    for(int i = 0; i < decks.size(); i++) {
        try{
            writer = new BufferedWriter(new FileWriter("deck"+ String.valueOf(decks.get(i).getDeckIndex()) + "_output.txt"));
            ArrayList<Integer> deckCards = new ArrayList<Integer>();
            for (int x = 0; x < decks.get(i).getCards().size(); x++) {
              deckCards.add(decks.get(i).getCards().get(x).getValue());
            }
            writer.write("Deck " + String.valueOf(decks.get(i).getDeckIndex()) + " final contents: " + deckCards.toString());
            writer.close();
        }catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
  }

  /**
  * Defines the object of type Player which represents players participating in
  * the card game which a have unique Index value to identify the player, a Hand 
  * which states the current cards in the player's hand, a Drawing Deck which
  * states the deck a player should draw from and a Discarding Deck which states 
  * the deck a player should discard to. The player's Index is a positive integer, 
  * the player's Hand is a list of Card objects and both decks are an object consisting
  * of an index and list of Card objects.
  *
  * @075397 and (Ken's candidate number) 
  * @21/10/2020
  */

  private static class Player {

    int playerIndex;
    ArrayList<Card> hand;
    CardDeck drawingDeck;
    CardDeck discardingDeck;

    /**
    * The constructor method for creating a Player object.
    *
    * @param playerIndex, hand, drawingDeck, discardingDeck.
    */

    private Player(int playerIndex, ArrayList<Card> hand, CardDeck drawingDeck, CardDeck discardingDeck) {
      this.playerIndex = playerIndex;
      this.hand = hand;
      this.drawingDeck = drawingDeck;
      this.discardingDeck = discardingDeck;
    }

    /**
    * The setter method to update the value of playerIndex.
    *
    * @param playerIndex.
    */

    private void setPlayerIndex(int playerIndex) {
      this.playerIndex = playerIndex;
    }

    /**
    * The setter method to update the value of hand.
    *
    * @param hand.
    */

    private void setHand(ArrayList<Card> hand) {
      this.hand = hand;
    }

    /**
    * The setter method to update the value of drawingDeck.
    *
    * @param drawingDeck.
    */

    private void setDrawingDeck(CardDeck drawingDeck) {
      this.drawingDeck = drawingDeck;
    }

    /**
    * The setter method to update the value of discardingDeck.
    *
    * @param discardingDeck.
    */

    private void setDiscardingDeck(CardDeck discardingDeck) {
      this.discardingDeck = discardingDeck;
    }

    /**
    * The getter method to retrieve the value of playerIndex.
    *
    * @return playerIndex.
    */

    private int getPlayerIndex() {
      return playerIndex;
    }

    /**
    * The getter method to retrieve the value of hand.
    *
    * @return hand.
    */

    private ArrayList<Card> getHand() {
      return hand;
    }

    /**
    * The getter method to retrieve the value of drawingDeck.
    *
    * @return drawingDeck.
    */

    private CardDeck getDrawingDeck() {
      return drawingDeck;
    }

    /**
    * The getter method to retrieve the value of discardingDeck.
    *
    * @return discardingDeck.
    */

    private CardDeck getDiscardingDeck() {
      return discardingDeck;
    }

    /**
    * Add a card from the player's drawing deck to player's hand then discard 
    * a non-denomination card, at random, from the player's hand to the player's 
    * discarding deck and then ouput the players actions to a txt file.
    *
    * @param player.
    */
   
    private static synchronized void drawAndDiscard(Player player) {
      int denomination = player.getPlayerIndex();
      
      //Add a card from the player's drawing deck to their hand.
      Card cardToDraw = player.getDrawingDeck().getCards().get(0);
      player.getHand().add(cardToDraw);
      player.getDrawingDeck().getCards().remove(0);
      
      //Create a list of all cards in the player's hand which could be discarded (non-denomination).
      ArrayList<Card> discardableCards = new ArrayList<Card>();
      for (int i = 0; i < player.getHand().size(); i++) {
        if (player.getHand().get(i).getValue() != denomination) {
          discardableCards.add(player.getHand().get(i));
        } else {
          continue;
        }
      }
      
      //Discard at random one of the player's non-denomination cards.
      Random rand = new Random();
      Card cardToDiscard = discardableCards.get(rand.nextInt(discardableCards.size()));
      player.getDiscardingDeck().getCards().add(cardToDiscard);
      player.getHand().remove(cardToDiscard);
      
      //Create a list of the player's hand.
      ArrayList<Integer> playerHand = new ArrayList<Integer>();
      for (int i = 0; i < player.getHand().size(); i++) {
        playerHand.add(player.getHand().get(i).getValue());
      }
      
      //Output the actions caused by this player's draw and discard.
      try {
        BufferedWriter writer = new BufferedWriter(new FileWriter("player"+ String.valueOf(player.getPlayerIndex()) + "_output.txt", true));
        writer.newLine();
        writer.append("Player " + player.getPlayerIndex() + " draws a " + cardToDraw.getValue() + " from deck "+ player.getDrawingDeck().getDeckIndex());
        writer.newLine();
        writer.append("Player " + player.getPlayerIndex() + " discards a " + cardToDiscard.getValue() + " to deck " + player.getDiscardingDeck().getDeckIndex());
        writer.newLine();
        writer.append("Player " + player.getPlayerIndex() + " current hand is " + playerHand);
        writer.close();
      } catch (IOException e) {
        System.out.println("An error occurred.");
        e.printStackTrace();
      }
    }

    /**
    * Check a player's hand for four cards of the same value.
    *
    * @param player.
    * @return true if player had won, otherwise false.
    */
   
    private static synchronized boolean checkWin(Player player) {
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
  * values which originate from a Pack text file and are distributed to both Players 
  * and CardDecks. These values are positive integers and are provided by the Pack 
  * text file.
  *
  * @075397 and (Ken's candidate number) 
  * @21/10/2020
  */
  private static class Card {

    int value;

    /**
    * The constructor method for creating a Card object.
    *
    * @param value.
    */

    private Card(int value) {
      this.value = value;
    }

    /**
    * The setter method to update value.
    *
    * @param value.
    */
    private void setValue(int value) {
      this.value = value;
    }

    /**
    * The getter method to retrieve value.
    *
    * @return value.
    */
    private int getValue() {
      return value;
    }
  }

  /**
  * Defines the object of type CardDeck which represents decks of cards used for 
  * drawing and discarding players cards which a have unique Index value to identify 
  * the deck and Cards which states the current cards in the deck. The decks's Index 
  * is a positive integer and the deck's Cards is a list of Card objects.
  *
  * @075397 and (Ken's candidate number) 
  * @21/10/2020
  */
  private static class CardDeck {

    int deckIndex;
    ArrayList<Card> cards;

    /**
    * The constructor method for creating a CardDeck object.
    *
    * @param deckIndex, cards.
    */

    private CardDeck(int deckIndex, ArrayList<Card> cards) {
      this.deckIndex = deckIndex;
      this.cards = cards;
    }

    /**
    * The setter method to update the value of deckIndex.
    *
    * @param deckIndex.
    */
    private void setDeckIndex(int deckIndex) {
      this.deckIndex = deckIndex;
    }

    /**
    * The setter method to update the value of cards.
    *
    * @param cards.
    */
    private void setCards(ArrayList<Card> cards) {
      this.cards = cards;
    }

    /**
    * The getter method to retrieve the value of deckIndex.
    *
    * @return deckIndex.
    */
    private int getDeckIndex() {
      return deckIndex;
    }

    /**
    * The getter method to retrieve the value of cards.
    *
    * @return cards.
    */
    private ArrayList<Card> getCards() {
      return cards;
    }
  }
  
  /**
   * Defines the object of type PlayerThread which represents a player thread object. The thread consists of a run()
   * method which executes the moment the thread is started and terminates upon completion.
   *
   * @075397 and (Ken's candidate number) 
   * @06/11/2020
   */
  private static class PlayerThread extends Thread{

    Player playerRef;
     
    /**
    * The constructor method for creating a PlayerThread object.
    *
    * @param player.
    */
   
    private PlayerThread(Player player) {
      playerRef = player;
    }

    @Override
    public void run() {   
      try {
        while (gameOver == false) {
          playerRef.drawAndDiscard(playerRef);
          if (Player.checkWin(playerRef) == true) {
            winningPlayer = playerRef.getPlayerIndex();
            gameOver = true;
          }
          Thread.sleep(500);
        }
      } catch (InterruptedException e) {
        System.out.println("An interruption error occurred.");
        e.printStackTrace();
      }
    }
  }
}