package org.example.sudoku.model;

/**
 * Contrato para validar jugadas en un tablero de Sudoku.
 * Permite cambiar las reglas sin tocar el resto del programa.
 */
public interface Validador {

    /**
     * Indica si se puede colocar un número en una celda sin repetirlo
     * en su fila, columna o bloque.
     *
     * @param tablero tablero sobre el que se valida
     * @param fila    fila de la celda (0-5)
     * @param col     columna de la celda (0-5)
     * @param numero  número a colocar (1-6)
     * @return {@code true} si la jugada respeta las reglas
     */
    boolean esValido(Tablero tablero, int fila, int col, int numero);
}
