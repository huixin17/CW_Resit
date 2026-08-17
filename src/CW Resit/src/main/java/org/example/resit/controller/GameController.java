package org.example.resit.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import org.example.resit.model.Board;
import org.example.resit.model.Direction;
import org.example.resit.model.GameStateListener;
import org.example.resit.model.Tile;

/**
 * Builds and controls the JavaFX view for the 2048 game. Listens to
 * {@link Board} state changes (Observer pattern) and re-renders the grid,
 * score, and best score accordingly. Also wires up keyboard input and the
 * New Game / Undo buttons.
 */
public class GameController implements GameStateListener {

    private static final int CELL_SIZE = 90;
    private static final int GAP = 10;

    private final Board board;
    private final GridPane gridPane = new GridPane();
    private final Label scoreLabel = new Label();
    private final Label bestScoreLabel = new Label();
    private final Button undoButton = new Button("Undo");
    private Runnable onBackToMenu;

    /**
     * Sets the action to run when the player clicks "Main Menu". Optional —
     * if never set, no Main Menu button is shown.
     *
     * @param onBackToMenu callback invoked when the button is clicked
     */
    public void setOnBackToMenu(Runnable onBackToMenu) {
        this.onBackToMenu = onBackToMenu;
    }

    /**
     * Creates a controller for the given board. The board should already be
     * in the state the caller wants to display (freshly reset for a new
     * game, or already loaded via {@link Board#loadGame()} for a resumed
     * game).
     *
     * @param board the board to render and control
     */
    public GameController(Board board) {
        this.board = board;
    }

    /**
     * Builds the root JavaFX node for the game and starts listening to the
     * underlying {@link Board}.
     *
     * @return the root node to attach to the scene
     */
    public BorderPane buildView() {
        board.addListener(this);

        gridPane.setHgap(GAP);
        gridPane.setVgap(GAP);
        gridPane.setPadding(new Insets(GAP));
        gridPane.setStyle("-fx-background-color: #bbada0;");

        Button newGameButton = new Button("New Game");
        newGameButton.setOnAction(e -> board.reset());

        undoButton.setOnAction(e -> board.undo());

        scoreLabel.setFont(Font.font(null, FontWeight.BOLD, 16));
        bestScoreLabel.setFont(Font.font(null, FontWeight.BOLD, 16));

        HBox scoreBox = new HBox(20, scoreLabel, bestScoreLabel);
        scoreBox.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(10, newGameButton, undoButton);
        if (onBackToMenu != null) {
            Button menuButton = new Button("Main Menu");
            menuButton.setOnAction(e -> onBackToMenu.run());
            buttonBox.getChildren().add(menuButton);
        }
        buttonBox.setAlignment(Pos.CENTER);

        VBox top = new VBox(10, scoreBox, buttonBox);
        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(gridPane);

        root.setOnKeyPressed(event -> {
            Direction direction = switch (event.getCode()) {
                case UP, W -> Direction.UP;
                case DOWN, S -> Direction.DOWN;
                case LEFT, A -> Direction.LEFT;
                case RIGHT, D -> Direction.RIGHT;
                default -> null;
            };
            if (direction != null) {
                board.move(direction);
            }
        });

        render();
        return root;
    }

    /**
     * Gives keyboard focus to the game so arrow keys are captured
     * immediately. Should be called once the scene is shown.
     *
     * @param root the root node returned by {@link #buildView()}
     */
    public void requestGameFocus(BorderPane root) {
        root.setFocusTraversable(true);
        root.requestFocus();
    }

    private void render() {
        gridPane.getChildren().clear();
        Tile[][] grid = board.getGrid();
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                gridPane.add(buildCell(grid[r][c]), c, r);
            }
        }
        undoButton.setDisable(!board.isUndoAvailable());
    }

    private StackPane buildCell(Tile tile) {
        Rectangle background = new Rectangle(CELL_SIZE, CELL_SIZE);
        background.setArcWidth(8);
        background.setArcHeight(8);
        background.setFill(colorFor(tile.getValue()));

        Label label = new Label(tile.isEmpty() ? "" : String.valueOf(tile.getValue()));
        label.setFont(Font.font(null, FontWeight.BOLD, 22));
        label.setTextFill(tile.getValue() <= 4 ? Color.web("#776e65") : Color.WHITE);

        return new StackPane(background, label);
    }

    private Color colorFor(int value) {
        return switch (value) {
            case 0 -> Color.web("#cdc1b4");
            case 2 -> Color.web("#eee4da");
            case 4 -> Color.web("#ede0c8");
            case 8 -> Color.web("#f2b179");
            case 16 -> Color.web("#f59563");
            case 32 -> Color.web("#f67c5f");
            case 64 -> Color.web("#f65e3b");
            case 128 -> Color.web("#edcf72");
            case 256 -> Color.web("#edcc61");
            case 512 -> Color.web("#edc850");
            case 1024 -> Color.web("#edc53f");
            case 2048 -> Color.web("#edc22e");
            default -> Color.web("#3c3a32");
        };
    }

    @Override
    public void onScoreChanged(int newScore) {
        scoreLabel.setText("Score: " + newScore);
        bestScoreLabel.setText("Best: " + board.getBestScore());
    }

    @Override
    public void onBoardChanged() {
        render();
    }

    @Override
    public void onGameOver(boolean won) {
        Alert alert = new Alert(won ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
        alert.setTitle(won ? "You win!" : "Game over");
        alert.setHeaderText(won ? "You reached 2048!" : "No more moves available.");
        alert.setContentText("Final score: " + board.getScore());
        alert.showAndWait();
    }
}
