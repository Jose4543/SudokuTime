package org.example.sudoku;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import org.example.sudoku.model.GeneradorTablero;
import org.example.sudoku.model.Tablero;
import org.example.sudoku.model.Validador;
import org.example.sudoku.model.ValidadorSudoku;

import java.util.Optional;

/**
 * Controlador de la vista principal del Sudoku 6x6.
 * Conecta los eventos de la interfaz con el modelo.
 */
public class HelloController {

    // ---- Cronómetro ----
    /** Segundos que lleva transcurridos la partida actual. */
    private int segundosTranscurridos = 0;

    /** Línea de tiempo que suma un segundo cada vez. */
    private Timeline cronometro;

    // ---- Modelo ----
    private final Validador validador = new ValidadorSudoku();
    private final GeneradorTablero generador = new GeneradorTablero(validador);
    private Tablero tablero = new Tablero();

    // ---- Vista (inyectada desde el FXML) ----
    /** Etiqueta que muestra el tiempo transcurrido (mm:ss). */
    @FXML
    private Label lblTiempo;

    /** Cuadrícula que contiene los 36 botones del tablero. */
    @FXML
    private GridPane gridTablero;

    /** Botones del tablero organizados por [fila][columna]. */
    private final Button[][] celdas = new Button[Tablero.TAMANO][Tablero.TAMANO];

    /**
     * Se ejecuta automáticamente cuando JavaFX termina de cargar el FXML.
     * Organiza los 36 botones del tablero en la matriz {@link #celdas}.
     */
    @FXML
    private void initialize() {
        for (Node nodo : gridTablero.getChildren()) {
            // gridLinesVisible agrega nodos que no son botones, por eso se filtra
            if (nodo instanceof Button) {
                Integer f = GridPane.getRowIndex(nodo);
                Integer c = GridPane.getColumnIndex(nodo);
                // Scene Builder omite el índice cuando vale 0, y llega como null
                int fila = (f == null) ? 0 : f;
                int col = (c == null) ? 0 : c;
                celdas[fila][col] = (Button) nodo;
            }
        }
        actualizarEtiquetaTiempo();
    }

    // =====================================================================
    //  Dibujo del tablero
    // =====================================================================

    /** Muestra en pantalla los números que hay en el modelo. */
    private void pintarTablero() {
        for (int f = 0; f < Tablero.TAMANO; f++) {
            for (int c = 0; c < Tablero.TAMANO; c++) {
                int numero = tablero.get(f, c);
                celdas[f][c].setText(numero == 0 ? "" : String.valueOf(numero));
            }
        }
    }

    // =====================================================================
    //  Inicio y reinicio del juego
    // =====================================================================

    /**
     * Pide confirmación al jugador y, si acepta, genera un tablero nuevo,
     * lo muestra y reinicia el cronómetro.
     */
    @FXML
    private void handleJugar() {
        if (!confirmarNuevoJuego()) {
            return; // el jugador canceló: no se cambia nada
        }
        tablero = generador.generar();
        pintarTablero();
        iniciarCronometro();
    }

    /**
     * Muestra una alerta de confirmación antes de iniciar un juego nuevo.
     *
     * @return {@code true} si el jugador pulsó "Aceptar"
     */
    private boolean confirmarNuevoJuego() {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Nuevo juego");
        alerta.setHeaderText("¿Iniciar un nuevo juego?");
        alerta.setContentText("Se generará un tablero nuevo y se perderá el progreso actual.");
        Optional<ButtonType> respuesta = alerta.showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.OK;
    }

    /** Detiene el cronómetro, lo vuelve a 00:00 y limpia el tablero. */
    @FXML
    private void handleReiniciar() {
        detenerCronometro();
        segundosTranscurridos = 0;
        actualizarEtiquetaTiempo();
        tablero.limpiar();
        pintarTablero();
    }

    // =====================================================================
    //  Cronómetro
    // =====================================================================

    /** Pone el cronómetro en 00:00 y empieza a contar hacia arriba. */
    private void iniciarCronometro() {
        detenerCronometro(); // evita cronómetros duplicados
        segundosTranscurridos = 0;
        actualizarEtiquetaTiempo();

        cronometro = new Timeline(new KeyFrame(Duration.seconds(1), evento -> {
            segundosTranscurridos++;
            actualizarEtiquetaTiempo();
        }));
        cronometro.setCycleCount(Timeline.INDEFINITE);
        cronometro.play();
    }

    /** Detiene el cronómetro conservando el tiempo acumulado. */
    private void detenerCronometro() {
        if (cronometro != null) {
            cronometro.stop();
        }
    }

    /** Muestra el tiempo transcurrido con formato mm:ss. */
    private void actualizarEtiquetaTiempo() {
        int minutos = segundosTranscurridos / 60;
        int segundos = segundosTranscurridos % 60;
        lblTiempo.setText(String.format("%02d:%02d", minutos, segundos));
    }
}
