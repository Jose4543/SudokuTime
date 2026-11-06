module org.example.sudoku {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.sudoku to javafx.fxml;
    exports org.example.sudoku;
}