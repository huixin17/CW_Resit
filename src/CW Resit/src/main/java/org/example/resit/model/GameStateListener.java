package org.example.resit.model;

/**
 * Observer interface used by {@link Board} to notify listeners (e.g. the UI
 * controller) whenever the game state changes, decoupling game logic from
 * presentation. Implements the Observer design pattern.
 */
public interface GameStateListener {

    /**
     * Called whenever the score changes.
     *
     * @param newScore the updated score
     */
    void onScoreChanged(int newScore);

    /**
     * Called whenever the board grid changes (after a move or reset).
     */
    void onBoardChanged();

    /**
     * Called when the game reaches a terminal state.
     *
     * @param won true if the player reached the 2048 tile, false if the
     *            board is simply full with no valid moves left
     */
    void onGameOver(boolean won);
}
