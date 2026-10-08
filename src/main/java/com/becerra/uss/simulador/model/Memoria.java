package com.becerra.uss.simulador.model;

import com.becerra.uss.simulador.exception.FalloEjecucionException;

/** Memoria direccionable por palabra, con ancho de palabra configurable y validación de direcciones. */
public class Memoria {
    private final String nombre;
    private final int[] celdas;
    private final int bits;

    public Memoria(String nombre, int tamano, int bits) {
        if (tamano < 1) {
            throw new IllegalArgumentException("El tamaño de memoria debe ser >= 1");
        }
        if (bits < 1 || bits > 32) {
            throw new IllegalArgumentException("El ancho de palabra debe estar entre 1 y 32 bits");
        }
        this.nombre = nombre;
        this.celdas = new int[tamano];
        this.bits = bits;
    }

    public int leer(int direccion) {
        validar(direccion);
        return celdas[direccion];
    }

    public void escribir(int direccion, int dato) {
        validar(direccion);
        celdas[direccion] = Alu.normalizar(dato, bits);
    }

    /** Copia del contenido (no expone el arreglo interno). */
    public int[] getEstadoCeldas() {
        return celdas.clone();
    }

    public int getTamano() { return celdas.length; }
    public int getBits() { return bits; }
    public String getNombre() { return nombre; }

    private void validar(int direccion) {
        if (direccion < 0 || direccion >= celdas.length) {
            throw new FalloEjecucionException("DIRECCION_FUERA_DE_RANGO",
                    "Dirección " + direccion + " fuera de rango en la " + nombre
                            + " (válido: 0-" + (celdas.length - 1) + ").");
        }
    }
}
