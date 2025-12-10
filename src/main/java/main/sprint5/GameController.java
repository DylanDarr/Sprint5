package main.sprint5;

import javafx.animation.PauseTransition;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class GameController {

    private Board board;
    private GUI gui;
    private GameRecorder recorder;

    private Label gameStatus;
    private Text player1SOSCount;
    private Text player2SOSCount;
    private Text messageText;
    private RadioButton simpleGameButton;
    private RadioButton generalGameButton;
    private TextField boardSizeField;
    private boolean isPlayer1Bot = false;
    private boolean isPlayer2Bot = false;
    private boolean isReplaying = false;

    public GameController() {
        this.board = new Board(3);
        this.recorder = new GameRecorder();
    }

    public void setGui(GUI gui) {
        this.gui = gui;
    }

    public void setUiComponents(Label gameStatus, Text p1Count, Text p2Count, Text messageText,
                                RadioButton simpleBtn, RadioButton generalBtn, TextField boardSizeField) {
        this.gameStatus = gameStatus;
        this.player1SOSCount = p1Count;
        this.player2SOSCount = p2Count;
        this.messageText = messageText;
        this.simpleGameButton = simpleBtn;
        this.generalGameButton = generalBtn;
        this.boardSizeField = boardSizeField;
    }

    public Board getBoard() {
        return board;
    }

    public boolean isPlayerBot(String player) {
        if (player.equals("PLAYER1")) {
            return isPlayer1Bot;
        }
        else {
            return isPlayer2Bot;
        }
    }

    public void handleCellClick(int row, int column) throws IOException {
        if (board.getGameOver() || isReplaying) {
            return;
        }

        if (isCurrentPlayerBot()) {
            return;
        }

        String currentPlayer = board.getTurn();
        String currentSelection = (currentPlayer.equals("PLAYER1")) ? board.getPlayer1Selection() : board.getPlayer2Selection();

        String moveResult = board.makeMove(row, column);

        if (moveResult.equals("FAIL")) {
            setMessage("Select S or O");
        }
        else if (moveResult.equals("INVALID")) {
            setMessage("Select a valid box");
        }
        else {
            recorder.recordMove(currentPlayer, row, column, currentSelection);

            clearErrorMessage();
            String turnResult = board.selectTurn();

            if (!turnResult.equals("NOSOS")) {
                setMessage(turnResult);
                updateScores();
            }

            if (board.getGameOver()) {
                gui.drawBoard();
                updateGameStatus();
                gui.enableReplayButton();
                return;
            }
        }

        updateScores();
        gui.drawBoard();
        updateGameStatus();
        checkAndTriggerBotMove();
    }

    public void startGame() {
        if (board.getGameOver()) {
            String validGame = board.validGame();

            if (validGame.equals("PASS")) {
                int size = board.getBoardSize();
                String gameMode = board.getGamemodeSelected();

                isReplaying = false;
                recorder.startNewGame(size, gameMode);

                board = new Board(size);
                board.setBoardSize(size);
                board.setGamemodeSelection(gameMode);
                board.setGameOver(false);

                gui.gameStart();
                updateGameStatus();
                player1SOSCount.setText("0");
                player2SOSCount.setText("0");
                clearErrorMessage();
                checkAndTriggerBotMove();
            }
            else if (validGame.equals("BOARDSIZEERROR")) {
                setMessage("Enter a valid board size (3 <= x <= 20)");
            }
            else if (validGame.equals("GAMEMODEERROR")) {
                setMessage("Select Simple or General Game");
            }
        }
    }

    public void setPlayerSelection(String selection, String player) {
        board.setPlayerSelection(selection, player);
    }

    public void handleGameModeClick(String modeClicked) {
        if (modeClicked.equals("SIMPLE")) {
            board.setGamemodeSelection("SIMPLE");
            generalGameButton.setSelected(false);
        }
        else if (modeClicked.equals("GENERAL")) {
            board.setGamemodeSelection("GENERAL");
            simpleGameButton.setSelected(false);
        }
    }

    public void handleBoardSizeInput(String text) {
        if (!text.isEmpty()) {
            try {
                int size = Integer.parseInt(text);
                board.setBoardSize(size);
                if (board.validBoardSize()) {
                    clearErrorMessage();
                }
                else {
                    setMessage("Enter a number greater than 2 and less than 21");
                }
            } catch (NumberFormatException exception) {
                board.setBoardSize(0);
                setMessage("Enter a whole number");
            }
        }
        else {
            board.setBoardSize(0);
        }

    }

    private void setMessage(String str) {
        messageText.setText(str);
    }

    private void clearErrorMessage() {
        messageText.setText("");
    }

    private void updateScores() {
        player1SOSCount.setText(String.valueOf(board.getPlayer1SOSCount()));
        player2SOSCount.setText(String.valueOf(board.getPlayer2SOSCount()));
    }

    private void updateGameStatus() {
        if (board.getGameOver()) {
            gameStatus.setText("Game Over!");
        }
        else if (board.getTurn().equals("PLAYER1")) {
            gameStatus.setText("Player 1's Turn");
        }
        else {
            gameStatus.setText("Player 2's Turn");
        }
    }

    public void checkAndTriggerBotMove() {
        if (board.getGameOver() || isReplaying) return;

        if (isCurrentPlayerBot()) {
            PauseTransition delay = new PauseTransition(Duration.millis(500));
            delay.setOnFinished(event -> {
                try {
                    makeBotMove();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            delay.play();
        }
    }

    public void makeBotMove() throws IOException {
        if (isReplaying) {
            return;
        }

        String moveResult = board.botMove();

        if (moveResult.equals("FAIL") || moveResult.equals("INVALID") || moveResult.equals("NO MOVE AVAILABLE")) {
            System.err.println("Bot move failed.");
            return;
        }

        String[] parts = moveResult.split(",");
        if(parts.length == 3) {
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);
            String selection = parts[2];
            recorder.recordMove(board.getTurn(), row, col, selection);
        }

        clearErrorMessage();
        String turnResult = board.selectTurn();
        if (!turnResult.equals("NOSOS")) {
            setMessage(turnResult);
            updateScores();
        }

        if (board.getGameOver()) {
            gui.drawBoard();
            updateGameStatus();
            gui.enableReplayButton();
            return;
        }

        updateScores();
        gui.drawBoard();
        updateGameStatus();
        checkAndTriggerBotMove();
    }

    private boolean isCurrentPlayerBot() {
        String turn = board.getTurn();
        return (turn.equals("PLAYER1") && isPlayer1Bot) || (turn.equals("PLAYER2") && isPlayer2Bot);
    }

    public void setPlayerBotStatus(String player, boolean isBot) {
        if (player.equals("PLAYER1")) {
            isPlayer1Bot = isBot;
        }
        else {
            isPlayer2Bot = isBot;
        }
    }

    public void setAndTriggerPlayerBot(String player, boolean isBot) {
        if (player.equals("PLAYER1")) {
            isPlayer1Bot = isBot;
            checkAndTriggerBotMove();
        }
        else {
            isPlayer2Bot = isBot;
            checkAndTriggerBotMove();
        }
    }

    public void startReplay(){
        List<String> moves = recorder.loadMoves();

        if (moves.isEmpty()){
            return;
        }

        isReplaying = true;

        String header = moves.getFirst();
        String[] headerParts = header.split(",");
        int size = Integer.parseInt(headerParts[0]);
        String mode = headerParts[1];

        board = new Board(size);
        board.setBoardSize(size);
        board.setGamemodeSelection(mode);
        board.setGameOver(false);
        player1SOSCount.setText("0");
        player2SOSCount.setText("0");
        gui.drawBoard();

        playNextReplayMove(moves, 1);
    }

    private void playNextReplayMove(List<String> moves, int index) {
        if (index >= moves.size()) {
            isReplaying = false;
            gameStatus.setText("Replay Finished");
            return;
        }

        String line = moves.get(index);
        String[] parts = line.split(",");

        String player = parts[0];
        int row = Integer.parseInt(parts[1]);
        int col = Integer.parseInt(parts[2]);
        String selection = parts[3];

        board.setPlayerSelection(selection, player);
        board.makeMove(row,col);
        board.selectTurn();
        updateScores();
        gui.drawBoard();
        updateGameStatus();

        PauseTransition delay = new PauseTransition(Duration.millis(500));
        delay.setOnFinished(e -> playNextReplayMove(moves, index + 1));
        delay.play();
    }
}
