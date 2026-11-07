package org.example.sudoku.model;

/**
 * Validador con las reglas del Sudoku 6x6: el número no puede repetirse
 * en la misma fila, columna ni bloque de 2x3.
 */
public class ValidadorSudoku implements Validador {

    @Override
    public boolean esValido(Tablero tablero, int fila, int col, int numero) {
        return !repetidoEnFila(tablero, fila, col, numero)
                && !repetidoEnColumna(tablero, fila, col, numero)
                && !repetidoEnBloque(tablero, fila, col, numero);
    }

    private boolean repetidoEnFila(Tablero t, int fila, int col, int numero) {
        for (int c = 0; c < Tablero.TAMANO; c++) {
            if (c != col && t.get(fila, c) == numero) {
                return true;
            }
        }
        return false;
    }

    private boolean repetidoEnColumna(Tablero t, int fila, int col, int numero) {
        for (int f = 0; f < Tablero.TAMANO; f++) {
            if (f != fila && t.get(f, col) == numero) {
                return true;
            }
        }
        return false;
    }

    private boolean repetidoEnBloque(Tablero t, int fila, int col, int numero) {
        int inicioFila = (fila / Tablero.FILAS_BLOQUE) * Tablero.FILAS_BLOQUE;
        int inicioCol = (col / Tablero.COLS_BLOQUE) * Tablero.COLS_BLOQUE;
        for (int f = inicioFila; f < inicioFila + Tablero.FILAS_BLOQUE; f++) {
            for (int c = inicioCol; c < inicioCol + Tablero.COLS_BLOQUE; c++) {
                if ((f != fila || c != col) && t.get(f, c) == numero) {
                    return true;
                }
            }
        }
        return false;
    }
}
