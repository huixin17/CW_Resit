package org.example.resit.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Core game logic for a 4x4 game of 2048: grid state, moves, merging,
 * scoring, win/lose detection, a single-step undo, and a best-score value
 * persisted to disk across sessions.
 *
 * <p>This class contains no UI code. It notifies registered
 * {@link GameStateListener}s of any state change (Observer pattern), so it
 * can be reused by any front end (JavaFX, console, tests, etc.).</p>
 */
public class Board {

    public static final int SIZE = 4;
    private static final Path BEST_SCORE_FILE = Paths.get(System.getProperty("user.home"), ".resit2048_best");
    private static final Path SAVE_FILE = Paths.get(System.getProperty("user.home"), ".resit2048_save");

    private final int[][] grid = new int[SIZE][SIZE];
    private final Random random = new Random();
    private final List<GameStateListener> listeners = new ArrayList<>();

    private int score;
    private int bestScore;
    private boolean gameOverNotified;

    // single-step undo support
    private int[][] previousGrid;
    private int previousScore;
    private boolean undoAvailable;

    public Board() {
        bestScore = loadBestScore();
        reset();
    }

    /**
     * Registers a listener to be notified of score, board, and game-over
     * changes.
     *
     * @param listener the listener to add
     */
    public void addListener(GameStateListener listener) {
        listeners.add(listener);
    }

    /**
     * Clears the board, resets the score, and spawns the two starting
     * tiles.
     */
    public void reset() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = 0;
            }
        }
        score = 0;
        gameOverNotified = false;
        undoAvailable = false;
        spawnRandomTile();
        spawnRandomTile();
        fireBoardChanged();
        fireScoreChanged();
        saveGame();
    }

    /**
     * Attempts to move/merge all tiles in the given direction. If the move
     * changes the board, a new random tile is spawned, the score and
     * listeners are updated, and the current state is saved for one-step
     * undo.
     *
     * @param direction the direction to move
     * @return true if the board changed as a result of this move
     */
    public boolean move(Direction direction) {
        int[][] before = deepCopy(grid);
        int scoreBefore = score;

        switch (direction) {
            case LEFT -> moveLeft();
            case RIGHT -> {
                flipHorizontal();
                moveLeft();
                flipHorizontal();
            }
            case UP -> {
                transpose();
                moveLeft();
                transpose();
            }
            case DOWN -> {
                transpose();
                flipHorizontal();
                moveLeft();
                flipHorizontal();
                transpose();
            }
        }

        boolean changed = !gridsEqual(before, grid);
        if (changed) {
            previousGrid = before;
            previousScore = scoreBefore;
            undoAvailable = true;

            spawnRandomTile();
            fireBoardChanged();
            fireScoreChanged();
            checkGameOver();
            saveGame();
        }
        return changed;
    }

    /**
     * Reverts the board to the state before the last successful move.
     * Only one level of undo is supported (per the coursework's undo
     * feature), matching typical 2048 "take-back" implementations.
     *
     * @return true if an undo was performed
     */
    public boolean undo() {
        if (!undoAvailable) {
            return false;
        }
        for (int r = 0; r < SIZE; r++) {
            System.arraycopy(previousGrid[r], 0, grid[r], 0, SIZE);
        }
        score = previousScore;
        undoAvailable = false;
        gameOverNotified = false;
        fireBoardChanged();
        fireScoreChanged();
        saveGame();
        return true;
    }

    /**
     * @return true if an undo is currently available
     */
    public boolean isUndoAvailable() {
        return undoAvailable;
    }

    /**
     * Compresses and merges a single row to the left, adding merged values
     * to the score. Used as the base case that all four directions are
     * implemented in terms of (via transpose/flip).
     */
    private void moveLeft() {
        for (int r = 0; r < SIZE; r++) {
            int[] row = grid[r];
            int[] merged = mergeRowLeft(row);
            grid[r] = merged;
        }
    }

    private int[] mergeRowLeft(int[] row) {
        int[] compressed = compress(row);
        for (int i = 0; i < SIZE - 1; i++) {
            if (compressed[i] != 0 && compressed[i] == compressed[i + 1]) {
                compressed[i] *= 2;
                score += compressed[i];
                compressed[i + 1] = 0;
            }
        }
        return compress(compressed);
    }

    private int[] compress(int[] row) {
        int[] result = new int[SIZE];
        int idx = 0;
        for (int value : row) {
            if (value != 0) {
                result[idx++] = value;
            }
        }
        return result;
    }

    private void transpose() {
        int[][] copy = deepCopy(grid);
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = copy[c][r];
            }
        }
    }

    private void flipHorizontal() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE / 2; c++) {
                int tmp = grid[r][c];
                grid[r][c] = grid[r][SIZE - 1 - c];
                grid[r][SIZE - 1 - c] = tmp;
            }
        }
    }

    /**
     * Places a new tile (90% chance of 2, 10% chance of 4) in a random
     * empty cell, if any are available.
     */
    public void spawnRandomTile() {
        List<int[]> empty = new ArrayList<>();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] == 0) {
                    empty.add(new int[]{r, c});
                }
            }
        }
        if (empty.isEmpty()) {
            return;
        }
        int[] cell = empty.get(random.nextInt(empty.size()));
        grid[cell[0]][cell[1]] = random.nextDouble() < 0.9 ? 2 : 4;
    }

    private void checkGameOver() {
        if (gameOverNotified) {
            return;
        }
        boolean won = hasTile(2048);
        if (won || isBoardFullWithNoMoves()) {
            gameOverNotified = true;
            for (GameStateListener l : listeners) {
                l.onGameOver(won);
            }
        }
    }

    private boolean hasTile(int value) {
        for (int[] row : grid) {
            for (int v : row) {
                if (v == value) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return true if the grid is full and no two adjacent tiles can merge
     */
    public boolean isBoardFullWithNoMoves() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] == 0) {
                    return false;
                }
                if (c < SIZE - 1 && grid[r][c] == grid[r][c + 1]) {
                    return false;
                }
                if (r < SIZE - 1 && grid[r][c] == grid[r + 1][c]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @return a defensive copy of the current grid as {@link Tile} objects,
     *         for use by the UI layer
     */
    public Tile[][] getGrid() {
        Tile[][] tiles = new Tile[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                tiles[r][c] = new Tile(grid[r][c]);
            }
        }
        return tiles;
    }

    /**
     * @return the current score
     */
    public int getScore() {
        return score;
    }

    /**
     * @return the best score achieved across all sessions
     */
    public int getBestScore() {
        return bestScore;
    }

    private void fireScoreChanged() {
        if (score > bestScore) {
            bestScore = score;
            saveBestScore(bestScore);
        }
        for (GameStateListener l : listeners) {
            l.onScoreChanged(score);
        }
    }

    private void fireBoardChanged() {
        for (GameStateListener l : listeners) {
            l.onBoardChanged();
        }
    }

    /**
     * @return true if a saved game exists on disk that {@link #loadGame()}
     *         can restore
     */
    public static boolean hasSavedGame() {
        return Files.exists(SAVE_FILE);
    }

    /**
     * Persists the current grid and score to disk so the game can be
     * resumed later via {@link #loadGame()}. Called automatically after
     * every move, undo, and reset.
     */
    private void saveGame() {
        StringBuilder sb = new StringBuilder();
        sb.append(score).append(System.lineSeparator());
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                sb.append(grid[r][c]);
                if (c < SIZE - 1) {
                    sb.append(',');
                }
            }
            sb.append(System.lineSeparator());
        }
        try {
            Files.writeString(SAVE_FILE, sb.toString());
        } catch (IOException e) {
            // Autosave is a convenience feature; failures are non-fatal.
        }
    }

    /**
     * Restores the grid and score from the file written by
     * {@link #saveGame()}, replacing the current in-memory state.
     *
     * @return true if a saved game was found and successfully loaded
     */
    public boolean loadGame() {
        if (!Files.exists(SAVE_FILE)) {
            return false;
        }
        try {
            List<String> lines = Files.readAllLines(SAVE_FILE);
            if (lines.size() < SIZE + 1) {
                return false;
            }
            int loadedScore = Integer.parseInt(lines.get(0).trim());
            int[][] loadedGrid = new int[SIZE][SIZE];
            for (int r = 0; r < SIZE; r++) {
                String[] parts = lines.get(r + 1).split(",");
                if (parts.length != SIZE) {
                    return false;
                }
                for (int c = 0; c < SIZE; c++) {
                    loadedGrid[r][c] = Integer.parseInt(parts[c].trim());
                }
            }
            for (int r = 0; r < SIZE; r++) {
                System.arraycopy(loadedGrid[r], 0, grid[r], 0, SIZE);
            }
            score = loadedScore;
            gameOverNotified = false;
            undoAvailable = false;
            fireBoardChanged();
            fireScoreChanged();
            return true;
        } catch (IOException | NumberFormatException e) {
            return false;
        }
    }

    private int loadBestScore() {
        try {
            if (Files.exists(BEST_SCORE_FILE)) {
                String content = Files.readString(BEST_SCORE_FILE).trim();
                return content.isEmpty() ? 0 : Integer.parseInt(content);
            }
        } catch (IOException | NumberFormatException e) {
            // Fall back to 0 if the file is missing or corrupted; not a fatal error.
        }
        return 0;
    }

    private void saveBestScore(int value) {
        try {
            Files.writeString(BEST_SCORE_FILE, String.valueOf(value));
        } catch (IOException e) {
            // Persisting the best score is a nice-to-have; failures are non-fatal.
        }
    }

    private static int[][] deepCopy(int[][] source) {
        int[][] copy = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i].clone();
        }
        return copy;
    }

    private static boolean gridsEqual(int[][] a, int[][] b) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (a[r][c] != b[r][c]) {
                    return false;
                }
            }
        }
        return true;
    }
}
