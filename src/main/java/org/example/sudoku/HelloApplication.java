package org.example.sudoku;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("sudokuView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 840, 680);
        stage.setTitle("Time of Sudoku!");
        stage.setScene(scene);
        stage.show();
    }
}
