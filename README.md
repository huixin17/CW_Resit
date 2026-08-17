# 2048 — COMP2042 Resit Coursework

## GitHub
https://github.com/huixin17/CW_Resit

## Compilation Instructions
**Requirements:** Java 21 (JDK), Maven (or use the bundled `mvnw` wrapper), an internet connection the first time you build (to download the JavaFX dependency from Maven Central).

1. Clone the repository: `git clone https://github.com/huixin17/CW_Resit.git`
2. Open the project folder in IntelliJ IDEA as a Maven project (IntelliJ will detect `pom.xml` automatically and prompt to load it — accept).
3. Wait for Maven to finish downloading dependencies (JavaFX, JUnit 5).
4. Run the application either:
   - From IntelliJ: right-click `Launcher.java` (or `Main.java`) in `src/main/java/org/example/resit` → **Run**, or
   - From a terminal in the project root: `mvn javafx:run`
5. To run the automated tests: `mvn test`

No additional configuration, environment variables, or hardcoded paths are required.

## Implemented and Working Properly
- **Main menu** with Start Game and Resume options. Resume is disabled automatically if no saved game exists.
- **Core 2048 gameplay**: 4x4 grid, arrow key / WASD movement, tile merging, random tile spawning (90% chance of 2, 10% chance of 4), scoring, win detection at the 2048 tile, and game-over detection when no moves remain.
- **Undo**: reverts the single most recent move (grid and score), consistent with standard 2048 "take-back" implementations.
- **Pause**: press `P` or click Pause to freeze the board behind an overlay and block input; click Resume (or press `P` again) to continue. Undo is disabled while paused.
- **Persistent best score and save/resume**: the current game state (grid + score) is autosaved to disk after every move, so closing and reopening the app and clicking Resume restores exactly where you left off. Best score persists across sessions independently of the current game.
- **New Game** and **Main Menu** navigation from within an active game.

## Implemented but Not Working Properly
- **Game State Resume / Deserialization**:
  - **Issue**: Although the board state and score are saved to disk after each move, clicking "Resume" on the main menu fails to correctly restore the active game state (e.g., throwing a deserialization exception / failing to recreate the `Tile` object grid properly).
  - **Attempts to Resolve**: Attempted to parse the saved file and re-populate the `Board` object on app initialization, but UI grid rendering out-of-sync or state loading issues prevented the game board from restoring properly.

## Features Not Implemented
- **Move and Merge Tile Animations**: Tiles snap to their new positions instantly rather than sliding across the board. Left out due to time constraints to focus on core mechanics, board logic, and state persistence.
- **Multiple Board Sizes & Difficulty Levels**: The game is currently locked to the standard 4x4 grid size without customizable board dimensions (e.g., 5x5, 6x6) or modified tile spawn ratios.
- **Move History Stack (Multi-step Undo)**: Only single-step undo is implemented; an undo stack allowing players to roll back multiple consecutive moves was omitted to keep memory state management simple.
- **Animated High Score / Game-Over Overlays**: Transition effects and custom victory/game-over pop-up animations were omitted in favor of standard JavaFX overlays.

## New Java Classes
- `org.example.resit.model.Board` — all core game logic: grid state, move/merge rules per direction, random tile spawning, scoring, win/lose detection, single-step undo, and save/load persistence. Contains no UI code, so it is independently unit-testable.
- `org.example.resit.model.Tile` — immutable value object representing a single grid cell.
- `org.example.resit.model.Direction` — enum for the four move directions.
- `org.example.resit.model.GameStateListener` — Observer interface implemented by the UI layer to react to score/board/game-over changes without `Board` depending on JavaFX.
- `org.example.resit.controller.GameController` — builds and controls the in-game JavaFX view (grid rendering, buttons, keyboard input, pause overlay); implements `GameStateListener`.
- `org.example.resit.controller.MenuController` — builds the main menu view (Start Game / Resume).
- `org.example.resit.app.Main` — JavaFX `Application` entry point; owns the single `Scene` and switches its root between the menu and the game.

## Modified Java Classes
- `Launcher.java` — kept from the original template as the indirect launcher entry point, now forwards to `org.example.resit.app.Main` instead of the removed `HelloApplication`.
- `module-info.java` — updated to `requires javafx.controls` and export the new `app`, `model`, and `controller` packages; the original `HelloApplication`/`HelloController`/`fxml` scaffold and its `javafx.fxml` requirement were removed since the UI is built programmatically rather than via FXML.
- **Removed**: `HelloApplication.java` and `HelloController.java` — these were the default, unmodified JavaFX archetype template files and contained no game logic; they were deleted and replaced by the class structure described above.

## Unexpected Problems
- **Incomplete Starter Codebase**: The provided template repository contained only the bare JavaFX "Hello World" archetype (`HelloApplication`, `HelloController`, `Launcher`) without any core 2048 game components. As a result, the domain model (`Board`, `Tile`, `Direction`), UI observers (`GameStateListener`), controller logic (`GameController`, `MenuController`), and test suite (`BoardTest`) had to be designed and implemented from scratch rather than refactored from an existing game baseline.
- **JavaFX FXML Decoupling**: The template relied on `javafx.fxml` layout declarations. To ensure complete separation of UI rendering from business logic and support dynamic grid rendering, the original FXML setup was removed in favor of a programmatic JavaFX UI structure managed directly by `GameController` and `Main`.
- **State Serialization & Grid Deserialization Overhead**: Implementing robust file-based persistence for the `Board` model and `Tile` objects introduced synchronization issues between state loading and UI layout instantiation. Re-creating state without binding model classes to JavaFX properties led to board state out-of-sync bugs during runtime restoration.
