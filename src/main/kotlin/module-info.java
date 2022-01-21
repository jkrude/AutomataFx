module AutomataFx {
  requires javafx.controls;
  requires javafx.fxml;
  requires kotlin.stdlib;
  requires com.jfoenix;
  opens com.jkrude.automata to javafx.fxml, javafx.base, javafx.graphics;
  opens com.jkrude.common to javafx.fxml, javafx.base, javafx.graphics;
  opens com.jkrude.games to javafx.fxml, javafx.base, javafx.graphics;
  exports com.jkrude.automata;
  exports com.jkrude.common;
  exports com.jkrude.games;
}