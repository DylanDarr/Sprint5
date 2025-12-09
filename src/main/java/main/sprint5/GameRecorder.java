package main.sprint5;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GameRecorder {
    private final String filename = "game_record.txt";

    public void startNewGame(int boardSize, String gameMode) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(boardSize + "," + gameMode);
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void recordMove(String playerTurn, int row, int col, String selection) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(playerTurn + "," + row + "," + col + "," + selection);
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> loadMoves() {
        List<String> moves = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                moves.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return moves;
    }
}
