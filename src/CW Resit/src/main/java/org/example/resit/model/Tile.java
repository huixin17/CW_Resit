package org.example.resit.model;

/**
 * A single numbered tile on the board. Immutable value holder used by
 * {@link Board} to represent grid contents.
 */
public class Tile {

    private final int value;

    /**
     * Creates a tile with the given value.
     *
     * @param value the tile's numeric value (0 represents an empty cell)
     */
    public Tile(int value) {
        this.value = value;
    }

    /**
     * @return the numeric value of this tile
     */
    public int getValue() {
        return value;
    }

    /**
     * @return true if this tile represents an empty cell
     */
    public boolean isEmpty() {
        return value == 0;
    }
}
