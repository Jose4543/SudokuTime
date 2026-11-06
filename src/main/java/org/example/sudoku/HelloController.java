package org.example.sudoku;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private int tiempoRestante = 120;
    private Timeline temporizador;
    private Label welcomeText;
    @FXML
    private Label lblTiempo;

    @FXML

    private void iniciarTemporizador() {

        // Evitar crear varios temporizadores
        if (temporizador != null) {
            temporizador.stop();
        }

        tiempoRestante = 120;
        lblTiempo.setText(String.valueOf(tiempoRestante));

        temporizador = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {

                    tiempoRestante--;

                    lblTiempo.setText(String.valueOf(tiempoRestante));

                    if (tiempoRestante <= 0) {
                        temporizador.stop();
                        finalizarJuego();
                    }
                })
        );

        temporizador.setCycleCount(Timeline.INDEFINITE);
        temporizador.play();
    }
    private void finalizarJuego() {
        lblTiempo.setText("0");

        System.out.println("¡Se acabó el tiempo!");

        // alerta de juego terminado.
    }
    @FXML
    private void handleJugar() {
        iniciarTemporizador();
    }
    @FXML
    private void handleReiniciar() {

        if (temporizador != null) {
            temporizador.stop();
        }

        tiempoRestante = 120;
        lblTiempo.setText("120");
    }
}
