package org.example.sudoku;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import org.example.sudoku.model.GeneradorTablero;
import org.example.sudoku.model.Jugada;
import org.example.sudoku.model.Tablero;
import org.example.sudoku.model.Validador;
import org.example.sudoku.model.ValidadorSudoku;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Random;

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

    /** Indica qué celdas vinieron con número desde el inicio (no se pueden editar). */
    private final boolean[][] fijas = new boolean[Tablero.TAMANO][Tablero.TAMANO];

    /** Indica qué celdas tienen un número que se repite (se resaltan en rojo). */
    private final boolean[][] errores = new boolean[Tablero.TAMANO][Tablero.TAMANO];

    /** Pila con las jugadas del jugador; la última jugada queda arriba (para deshacer). */
    private final Deque<Jugada> historial = new ArrayDeque<>();

    /** Cantidad máxima de pistas por partida. */
    private static final int MAX_PISTAS = 3;

    /** Pistas que todavía puede pedir el jugador en esta partida. */
    private int pistasRestantes = MAX_PISTAS;

    /** Fila de la celda con pista visible (-1 si no hay). */
    private int pistaFila = -1;

    /** Columna de la celda con pista visible (-1 si no hay). */
    private int pistaCol = -1;

    /** Texto explicativo de la pista actual (vacío si no hay). */
    private String mensajePista = "";

    private final Random random = new Random();

    /** Indica si hay una partida en curso. */
    private boolean juegoActivo = false;

    // ---- Vista (inyectada desde el FXML) ----
    /** Etiqueta que muestra el tiempo transcurrido (mm:ss). */
    @FXML
    private Label lblTiempo;

    /** Botón "Pista" (se usa para mostrar cuántas pistas quedan). */
    @FXML
    private Button btnPista;

    /** Etiqueta donde se muestran los mensajes de error y de pista. */
    @FXML
    private Label lblMensaje;

    /** Cuadrícula que contiene los 36 botones del tablero. */
    @FXML
    private GridPane gridTablero;

    /** Botones del tablero organizados por [fila][columna]. */
    private final Button[][] celdas = new Button[Tablero.TAMANO][Tablero.TAMANO];

    /** Estilo con que cada botón viene definido en el FXML. */
    private final String[][] estilosBase = new String[Tablero.TAMANO][Tablero.TAMANO];

    /** Estilo que se agrega a las celdas con número inicial. */
    private static final String ESTILO_FIJA =
            "-fx-background-color: #E8F0E9; -fx-font-weight: bold;";

    /** Estilo que se agrega a la celda con la pista visible. */
    private static final String ESTILO_PISTA =
            "-fx-background-color: #FFF3B0; -fx-border-color: #E08A00; -fx-border-width: 2;"
                    + " -fx-text-fill: #E08A00; -fx-font-style: italic;";

    /** Estilo que se agrega a las celdas con números repetidos. */
    private static final String ESTILO_ERROR =
            "-fx-border-color: #C62828; -fx-border-width: 3; -fx-text-fill: #C62828;";

    /** Estilo que se agrega a la celda seleccionada. */
    private static final String ESTILO_SELECCION =
            "-fx-background-color: #CFE5D3; -fx-border-color: #234536; -fx-border-width: 2;";

    /** Fila de la celda seleccionada (-1 si no hay ninguna). */
    private int filaSeleccionada = -1;

    /** Columna de la celda seleccionada (-1 si no hay ninguna). */
    private int colSeleccionada = -1;

    /**
     * Se ejecuta automáticamente cuando JavaFX termina de cargar el FXML.
     * Organiza los 36 botones, registra los eventos de mouse y de teclado.
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
                Button boton = (Button) nodo;
                celdas[fila][col] = boton;
                estilosBase[fila][col] = boton.getStyle();
                boton.setOnMouseClicked(evento -> seleccionarCelda(fila, col));
            }
        }
        actualizarEtiquetaTiempo();

        // La escena no existe todavía en initialize(), así que se espera a que aparezca
        gridTablero.sceneProperty().addListener((obs, anterior, escena) -> {
            if (escena != null) {
                escena.setOnKeyPressed(this::manejarTecla);
            }
        });
    }

    // =====================================================================
    //  Dibujo del tablero
    // =====================================================================

    /**
     * Calcula el estilo completo de una celda sumando, en este orden:
     * el estilo del FXML, si es inicial, si está seleccionada y si tiene error.
     * Lo último que se agrega es lo que se ve (el rojo gana sobre la selección).
     */
    private String estiloDe(int fila, int col) {
        String estilo = estilosBase[fila][col];
        if (fijas[fila][col]) {
            estilo += ESTILO_FIJA;
        }
        if (fila == filaSeleccionada && col == colSeleccionada) {
            estilo += ESTILO_SELECCION;
        }
        if (fila == pistaFila && col == pistaCol) {
            estilo += ESTILO_PISTA;
        }
        if (errores[fila][col]) {
            estilo += ESTILO_ERROR;
        }
        return estilo;
    }

    /** Aplica a una celda su estilo actual. */
    private void refrescarEstilo(int fila, int col) {
        celdas[fila][col].setStyle(estiloDe(fila, col));
    }

    /** Muestra en pantalla los números que hay en el modelo y revalida el tablero. */
    private void pintarTablero() {
        for (int f = 0; f < Tablero.TAMANO; f++) {
            for (int c = 0; c < Tablero.TAMANO; c++) {
                int numero = tablero.get(f, c);
                celdas[f][c].setText(numero == 0 ? "" : String.valueOf(numero));
            }
        }
        actualizarErrores();
    }

    // =====================================================================
    //  Validación en tiempo real
    // =====================================================================

    /**
     * Revisa todas las celdas con número y marca las que se repiten en su
     * fila, columna o bloque. Se llama después de cada cambio, así que los
     * errores aparecen y desaparecen al instante. También actualiza el mensaje.
     */
    private void actualizarErrores() {
        int cantidad = 0;
        for (int f = 0; f < Tablero.TAMANO; f++) {
            for (int c = 0; c < Tablero.TAMANO; c++) {
                int numero = tablero.get(f, c);
                errores[f][c] = numero != 0 && !validador.esValido(tablero, f, c, numero);
                if (errores[f][c]) {
                    cantidad++;
                }
                refrescarEstilo(f, c);
            }
        }
        mostrarMensaje(cantidad);
    }

    /**
     * Muestra u oculta el mensaje de error según cuántas celdas están mal.
     *
     * @param celdasConError cantidad de celdas con números repetidos
     */
    private void mostrarMensaje(int celdasConError) {
        if (lblMensaje == null) {
            return; // el label aún no está en el FXML
        }
        if (celdasConError > 0) {
            lblMensaje.setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold;");
            lblMensaje.setText("Hay números repetidos en fila, columna o bloque. Revisa las celdas en rojo.");
        } else if (!mensajePista.isEmpty()) {
            lblMensaje.setStyle("-fx-text-fill: #B26A00; -fx-font-weight: bold;");
            lblMensaje.setText(mensajePista);
        } else {
            lblMensaje.setText("");
        }
    }

    // =====================================================================
    //  Selección de celdas (mouse)
    // =====================================================================

    /**
     * Marca una celda como seleccionada y quita la marca de la anterior.
     *
     * @param fila fila de la celda (0-5)
     * @param col  columna de la celda (0-5)
     */
    private void seleccionarCelda(int fila, int col) {
        limpiarSeleccion();
        filaSeleccionada = fila;
        colSeleccionada = col;
        refrescarEstilo(fila, col);
    }

    /** Quita la selección actual, si existe, y restaura el estilo de la celda. */
    private void limpiarSeleccion() {
        int filaAnterior = filaSeleccionada;
        int colAnterior = colSeleccionada;
        filaSeleccionada = -1;
        colSeleccionada = -1;
        if (filaAnterior >= 0) {
            refrescarEstilo(filaAnterior, colAnterior);
        }
    }

    // =====================================================================
    //  Ingreso y borrado de números (teclado y botones en pantalla)
    // =====================================================================

    /**
     * Atiende cualquier tecla pulsada: dígitos 1-6 escriben, Backspace o
     * Delete borran. Las demás teclas se ignoran.
     *
     * @param evento evento de teclado
     */
    private void manejarTecla(KeyEvent evento) {
        KeyCode tecla = evento.getCode();
        int numero = numeroDeTecla(tecla);
        if (numero != 0) {
            colocarNumero(numero);
        } else if (tecla == KeyCode.BACK_SPACE || tecla == KeyCode.DELETE) {
            borrarNumero();
        }
    }

    /**
     * Convierte una tecla en un número del 1 al 6.
     *
     * @param tecla tecla pulsada
     * @return el número, o 0 si la tecla no es un dígito entre 1 y 6
     */
    private int numeroDeTecla(KeyCode tecla) {
        switch (tecla) {
            case DIGIT1: case NUMPAD1: return 1;
            case DIGIT2: case NUMPAD2: return 2;
            case DIGIT3: case NUMPAD3: return 3;
            case DIGIT4: case NUMPAD4: return 4;
            case DIGIT5: case NUMPAD5: return 5;
            case DIGIT6: case NUMPAD6: return 6;
            default: return 0;
        }
    }

    /**
     * Atiende los botones numéricos de la pantalla (1-6). Los seis botones
     * comparten este método; el número se lee del texto del botón pulsado.
     *
     * @param evento evento de acción del botón
     */
    @FXML
    private void handleNumero(ActionEvent evento) {
        Button boton = (Button) evento.getSource();
        colocarNumero(Integer.parseInt(boton.getText()));
    }

    /** Atiende el botón "Borrar" de la pantalla. */
    @FXML
    private void handleBorrar() {
        borrarNumero();
    }

    /**
     * Escribe un número en la celda seleccionada, si se puede editar.
     *
     * @param numero número a escribir (1-6)
     */
    private void colocarNumero(int numero) {
        if (!sePuedeEditarSeleccion()) {
            return;
        }
        int fila = filaSeleccionada;
        int col = colSeleccionada;
        int anterior = tablero.get(fila, col);
        if (anterior != numero) {
            historial.push(new Jugada(fila, col, anterior));
        }
        tablero.set(fila, col, numero);
        celdas[fila][col].setText(String.valueOf(numero));
        if (fila == pistaFila && col == pistaCol) {
            limpiarPista();
        }
        actualizarErrores();
        if (tableroCompletoYValido()) {
            finalizarJuego();
        }
    }

    /** Vacía la celda seleccionada, si se puede editar. */
    private void borrarNumero() {
        if (!sePuedeEditarSeleccion()) {
            return;
        }
        int fila = filaSeleccionada;
        int col = colSeleccionada;
        int anterior = tablero.get(fila, col);
        if (anterior != 0) {
            historial.push(new Jugada(fila, col, anterior));
        }
        tablero.borrar(fila, col);
        celdas[fila][col].setText("");
        if (fila == pistaFila && col == pistaCol) {
            limpiarPista();
        }
        actualizarErrores();
    }

    /**
     * Indica si hay una partida en curso y una celda seleccionada que no
     * sea una de las celdas iniciales.
     */
    private boolean sePuedeEditarSeleccion() {
        return juegoActivo
                && filaSeleccionada >= 0
                && !fijas[filaSeleccionada][colSeleccionada];
    }

    // =====================================================================
    //  Deshacer y fin de la partida
    // =====================================================================

    /** Atiende el botón "Deshacer": revierte la última jugada del jugador. */
    @FXML
    private void handleDeshacer() {
        if (!juegoActivo || historial.isEmpty()) {
            return;
        }
        Jugada jugada = historial.pop();
        if (jugada.fila() == pistaFila && jugada.col() == pistaCol) {
            limpiarPista();
        }
        if (jugada.numeroAnterior() == 0) {
            tablero.borrar(jugada.fila(), jugada.col());
            celdas[jugada.fila()][jugada.col()].setText("");
        } else {
            tablero.set(jugada.fila(), jugada.col(), jugada.numeroAnterior());
            celdas[jugada.fila()][jugada.col()].setText(String.valueOf(jugada.numeroAnterior()));
        }
        seleccionarCelda(jugada.fila(), jugada.col());
        actualizarErrores();
    }

    /**
     * Atiende el botón "Validar": revisa todo el tablero y le informa al
     * jugador cuántas celdas faltan por llenar y cuántas tienen números repetidos.
     */
    @FXML
    private void handleValidar() {
        if (!juegoActivo) {
            return;
        }
        int vacias = 0;
        int conError = 0;
        for (int f = 0; f < Tablero.TAMANO; f++) {
            for (int c = 0; c < Tablero.TAMANO; c++) {
                if (tablero.estaVacia(f, c)) {
                    vacias++;
                }
                if (errores[f][c]) {
                    conError++;
                }
            }
        }

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Validar tablero");
        if (conError > 0) {
            alerta.setHeaderText("Hay números repetidos");
            alerta.setContentText("Tienes " + conError + " celdas en conflicto (en rojo). "
                    + "Faltan " + vacias + " celdas por llenar.");
        } else {
            alerta.setHeaderText("Vas bien");
            alerta.setContentText("No hay números repetidos. Faltan " + vacias + " celdas por llenar.");
        }
        alerta.showAndWait();
    }

    /**
     * Indica si el tablero está lleno y ningún número se repite.
     *
     * @return {@code true} si el jugador resolvió el Sudoku
     */
    private boolean tableroCompletoYValido() {
        for (int f = 0; f < Tablero.TAMANO; f++) {
            for (int c = 0; c < Tablero.TAMANO; c++) {
                if (tablero.estaVacia(f, c) || errores[f][c]) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Detiene el cronómetro, bloquea el tablero y felicita al jugador con su tiempo. */
    private void finalizarJuego() {
        detenerCronometro();
        juegoActivo = false;
        limpiarSeleccion();
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("¡Sudoku completado!");
        alerta.setHeaderText("¡Felicidades, resolviste el Sudoku!");
        alerta.setContentText("Tu tiempo fue " + lblTiempo.getText() + ".");
        alerta.showAndWait();
    }

    // =====================================================================
    //  Ayuda (pistas)
    // =====================================================================

    /**
     * Atiende el botón "Pista". Elige al azar una celda vacía y muestra, resaltado,
     * el número que le corresponde. La pista NO se escribe en el tablero (el
     * jugador debe escribirla él) y hay un máximo por partida, así que
     * nunca se puede completar el tablero usando solo la ayuda.
     */
    @FXML
    private void handlePista() {
        if (!juegoActivo) {
            return;
        }
        if (pistasRestantes <= 0) {
            return; // el botón ya aparece deshabilitado
        }
        List<int[]> vacias = new ArrayList<>();
        for (int f = 0; f < Tablero.TAMANO; f++) {
            for (int c = 0; c < Tablero.TAMANO; c++) {
                if (tablero.estaVacia(f, c)) {
                    vacias.add(new int[]{f, c});
                }
            }
        }
        if (vacias.isEmpty()) {
            return;
        }
        limpiarPista(); // quita la pista anterior, si había una
        int[] elegida = vacias.get(random.nextInt(vacias.size()));
        pistaFila = elegida[0];
        pistaCol = elegida[1];
        int numero = generador.getSolucion().get(pistaFila, pistaCol);

        celdas[pistaFila][pistaCol].setText(String.valueOf(numero));
        mensajePista = "Pista: en la fila " + (pistaFila + 1) + ", columna "
                + (pistaCol + 1) + " prueba con el " + numero + ".";
        pistasRestantes--;
        actualizarBotonPista();
        actualizarErrores();
    }

    /** Quita la pista visible: vacía la celda si el jugador no escribió nada en ella. */
    private void limpiarPista() {
        if (pistaFila >= 0 && tablero.estaVacia(pistaFila, pistaCol)) {
            celdas[pistaFila][pistaCol].setText("");
        }
        pistaFila = -1;
        pistaCol = -1;
        mensajePista = "";
    }

    /** Muestra en el botón cuántas pistas quedan. */
    private void actualizarBotonPista() {
        if (btnPista != null) {
            btnPista.setText("Pista (" + pistasRestantes + ")");
            btnPista.setDisable(pistasRestantes <= 0);
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
        limpiarSeleccion();
        limpiarPista();
        historial.clear();
        pistasRestantes = MAX_PISTAS;
        actualizarBotonPista();
        tablero = generador.generar();
        for (int f = 0; f < Tablero.TAMANO; f++) {
            for (int c = 0; c < Tablero.TAMANO; c++) {
                fijas[f][c] = !tablero.estaVacia(f, c);
            }
        }
        juegoActivo = true;
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
        limpiarSeleccion();
        limpiarPista();
        historial.clear();
        pistasRestantes = MAX_PISTAS;
        actualizarBotonPista();
        juegoActivo = false;
        tablero.limpiar();
        for (boolean[] fila : fijas) {
            java.util.Arrays.fill(fila, false);
        }
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
