import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.effect.DropShadow;
import javafx.animation.*;
import javafx.util.Duration;
import java.util.*;

public class PokerGame extends Application {
    
    // Game variables
    private List<Card> deck;
    private List<Card> playerHand;
    private List<Card> computerHand;
    private List<Card> communityCards;
    private int playerChips = 1000;
    private int computerChips = 1000;
    private int currentBet = 0;
    private int playerBet = 0;
    private int computerBet = 0;
    private String gamePhase = "betting"; // betting, flop, turn, river, showdown
    private boolean playerTurn = true;
    private boolean gameActive = true;
    
    // UI Components
    private VBox root;
    private HBox computerCardsBox;
    private HBox communityCardsBox;
    private HBox playerCardsBox;
    private Label playerChipsLabel;
    private Label computerChipsLabel;
    private Label potLabel;
    private Label gameStatusLabel;
    private Label currentBetLabel;
    private HBox buttonPanel;
    private Button betButton;
    private Button foldButton;
    private Button checkButton;
    private Button callButton;
    private Button raiseButton;
    private Button newGameButton;
    private TextField betAmountField;
    
    @Override
    public void start(Stage primaryStage) {
        root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #2a5a3a 0%, #1a3a2a 100%);");
        
        // Title
        Label titleLabel = new Label("TEXAS HOLD'EM POKER");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.GOLD);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setEffect(new DropShadow());
        
        // Computer's area
        VBox computerArea = createComputerArea();
        
        // Community cards area
        VBox communityArea = createCommunityArea();
        
        // Player's area
        VBox playerArea = createPlayerArea();
        
        // Game info panel
        HBox infoPanel = createInfoPanel();
        
        // Control panel
        HBox controlPanel = createControlPanel();
        
        root.getChildren().addAll(titleLabel, computerArea, communityArea, playerArea, infoPanel, controlPanel);
        
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("Poker Game - Texas Hold'em");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        startNewGame();
    }
    
    private VBox createComputerArea() {
        VBox vbox = new VBox(5);
        vbox.setAlignment(Pos.CENTER);
        
        computerChipsLabel = new Label("Computer: $" + computerChips);
        computerChipsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        computerChipsLabel.setTextFill(Color.WHITE);
        
        computerCardsBox = new HBox(10);
        computerCardsBox.setAlignment(Pos.CENTER);
        computerCardsBox.setPrefHeight(150);
        
        // Show back of cards for computer
        for (int i = 0; i < 2; i++) {
            computerCardsBox.getChildren().add(createBackOfCard());
        }
        
        vbox.getChildren().addAll(computerChipsLabel, computerCardsBox);
        return vbox;
    }
    
    private VBox createCommunityArea() {
        VBox vbox = new VBox(5);
        vbox.setAlignment(Pos.CENTER);
        
        Label communityLabel = new Label("COMMUNITY CARDS");
        communityLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        communityLabel.setTextFill(Color.WHITE);
        
        communityCardsBox = new HBox(10);
        communityCardsBox.setAlignment(Pos.CENTER);
        communityCardsBox.setPrefHeight(150);
        
        vbox.getChildren().addAll(communityLabel, communityCardsBox);
        return vbox;
    }
    
    private VBox createPlayerArea() {
        VBox vbox = new VBox(5);
        vbox.setAlignment(Pos.CENTER);
        
        playerChipsLabel = new Label("You: $" + playerChips);
        playerChipsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        playerChipsLabel.setTextFill(Color.WHITE);
        
        playerCardsBox = new HBox(10);
        playerCardsBox.setAlignment(Pos.CENTER);
        playerCardsBox.setPrefHeight(150);
        
        vbox.getChildren().addAll(playerChipsLabel, playerCardsBox);
        return vbox;
    }
    
    private HBox createInfoPanel() {
        HBox hbox = new HBox(20);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(10));
        hbox.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 10;");
        
        potLabel = new Label("Pot: $0");
        potLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        potLabel.setTextFill(Color.YELLOW);
        
        currentBetLabel = new Label("Current Bet: $0");
        currentBetLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        currentBetLabel.setTextFill(Color.YELLOW);
        
        gameStatusLabel = new Label("Game Started - Your Turn");
        gameStatusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gameStatusLabel.setTextFill(Color.WHITE);
        
        hbox.getChildren().addAll(potLabel, currentBetLabel, gameStatusLabel);
        return hbox;
    }
    
    private HBox createControlPanel() {
        buttonPanel = new HBox(10);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setPadding(new Insets(10));
        
        betButton = new Button("Bet");
        betButton.setStyle("-fx-font-size: 14px; -fx-min-width: 80px; -fx-min-height: 40px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        betButton.setOnAction(e -> placeBet());
        
        foldButton = new Button("Fold");
        foldButton.setStyle("-fx-font-size: 14px; -fx-min-width: 80px; -fx-min-height: 40px; -fx-background-color: #f44336; -fx-text-fill: white;");
        foldButton.setOnAction(e -> fold());
        
        checkButton = new Button("Check");
        checkButton.setStyle("-fx-font-size: 14px; -fx-min-width: 80px; -fx-min-height: 40px; -fx-background-color: #2196F3; -fx-text-fill: white;");
        checkButton.setOnAction(e -> check());
        
        callButton = new Button("Call");
        callButton.setStyle("-fx-font-size: 14px; -fx-min-width: 80px; -fx-min-height: 40px; -fx-background-color: #FF9800; -fx-text-fill: white;");
        callButton.setOnAction(e -> call());
        
        raiseButton = new Button("Raise");
        raiseButton.setStyle("-fx-font-size: 14px; -fx-min-width: 80px; -fx-min-height: 40px; -fx-background-color: #9C27B0; -fx-text-fill: white;");
        raiseButton.setOnAction(e -> raise());
        
        betAmountField = new TextField();
        betAmountField.setPromptText("Amount");
        betAmountField.setPrefWidth(100);
        
        newGameButton = new Button("New Game");
        newGameButton.setStyle("-fx-font-size: 14px; -fx-min-width: 100px; -fx-min-height: 40px; -fx-background-color: #FF5722; -fx-text-fill: white;");
        newGameButton.setOnAction(e -> startNewGame());
        
        buttonPanel.getChildren().addAll(betButton, foldButton, checkButton, callButton, raiseButton, betAmountField, newGameButton);
        
        // Disable call button initially
        callButton.setDisable(true);
        
        return buttonPanel;
    }
    
    private Rectangle createCardPlaceholder() {
        Rectangle card = new Rectangle(80, 120);
        card.setArcWidth(10);
        card.setArcHeight(10);
        card.setFill(Color.WHITE);
        card.setStroke(Color.BLACK);
        card.setStrokeWidth(2);
        card.setEffect(new DropShadow());
        return card;
    }
    
    private VBox createCardFace(String rank, String suit) {
        VBox card = new VBox();
        card.setPrefSize(80, 120);
        card.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");
        card.setAlignment(Pos.CENTER);
        card.setEffect(new DropShadow());
        
        Color suitColor = suit.equals("♥") || suit.equals("♦") ? Color.RED : Color.BLACK;
        
        Label rankLabel = new Label(rank);
        rankLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        rankLabel.setTextFill(suitColor);
        
        Label suitLabel = new Label(suit);
        suitLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        suitLabel.setTextFill(suitColor);
        
        card.getChildren().addAll(rankLabel, suitLabel);
        return card;
    }
    
    private VBox createBackOfCard() {
        VBox card = new VBox();
        card.setPrefSize(80, 120);
        card.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #2b6a9f 0%, #1a3a5a 100%); -fx-border-color: gold; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10;");
        card.setAlignment(Pos.CENTER);
        card.setEffect(new DropShadow());
        
        Label backLabel = new Label("?");
        backLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        backLabel.setTextFill(Color.GOLD);
        
        card.getChildren().add(backLabel);
        return card;
    }
    
    private void startNewGame() {
        // Initialize deck
        initializeDeck();
        shuffleDeck();
        
        // Deal hands
        playerHand = new ArrayList<>();
        computerHand = new ArrayList<>();
        communityCards = new ArrayList<>();
        
        // Deal 2 cards to each player
        playerHand.add(dealCard());
        computerHand.add(dealCard());
        playerHand.add(dealCard());
        computerHand.add(dealCard());
        
        // Reset game state
        playerChips = 1000;
        computerChips = 1000;
        currentBet = 10; // Small blind
        playerBet = 0;
        computerBet = 0;
        gamePhase = "betting";
        playerTurn = true;
        gameActive = true;
        
        // Update UI
        updateUI();
        updatePlayerCards();
        computerCardsBox.getChildren().clear();
        for (int i = 0; i < 2; i++) {
            computerCardsBox.getChildren().add(createBackOfCard());
        }
        communityCardsBox.getChildren().clear();
        
        gameStatusLabel.setText("Your turn - Bet or Fold");
        updateButtons(true);
    }
    
    private void initializeDeck() {
        deck = new ArrayList<>();
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        String[] suits = {"♥", "♦", "♣", "♠"};
        
        for (String suit : suits) {
            for (String rank : ranks) {
                deck.add(new Card(rank, suit));
            }
        }
    }
    
    private void shuffleDeck() {
        Collections.shuffle(deck);
    }
    
    private Card dealCard() {
        return deck.remove(0);
    }
    
    private void placeBet() {
        if (!playerTurn || !gameActive) return;
        
        try {
            int betAmount = Integer.parseInt(betAmountField.getText());
            if (betAmount > 0 && betAmount <= playerChips) {
                playerBet = betAmount;
                playerChips -= betAmount;
                currentBet = betAmount;
                updateUI();
                gameStatusLabel.setText("You bet $" + betAmount);
                
                // Computer's turn
                playerTurn = false;
                computerAction();
            } else {
                gameStatusLabel.setText("Invalid bet amount!");
            }
        } catch (NumberFormatException e) {
            gameStatusLabel.setText("Please enter a valid number");
        }
    }
    
    private void fold() {
        if (!playerTurn || !gameActive) return;
        
        gameActive = false;
        gameStatusLabel.setText("You folded! Computer wins the pot!");
        computerChips += (playerBet + computerBet);
        updateUI();
        updateButtons(false);
        newGameButton.setDisable(false);
    }
    
    private void check() {
        if (!playerTurn || !gameActive) return;
        
        if (currentBet == 0) {
            gameStatusLabel.setText("You checked");
            playerTurn = false;
            nextPhase();
        } else {
            gameStatusLabel.setText("You can't check - you must call, raise, or fold");
        }
    }
    
    private void call() {
        if (!playerTurn || !gameActive) return;
        
        int callAmount = currentBet - playerBet;
        if (callAmount <= playerChips) {
            playerChips -= callAmount;
            playerBet += callAmount;
            updateUI();
            gameStatusLabel.setText("You called $" + callAmount);
            playerTurn = false;
            nextPhase();
        } else {
            gameStatusLabel.setText("You don't have enough chips to call!");
        }
    }
    
    private void raise() {
        if (!playerTurn || !gameActive) return;
        
        try {
            int raiseAmount = Integer.parseInt(betAmountField.getText());
            int totalBet = currentBet + raiseAmount;
            if (totalBet > currentBet && totalBet <= playerChips + playerBet) {
                int additionalAmount = totalBet - playerBet;
                playerChips -= additionalAmount;
                playerBet = totalBet;
                currentBet = totalBet;
                updateUI();
                gameStatusLabel.setText("You raised to $" + totalBet);
                playerTurn = false;
                computerAction();
            } else {
                gameStatusLabel.setText("Invalid raise amount!");
            }
        } catch (NumberFormatException e) {
            gameStatusLabel.setText("Please enter a valid number");
        }
    }
    
    private void computerAction() {
        if (!gameActive) return;
        
        // Simple AI: Computer calls if it has enough chips
        int callAmount = currentBet - computerBet;
        
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> {
            if (callAmount <= computerChips) {
                computerChips -= callAmount;
                computerBet += callAmount;
                updateUI();
                gameStatusLabel.setText("Computer called $" + callAmount);
                nextPhase();
            } else {
                // Computer folds if can't call
                gameStatusLabel.setText("Computer folded! You win!");
                gameActive = false;
                playerChips += (playerBet + computerBet);
                updateUI();
                updateButtons(false);
            }
        });
        pause.play();
    }
    
    private void nextPhase() {
        switch (gamePhase) {
            case "betting":
                // Deal flop (3 cards)
                for (int i = 0; i < 3; i++) {
                    communityCards.add(dealCard());
                }
                gamePhase = "flop";
                currentBet = 0;
                playerBet = 0;
                computerBet = 0;
                playerTurn = true;
                updateCommunityCards();
                gameStatusLabel.setText("Flop dealt - Your turn");
                updateButtons(true);
                break;
                
            case "flop":
                // Deal turn (1 card)
                communityCards.add(dealCard());
                gamePhase = "turn";
                currentBet = 0;
                playerBet = 0;
                computerBet = 0;
                playerTurn = true;
                updateCommunityCards();
                gameStatusLabel.setText("Turn dealt - Your turn");
                updateButtons(true);
                break;
                
            case "turn":
                // Deal river (1 card)
                communityCards.add(dealCard());
                gamePhase = "river";
                currentBet = 0;
                playerBet = 0;
                computerBet = 0;
                playerTurn = true;
                updateCommunityCards();
                gameStatusLabel.setText("River dealt - Final betting round");
                updateButtons(true);
                break;
                
            case "river":
                // Showdown
                showdown();
                break;
        }
    }
    
    private void showdown() {
        gameActive = false;
        
        // Evaluate hands
        String playerHandRank = evaluateHand(playerHand, communityCards);
        String computerHandRank = evaluateHand(computerHand, communityCards);
        
        int playerValue = getHandValue(playerHandRank);
        int computerValue = getHandValue(computerHandRank);
        
        // Show computer's cards
        computerCardsBox.getChildren().clear();
        for (Card card : computerHand) {
            computerCardsBox.getChildren().add(createCardFace(card.rank, card.suit));
        }
        
        int pot = playerBet + computerBet;
        
        if (playerValue > computerValue) {
            gameStatusLabel.setText("You win! " + playerHandRank + " beats " + computerHandRank + "! +$" + pot);
            playerChips += pot;
        } else if (computerValue > playerValue) {
            gameStatusLabel.setText("Computer wins! " + computerHandRank + " beats " + playerHandRank + "!");
            computerChips += pot;
        } else {
            gameStatusLabel.setText("Tie! " + playerHandRank + " vs " + computerHandRank + " - Split pot!");
            int halfPot = pot / 2;
            playerChips += halfPot;
            computerChips += halfPot;
        }
        
        updateUI();
        updateButtons(false);
        
        // Check if game should end
        if (playerChips <= 0) {
            gameStatusLabel.setText("GAME OVER! You ran out of chips!");
        } else if (computerChips <= 0) {
            gameStatusLabel.setText("YOU WIN! Computer ran out of chips!");
        } else {
            newGameButton.setDisable(false);
        }
    }
    
    private String evaluateHand(List<Card> hand, List<Card> community) {
        // Combine hand and community cards
        List<Card> allCards = new ArrayList<>();
        allCards.addAll(hand);
        allCards.addAll(community);
        
        // Simple hand evaluation (for demo purposes)
        // In a real game, you'd implement full poker hand evaluation
        return "High Card";
    }
    
    private int getHandValue(String handRank) {
        Map<String, Integer> handValues = new HashMap<>();
        handValues.put("Royal Flush", 10);
        handValues.put("Straight Flush", 9);
        handValues.put("Four of a Kind", 8);
        handValues.put("Full House", 7);
        handValues.put("Flush", 6);
        handValues.put("Straight", 5);
        handValues.put("Three of a Kind", 4);
        handValues.put("Two Pair", 3);
        handValues.put("One Pair", 2);
        handValues.put("High Card", 1);
        
        return handValues.getOrDefault(handRank, 1);
    }
    
    private void updateUI() {
        playerChipsLabel.setText("You: $" + playerChips);
        computerChipsLabel.setText("Computer: $" + computerChips);
        potLabel.setText("Pot: $" + (playerBet + computerBet));
        currentBetLabel.setText("Current Bet: $" + currentBet);
    }
    
    private void updatePlayerCards() {
        playerCardsBox.getChildren().clear();
        for (Card card : playerHand) {
            playerCardsBox.getChildren().add(createCardFace(card.rank, card.suit));
        }
    }
    
    private void updateCommunityCards() {
        communityCardsBox.getChildren().clear();
        for (Card card : communityCards) {
            communityCardsBox.getChildren().add(createCardFace(card.rank, card.suit));
        }
    }
    
    private void updateButtons(boolean enable) {
        betButton.setDisable(!enable);
        foldButton.setDisable(!enable);
        checkButton.setDisable(!enable);
        callButton.setDisable(!enable);
        raiseButton.setDisable(!enable);
        
        if (enable && currentBet > 0) {
            callButton.setDisable(false);
            checkButton.setDisable(true);
        } else if (enable) {
            callButton.setDisable(true);
            checkButton.setDisable(false);
        }
    }
    
    // Card class
    private class Card {
        String rank;
        String suit;
        
        Card(String rank, String suit) {
            this.rank = rank;
            this.suit = suit;
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}