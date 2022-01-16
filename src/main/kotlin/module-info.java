module AutomataFx {
  requires javafx.controls;
  requires javafx.fxml;
  requires kotlin.stdlib;
  requires com.jfoenix;
  requires jfxtras.labs;
  opens com.jkrude.automata to javafx.fxml, javafx.base, javafx.graphics;
  exports com.jkrude.automata;
}