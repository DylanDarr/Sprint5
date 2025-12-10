package main.sprint5;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import java.io.File;
import java.io.IOException;

public class GUI extends Application {

    private BuildGUI buildGUI;
    private GameController controller;

    public void drawBoard() {
        Board board = controller.getBoard();
        Box[][] boxes = buildGUI.getBoxes();

        if (boxes == null) return;

        for (int row = 0; row < board.getBoardSize(); row++){
            for (int column = 0; column < board.getBoardSize(); column++) {
                boxes[row][column].getChildren().clear();
                if (board.getCell(row, column) == Board.Cell.S)
                    boxes[row][column].drawS(row, column);
                else if (board.getCell(row, column) == Board.Cell.O)
                    boxes[row][column].drawO();
            }
        }
    }

    public void gameStart() {
        buildGUI.gameStart();
    }

    @Override
    public void start(Stage primaryStage){
        this.controller = new GameController();

        this.buildGUI = new BuildGUI(controller);

        this.controller.setGui(this);

        primaryStage.setTitle("SOS");
        primaryStage.setScene(buildGUI.getPrimaryScene());
        primaryStage.show();
    }

    public void enableReplayButton() {
        buildGUI.replayButton.setVisible(true);
    }


    public class Box extends Pane {
        private final int row, column;
        private final GameController controller;

        public Box(int row, int column, GameController controller) {
            this.row = row;
            this.column = column;
            this.controller = controller;

            setStyle("-fx-border-color: white");
            this.setPrefSize(500, 500);

            this.setOnMouseClicked(e -> {
                try {
                    controller.handleCellClick(row, column);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }

        private StackPane imageSelect(String playerSelection, String color) {
            File file = new File("./src/assets/" + playerSelection.toLowerCase() + color + ".png");
            Image imageURL = new Image(file.toURI().toString());
            ImageView image = new ImageView(imageURL);

            if (playerSelection.equals("S")){
                image.fitWidthProperty().bind(this.widthProperty().subtract(15));
                image.fitHeightProperty().bind(this.heightProperty().subtract(15));
            }
            else{
                image.fitWidthProperty().bind(this.widthProperty().subtract(17));
                image.fitHeightProperty().bind(this.heightProperty().subtract(17));
            }
            return (new StackPane(image));
        }

        public void drawS(int row, int column) {
            String color = controller.getBoard().getColor(row, column).toString();
            StackPane sBox = imageSelect("S", color);

            sBox.prefWidthProperty().bind(this.widthProperty());
            sBox.prefHeightProperty().bind(this.heightProperty());
            sBox.setAlignment(Pos.CENTER);

            getChildren().add(sBox);
        }

        public void drawO() {
            String color = controller.getBoard().getColor(row, column).toString();
            StackPane oBox = imageSelect("O", color);

            oBox.prefWidthProperty().bind(this.widthProperty());
            oBox.prefHeightProperty().bind(this.heightProperty());
            oBox.setAlignment(Pos.CENTER);

            getChildren().add(oBox);
        }
    }


    public class BuildGUI extends Stage {

        private final Scene primaryScene;
        private final BorderPane borderPane;
        private final GridPane bodyGrid;

        private final Label gameStatus = new Label("Setup New Game");
        private final Text player1SOSCount = new Text();
        private final Text player2SOSCount = new Text();
        private final Text messageText = new Text();
        private final RadioButton simpleGameButton = new RadioButton();
        private final RadioButton generalGameButton = new RadioButton();
        private final TextField boardSizeField = new TextField("3");
        private final Button replayButton = new Button("Replay Game");

        private Box[][] boxes;
        private final GameController controller;

        public Scene getPrimaryScene() {
            return primaryScene;
        }

        public Box[][] getBoxes() {
            return boxes;
        }

        public BuildGUI(GameController controller) {
            this.controller = controller;

            bodyGrid = new GridPane();
            bodyGrid.setHgap(20);
            bodyGrid.add(new Text("SOS"), 0,0);

            HBox gameSelect = createGameSelect();
            bodyGrid.add(gameSelect, 1,0);

            HBox boardSize = createBoardSizeSelect();
            bodyGrid.add(boardSize, 2,0);

            HBox errorTextHBox = new HBox();
            errorTextHBox.setAlignment(Pos.CENTER);
            errorTextHBox.getChildren().add(messageText);
            bodyGrid.add(errorTextHBox, 1, 2);

            HBox startGameButton = createStartButton();
            bodyGrid.add(startGameButton, 1, 3);

            replayButton.setVisible(false);
            replayButton.setOnAction(e -> controller.startReplay());

            HBox replayBox = new HBox(replayButton);
            replayBox.setAlignment(Pos.CENTER);
            bodyGrid.add(replayBox, 1, 4);

            borderPane = new BorderPane();
            borderPane.setCenter(bodyGrid);

            primaryScene = new Scene(borderPane, 600, 600);

            controller.setUiComponents(gameStatus, player1SOSCount, player2SOSCount, messageText,
                    simpleGameButton, generalGameButton, boardSizeField);
        }

        private boolean radioSelection(RadioButton currentBtn, RadioButton secondaryBtn, String selection){
            if (currentBtn.isSelected()) {
                if (secondaryBtn.isSelected())
                    secondaryBtn.setSelected(false);
                System.out.println(selection + " is selected");
                return true;
            }
            return false;
        }

        private VBox createPlayingSelect(String player) {
            RadioButton humanButton = new RadioButton("Human");
            RadioButton botButton = new RadioButton("Bot");

            if (controller.isPlayerBot(player)) {
                botButton.setSelected(true);
                controller.setPlayerBotStatus(player, true);

            }
            else{
                humanButton.setSelected(true);
                controller.setPlayerBotStatus(player, false);
            }

            humanButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    radioSelection( humanButton, botButton, "HUMAN");
                    controller.setAndTriggerPlayerBot(player, false);
                }
            });

            botButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    radioSelection( botButton, humanButton, "BOT");
                    controller.setAndTriggerPlayerBot(player, true);
                }
            });

            VBox buttonBoxVBox = new VBox(10);
            buttonBoxVBox.setMinHeight(50);
            buttonBoxVBox.setAlignment(Pos.CENTER);
            buttonBoxVBox.getChildren().addAll(humanButton, botButton);

            return buttonBoxVBox;
        }

        private VBox createPlayerSOSelect(String player){
            RadioButton sSelect = new RadioButton();
            RadioButton oSelect = new RadioButton();

            sSelect.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    if ( radioSelection(sSelect, oSelect, "S") ){
                        controller.setPlayerSelection("S", player);
                    }
                    else{
                        controller.setPlayerSelection("", player);
                    }
                }
            });

            oSelect.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    if ( radioSelection( oSelect, sSelect, "O") ){
                        controller.setPlayerSelection("O", player);
                    }
                    else {
                        controller.setPlayerSelection("", player);
                    }
                }
            });

            HBox sSelectHBox = new HBox(new Text("S  "), sSelect);
            sSelectHBox.setMinWidth(100);
            sSelectHBox.setAlignment(Pos.CENTER);
            HBox oSelectHBox = new HBox(new Text("O  "), oSelect);
            oSelectHBox.setMinWidth(100);
            oSelectHBox.setAlignment(Pos.CENTER);

            Text text;
            if (player.equals("PLAYER1")) {
                text = new Text("Player 1: \n  (RED) ");
            }
            else {
                text = new Text("Player 2: \n  (BLUE) ");
            }

            VBox sOSelect = new VBox();
            sOSelect.setAlignment(Pos.CENTER);
            sOSelect.setSpacing(20);
            sOSelect.getChildren().addAll(text, createPlayingSelect(player), sSelectHBox, oSelectHBox);

            return (sOSelect);
        }

        private VBox createPlayerMenu(String player) {
            VBox  playerMenu = new VBox();
            if (player.equals("PLAYER1")){
                playerMenu.getChildren().addAll(createPlayerSOSelect(player), player1SOSCount);
            } else {
                playerMenu.getChildren().addAll(createPlayerSOSelect(player), player2SOSCount);
            }

            playerMenu.setAlignment(Pos.CENTER);
            playerMenu.setSpacing(20);

            return playerMenu;
        }

        private HBox createGameSelect (){
            simpleGameButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    controller.handleGameModeClick("SIMPLE");
                }
            });

            generalGameButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    controller.handleGameModeClick("GENERAL");
                }
            });

            HBox buttonBoxHBox = new HBox();
            buttonBoxHBox.setMinHeight(50);
            buttonBoxHBox.setAlignment(Pos.CENTER);
            buttonBoxHBox.setSpacing(10);
            buttonBoxHBox.getChildren().addAll(new Text("Simple Game"), simpleGameButton,
                    new Text("General Game"), generalGameButton);

            return buttonBoxHBox;
        }

        private HBox createBoardSizeSelect(){
            HBox boardSizeHBox = new HBox();

            boardSizeHBox.setMinWidth(5);
            boardSizeHBox.setMaxWidth(100);
            boardSizeHBox.setSpacing(10);
            boardSizeHBox.setPadding(new Insets(0, 5, 0, 0));
            boardSizeHBox.setAlignment(Pos.CENTER);

            boardSizeHBox.getChildren().addAll(new Text("Board Size"), boardSizeField);

            boardSizeField.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    controller.handleBoardSizeInput(boardSizeField.getText());
                }
            });

            return boardSizeHBox;
        }

        private GridPane gameBoard() {
            GridPane gameBoardGrid = new GridPane();
            int size = controller.getBoard().getBoardSize();
            boxes = new Box[size][size];

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    boxes[i][j] = new Box(i, j, controller);
                    gameBoardGrid.add(boxes[i][j], j, i);
                }
            }
            drawBoard();
            return gameBoardGrid;
        }

        private HBox createStartButton() {
            Button startGameButton = new Button("Start Game");
            HBox startGameHBox = new HBox();
            startGameHBox.setMinHeight(50);
            startGameHBox.setAlignment(Pos.CENTER);
            startGameHBox.getChildren().add(startGameButton);

            startGameButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent){
                    controller.startGame();
                }
            });

            return startGameHBox;
        }

        public void gameStart() {
            replayButton.setVisible(false);
            bodyGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) == 1);

            VBox player1Menu = createPlayerMenu("PLAYER1");
            bodyGrid.add(player1Menu, 0, 1);

            GridPane gameBoardGrid = this.gameBoard();
            bodyGrid.add(gameBoardGrid, 1, 1);

            VBox player2Menu = createPlayerMenu("PLAYER2");
            bodyGrid.add(player2Menu, 2, 1);

            borderPane.setBottom(gameStatus);
            BorderPane.setAlignment(gameStatus, Pos.CENTER);

            controller.handleBoardSizeInput(boardSizeField.getText());
            controller.handleGameModeClick("");
        }
    }

    public static void main(String[] args){
        launch(args);
    }
}
