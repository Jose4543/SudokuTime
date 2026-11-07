package org.example.sudoku.model;

/**
 * Modelo del tablero de Sudoku 6x6. Guarda los números en una matriz
 * donde 0 significa "celda vacía". No sabe nada de la interfaz gráfica.
 */
public class Tablero {

    /** Tamaño del tablero (filas y columnas). */
    public static final int TAMANO = 6;
    /** Filas que tiene cada bloque. */
    public static final int FILAS_BLOQUE = 2;
    /** Columnas que tiene cada bloque. */
    public static final int COLS_BLOQUE = 3;

    private final int[][] celdas = new int[TAMANO][TAMANO];

    /**
     * Obtiene el número de una celda.
     *
     * @param fila fila (0-5)
     * @param col  columna (0-5)
     * @return el número (1-6) o 0 si está vacía
     */
    public int get(int fila, int col) {
        return celdas[fila][col];
    }

    /**
     * Coloca un número en una celda. No valida reglas del Sudoku,
     * solo que la posición y el valor estén en rango.
     *
     * @param fila   fila (0-5)
     * @param col    columna (0-5)
     * @param numero número (1-6)
     * @throws IllegalArgumentException si algún valor está fuera de rango
     */
    public void set(int fila, int col, int numero) {
        verificarPosicion(fila, col);
        if (numero < 1 || numero > TAMANO) {
            throw new IllegalArgumentException("Número fuera de rango: " + numero);
        }
        celdas[fila][col] = numero;
    }

    /**
     * Vacía una celda.
     *
     * @param fila fila (0-5)
     * @param col  columna (0-5)
     */
    public void borrar(int fila, int col) {
        verificarPosicion(fila, col);
        celdas[fila][col] = 0;
    }

    /**
     * Indica si una celda está vacía.
     *
     * @param fila fila (0-5)
     * @param col  columna (0-5)
     * @return {@code true} si no tiene número
     */
    public boolean estaVacia(int fila, int col) {
        return celdas[fila][col] == 0;
    }

    /** Deja todas las celdas vacías. */
    public void limpiar() {
        for (int[] fila : celdas) {
            java.util.Arrays.fill(fila, 0);
        }
    }

    private void verificarPosicion(int fila, int col) {
        if (fila < 0 || fila >= TAMANO || col < 0 || col >= TAMANO) {
            throw new IllegalArgumentException("Posición fuera de rango: " + fila + "," + col);
        }
    }
}
