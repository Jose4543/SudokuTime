package org.example.sudoku.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Genera el tablero inicial de una partida. Primero construye un tablero
 * completo y válido (la solución) y luego deja visibles solo 2 números
 * por cada bloque de 2x3. Así el tablero inicial siempre es válido y
 * siempre tiene solución.
 */
public class GeneradorTablero {

    /** Cantidad de números visibles al inicio en cada bloque. */
    private static final int NUMEROS_POR_BLOQUE = 2;

    private final Validador validador;
    private final Random random = new Random();
    private Tablero solucion;

    /**
     * Crea un generador que usa el validador dado para respetar las reglas.
     *
     * @param validador validador de jugadas
     */
    public GeneradorTablero(Validador validador) {
        this.validador = validador;
    }

    /**
     * Genera un tablero inicial con 2 números por bloque de 2x3.
     *
     * @return el tablero inicial de la partida
     */
    public Tablero generar() {
        solucion = new Tablero();
        llenar(solucion, 0);

        Tablero inicial = new Tablero();
        for (int bf = 0; bf < Tablero.TAMANO; bf += Tablero.FILAS_BLOQUE) {
            for (int bc = 0; bc < Tablero.TAMANO; bc += Tablero.COLS_BLOQUE) {
                copiarNumerosDelBloque(inicial, bf, bc);
            }
        }
        return inicial;
    }

    /**
     * Devuelve la solución completa del último tablero generado.
     * Sirve para que la ayuda sugiera números correctos.
     *
     * @return tablero resuelto, o {@code null} si aún no se ha generado ninguno
     */
    public Tablero getSolucion() {
        return solucion;
    }

    /** Copia 2 celdas al azar de un bloque desde la solución al tablero inicial. */
    private void copiarNumerosDelBloque(Tablero inicial, int filaInicio, int colInicio) {
        List<int[]> celdas = new ArrayList<>();
        for (int f = filaInicio; f < filaInicio + Tablero.FILAS_BLOQUE; f++) {
            for (int c = colInicio; c < colInicio + Tablero.COLS_BLOQUE; c++) {
                celdas.add(new int[]{f, c});
            }
        }
        Collections.shuffle(celdas, random);
        for (int i = 0; i < NUMEROS_POR_BLOQUE; i++) {
            int f = celdas.get(i)[0];
            int c = celdas.get(i)[1];
            inicial.set(f, c, solucion.get(f, c));
        }
    }

    /**
     * Llena el tablero celda por celda con backtracking, probando los
     * números en orden aleatorio para que cada partida sea distinta.
     *
     * @param t   tablero a llenar
     * @param pos posición actual (0-35), recorriendo fila por fila
     * @return {@code true} si logró llenar todo el tablero
     */
    private boolean llenar(Tablero t, int pos) {
        if (pos == Tablero.TAMANO * Tablero.TAMANO) {
            return true;
        }
        int fila = pos / Tablero.TAMANO;
        int col = pos % Tablero.TAMANO;

        List<Integer> numeros = new ArrayList<>();
        for (int n = 1; n <= Tablero.TAMANO; n++) {
            numeros.add(n);
        }
        Collections.shuffle(numeros, random);

        for (int n : numeros) {
            if (validador.esValido(t, fila, col, n)) {
                t.set(fila, col, n);
                if (llenar(t, pos + 1)) {
                    return true;
                }
                t.borrar(fila, col); // retroceder
            }
        }
        return false;
    }
}
