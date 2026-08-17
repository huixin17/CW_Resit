package org.example.resit.app;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.example.resit.controller.GameController;
import org.example.resit.controller.MenuController;
import org.example.resit.model.Board;

/**
 * JavaFX application entry point. Shows a main menu (Start Game / Resume)
 * before entering the game itself, and swaps the scene root to switch
 * between the two screens. Delegates all actual construction to
 * {@link MenuController} and {@link GameController}, keeping this class
 * focused solely on window and navigation setup.
 */
public class Main extends Application {

    private Scene scene;

    @Override
    public void start(Stage stage) {
        scene = new Scene(showMenu(), 420, 560);
        stage.setTitle("2048 - COMP2042 Resit Coursework");
        stage.setScene(scene);
        stage.show();
    }

    private Parent showMenu() {
        MenuController menu = new MenuController();
        return menu.buildView(this::startNewGame, this::resumeGame);
    }

    private void startNewGame() {
        Board board = new Board();
        board.reset();
        openGame(board);
    }

    private void resumeGame() {
        Board board = new Board();
        board.loadGame();
        openGame(board);
    }

    private void openGame(Board board) {
        GameController controller = new GameController(board);
        controller.setOnBackToMenu(() -> scene.setRoot(showMenu()));

        BorderPane root = controller.buildView();
        scene.setRoot(root);
        controller.requestGameFocus(root);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
