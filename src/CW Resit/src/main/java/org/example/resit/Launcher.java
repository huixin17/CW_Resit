package org.example.resit;

import org.example.resit.app.Main;

/**
 * Indirect launcher for the JavaFX application. Some IDE/runtime
 * configurations fail to launch a class that extends
 * {@code javafx.application.Application} directly when run from a fat/shaded
 * jar; going through a plain class with a {@code main} method avoids that
 * issue. Kept as its own class (as in the original template) rather than
 * merged into {@code Main} for that reason.
 */
public class Launcher {

    public static void main(String[] args) {
        Main.main(args);
    }
}
