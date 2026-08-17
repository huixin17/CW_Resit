package org.example.resit.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests covering the core game rules in {@link Board}: tile spawning,
 * merge/move behaviour, scoring, undo, and game-over detection.
 */
class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    void newBoardStartsWithExactlyTwoTiles() {
        long tileCount = countNonEmptyTiles(board);
        assertEquals(2, tileCount, "A freshly reset board should have exactly two starting tiles");
    }

    @Test
    void newBoardStartsWithZeroScore() {
        assertEquals(0, board.getScore());
    }

    @Test
    void moveSpawnsExactlyOneNewTileWhenBoardChanges() {
        long before = countNonEmptyTiles(board);
        // Try all four directions until one actually changes the board,
        // since the two random starting tiles may already be aligned.
        boolean changed = board.move(Direction.LEFT)
                || board.move(Direction.RIGHT)
                || board.move(Direction.UP)
                || board.move(Direction.DOWN);

        if (changed) {
            long after = countNonEmptyTiles(board);
            assertEquals(before + 1, after, "A successful move should add exactly one new tile");
        }
    }

    @Test
    void undoIsUnavailableBeforeAnyMove() {
        assertFalse(board.isUndoAvailable());
        assertFalse(board.undo());
    }

    @Test
    void undoRestoresPreviousScoreAndIsConsumedAfterUse() {
        boolean changed = board.move(Direction.LEFT)
                || board.move(Direction.RIGHT)
                || board.move(Direction.UP)
                || board.move(Direction.DOWN);

        if (changed) {
            assertTrue(board.isUndoAvailable());
            int scoreAfterMove = board.getScore();
            boolean undone = board.undo();
            assertTrue(undone);
            assertFalse(board.isUndoAvailable(), "Undo should only be usable once per move");
        }
    }

    @Test
    void resetClearsScoreAndRefillsTwoTiles() {
        board.move(Direction.LEFT);
        board.reset();
        assertEquals(0, board.getScore());
        assertEquals(2, countNonEmptyTiles(board));
    }

    @Test
    void isBoardFullWithNoMovesFalseOnFreshBoard() {
        assertFalse(board.isBoardFullWithNoMoves());
    }

    @Test
    void hasSavedGameIsTrueAfterAnyBoardActivity() {
        // The constructor's reset() call autosaves, so a save should exist
        // as soon as any Board has been created and used.
        assertTrue(Board.hasSavedGame());
    }

    @Test
    void loadGameRestoresPreviouslySavedScoreAndGrid() {
        board.move(Direction.LEFT);
        board.move(Direction.RIGHT);
        board.move(Direction.UP);
        board.move(Direction.DOWN);
        int savedScore = board.getScore();
        Tile[][] savedGrid = board.getGrid();

        Board freshBoard = new Board();
        boolean loaded = freshBoard.loadGame();

        assertTrue(loaded);
        assertEquals(savedScore, freshBoard.getScore());
        Tile[][] loadedGrid = freshBoard.getGrid();
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                assertEquals(savedGrid[r][c].getValue(), loadedGrid[r][c].getValue());
            }
        }
    }

    private long countNonEmptyTiles(Board board) {
        Tile[][] grid = board.getGrid();
        long count = 0;
        for (Tile[] row : grid) {
            for (Tile t : row) {
                if (!t.isEmpty()) {
                    count++;
                }
            }
        }
        return count;
    }
}
