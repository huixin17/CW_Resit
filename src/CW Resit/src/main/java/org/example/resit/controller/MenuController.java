package org.example.resit.controller;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import org.example.resit.model.Board;

/**
 * Builds the main menu screen shown when the application starts, offering
 * "Start Game" (begin a fresh game) and "Resume" (continue the last saved
 * game, if one exists).
 */
public class MenuController {

    /**
     * Builds the menu view.
     *
     * @param onStart  invoked when the player chooses to start a new game
     * @param onResume invoked when the player chooses to resume; only
     *                 offered if a saved game exists
     * @return the root node for the menu scene
     */
    public VBox buildView(Runnable onStart, Runnable onResume) {
        Label title = new Label("2048");
        title.setFont(Font.font(null, FontWeight.BOLD, 48));

        Label subtitle = new Label("COMP2042 Resit Coursework");
        subtitle.setFont(Font.font(14));

        Button startButton = new Button("Start Game");
        startButton.setPrefWidth(160);
        startButton.setOnAction(e -> onStart.run());

        Button resumeButton = new Button("Resume");
        resumeButton.setPrefWidth(160);
        resumeButton.setDisable(!Board.hasSavedGame());
        resumeButton.setOnAction(e -> onResume.run());

        VBox root = new VBox(16, title, subtitle, startButton, resumeButton);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #faf8ef;");
        return root;
    }
}
