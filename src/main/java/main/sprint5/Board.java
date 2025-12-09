package main.sprint5;

import java.util.ArrayList;
import java.util.Random;

public class Board {
    public enum Cell {EMPTY, S, O}
    private Cell[][] gameGrid;
    public enum Color {RED, BLUE, PURPLE, BLACK}
    private Color[][] colorGrid;
    private int BOARDSIZE;
    private String turn;
    private int unsetSOS = 0;
    private boolean gameOver = true;
    private String gamemodeSelected = "";
    private String player1Selection = "";
    private String player2Selection = "";
    private int player1SOSCount;
    private int player2SOSCount;

    public Board(int bSize) {
        BOARDSIZE = bSize;
        gameGrid = new Cell[BOARDSIZE][BOARDSIZE];
        colorGrid = new Color[BOARDSIZE][BOARDSIZE];
        createBoard();
    }

    public void createBoard() {
        for (int row = 0; row < BOARDSIZE; row++){
            for (int column = 0; column < BOARDSIZE; column++){
                gameGrid[row][column] = Cell.EMPTY;
                colorGrid[row][column] = Color.BLACK;
            }
        }
        turn = "PLAYER1";
    }

    public Cell getCell(int row, int column) {
        if (row >= 0 && row < BOARDSIZE && column >= 0 && column < BOARDSIZE)
            return gameGrid[row][column];
        else
            return null;
    }

    public Color getColor(int row, int column) {
        if (row >= 0 && row < BOARDSIZE && column >= 0 && column < BOARDSIZE)
            return colorGrid[row][column];
        else
            return null;
    }

    public void setBoardSize(int number){
        BOARDSIZE = number;
    }

    public int getBoardSize() {return BOARDSIZE;}

    public String getTurn() {
        return turn;
    }

    public void setGameOver(boolean over) {gameOver = over;}

    public boolean getGameOver() {return gameOver;}

    public int getPlayer1SOSCount() {return player1SOSCount;}

    public int getPlayer2SOSCount() {return player2SOSCount;}

    public void setGamemodeSelection(String gamemode) {gamemodeSelected = gamemode;}

    public String getGamemodeSelected() {return gamemodeSelected;}

    public String getPlayer1Selection() { return player1Selection; }

    public String getPlayer2Selection() { return player2Selection; }

    public boolean validGameMode() {
        return gamemodeSelected.equals("SIMPLE") || gamemodeSelected.equals("GENERAL");
    }

    public boolean validBoardSize() {
        return BOARDSIZE >= 3 && BOARDSIZE <= 20 ;
    }

    public void setPlayerSelection(String currentSelected, String player) {
        if (player.equals("PLAYER1")) {
            player1Selection = currentSelected;
        }
        else {
            player2Selection = currentSelected;
        }
    }

    public String validGame(){
        if (!validGameMode()) {return "GAMEMODEERROR" ;}
        if (!validBoardSize()) { return "BOARDSIZEERROR" ;}
        return ("PASS");
    }

    public int sosCheck(int sRow, int sColumn, int oRow, int oColumn) {
        int RowIndex = oRow - (sRow - oRow);
        int ColumnIndex = oColumn - (sColumn - oColumn);
        if (RowIndex < 0 || ColumnIndex < 0)
            return 0;
        if (RowIndex > BOARDSIZE - 1 || ColumnIndex > BOARDSIZE - 1)
            return 0;
        if (gameGrid[RowIndex][ColumnIndex]
                == Cell.S){
            int[] rowArray = {sRow, oRow, RowIndex};
            int[] columnArray = {sColumn, oColumn, ColumnIndex};

            for (int i = 0; i < rowArray.length; i++){
                if (turn.equals("PLAYER1")){
                    if (colorGrid[rowArray[i]][columnArray[i]] == Color.BLUE ||
                            colorGrid[rowArray[i]][columnArray[i]] == Color.PURPLE){
                        colorGrid[rowArray[i]][columnArray[i]] = Color.PURPLE;
                    } else {
                        colorGrid[rowArray[i]][columnArray[i]] = Color.RED;
                    }
                } else {
                    if (colorGrid[rowArray[i]][columnArray[i]] == Color.RED ||
                            colorGrid[rowArray[i]][columnArray[i]] == Color.PURPLE){
                        colorGrid[rowArray[i]][columnArray[i]] = Color.PURPLE;
                    } else {
                        colorGrid[rowArray[i]][columnArray[i]] = Color.BLUE;
                    }
                }
            }
            return 1;
        }
        return 0;
    }

    public int determineSOS(int checkRow, int checkColumn, int placedRow, int placedColumn){
        if (gameGrid[placedRow][placedColumn] == Cell.S){
            if (gameGrid[checkRow][checkColumn] == Cell.O){
                return sosCheck(placedRow, placedColumn, checkRow, checkColumn);
            }
        }
        else if (gameGrid[placedRow][placedColumn] == Cell.O){
            if (gameGrid[checkRow][checkColumn] == Cell.S){
                return sosCheck(checkRow, checkColumn, placedRow, placedColumn);
            }
        }
        return 0;
    }
    public int checkBoardMin(int value){
        if (value - 1 <= 0) {return value;}
        return (value - 1);
    }

    public int checkBoardMax(int value){
        if (value + 1 >= BOARDSIZE) {return value;}
        return (value + 1);
    }

    public int scanBoard (int row, int column, String selectedCell){
        int SOS = 0;
        for (int rowCheck = checkBoardMin(row); rowCheck <= checkBoardMax(row); rowCheck++) {
            for (int columnCheck = checkBoardMin(column); columnCheck <= checkBoardMax(column); columnCheck++) {
                if (selectedCell.equals("S")) {SOS = SOS + determineSOS(rowCheck, columnCheck, row, column);}
                if (selectedCell.equals("O")) {SOS = SOS + determineSOS(rowCheck, columnCheck, row, column);}
            }
        }
        return (SOS);
    }

    public String makeMove(int row, int column){
        if (turn.equals("PLAYER1")){
            if (player1Selection.equals("S") || player1Selection.equals("O")) {
                return move(row, column, player1Selection);
            }
            else{
                System.out.println("Player 1 select S or O");
            }
        }
        else if (turn.equals("PLAYER2")){
            if (player2Selection.equals("S") || player2Selection.equals("O")) {
                return move(row, column, player2Selection);
            }
            else{
                System.out.println("Player 2 select S or O");
            }
        }
        return ("FAIL");
    }

    public String botMove(){
        Random rand = new Random();

        ArrayList<int[]> emptyCells = new ArrayList<>();
        for(int r = 0; r < BOARDSIZE; r++) {
            for(int c = 0; c < BOARDSIZE; c++) {
                if(getCell(r, c) == Board.Cell.EMPTY) {
                    emptyCells.add(new int[]{r, c});
                }
            }
        }

        if (emptyCells.isEmpty()) return "NO MOVE AVAILABLE";

        int[] move = emptyCells.get(rand.nextInt(emptyCells.size()));
        int row = move[0];
        int col = move[1];

        int SOSelect = rand.nextInt(2);
        if (turn.equals("PLAYER1")) {
            if (SOSelect == 0) {
                return move(row, col, "S");
            }
            else {
                return move(row, col, "O");
            }
        }
        else{
            if (SOSelect == 0) {
                return move(row, col, "S");
            }
            else {
                return move(row, col, "O");
            }
        }
    }

    private String move(int row, int column, String player1Selection) {
        if (row >= 0 && row < BOARDSIZE && column >= 0 && column < BOARDSIZE && gameGrid[row][column] == Cell.EMPTY) {
            gameGrid[row][column] = (player1Selection.equals("S")) ? Cell.S : Cell.O;
            unsetSOS = scanBoard(row, column, gameGrid[row][column].name());
            return row + "," + column + "," + player1Selection;
        }
        else {
            return ("INVALID");
        }
    }

    public boolean checkFullBoard(){
        for (int row = 0; row < BOARDSIZE; row++) {
            for (int column = 0; column < BOARDSIZE; column++) {
                if (gameGrid[row][column] == Cell.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    public String decideWin() {
        gameOver = true;
        if (player1SOSCount == player2SOSCount) {
            System.out.println("Tie Game");
            return "Tie Game";
        } else {
            if (player1SOSCount > player2SOSCount) {
                System.out.println("Player 1 Wins");
                return "Player 1 Wins";
            } else {
                System.out.println("Player 2 Wins");
                return "Player 2 Wins";
            }
        }
    }

    public String selectTurn() {
        boolean fullBoard = checkFullBoard();
        if (unsetSOS > 0){
            if (gamemodeSelected.equals("SIMPLE")){
                gameOver = true;
                if (turn.equals("PLAYER1")){
                    System.out.println("Player 1 Wins");
                    return "Player 1 Wins";
                } else {
                    System.out.println("Player 2 Wins");
                    return "Player 2 Wins";
                }
            } else {
                if (turn.equals("PLAYER1")){
                    player1SOSCount = player1SOSCount + unsetSOS;
                } else {
                    player2SOSCount = player2SOSCount + unsetSOS;
                }
                System.out.println(player1SOSCount + "  " + player2SOSCount);
                unsetSOS = 0;
            }
            if (fullBoard) {
                return decideWin();
            }
        } else {
            if (fullBoard) {
                return decideWin();
            }
            if (turn.equals("PLAYER1")){
                turn = "PLAYER2";
            } else {
                turn = "PLAYER1";
            }
        }
        return "NOSOS";
    }

}