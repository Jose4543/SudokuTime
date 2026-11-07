package org.example.sudoku.model;

/**
 * Registro de un cambio hecho por el jugador, necesario para poder deshacerlo.
 * Guarda en qué celda fue el cambio y qué número tenía antes.
 */
public class Jugada {

    private final int fila;
    private final int col;
    private final int numeroAnterior;

    /**
     * Crea el registro de una jugada.
     *
     * @param fila           fila de la celda modificada (0-5)
     * @param col            columna de la celda modificada (0-5)
     * @param numeroAnterior número que tenía la celda antes del cambio (0 si estaba vacía)
     */
    public Jugada(int fila, int col, int numeroAnterior) {
        this.fila = fila;
        this.col = col;
        this.numeroAnterior = numeroAnterior;
    }

    /**
     * Fila de la celda modificada.
     *
     * @return fila (0-5)
     */
    public int fila() {
        return fila;
    }

    /**
     * Columna de la celda modificada.
     *
     * @return columna (0-5)
     */
    public int col() {
        return col;
    }

    /**
     * Número que tenía la celda antes del cambio.
     *
     * @return el número (1-6), o 0 si la celda estaba vacía
     */
    public int numeroAnterior() {
        return numeroAnterior;
    }
}
