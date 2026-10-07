package com.becerra.uss.simulador.model;

public class Memoria {
    private int[] celdas;

    public Memoria(int tamano) {
        this.celdas = new int[tamano];
    }

    public int leer(int direccion) {
        return celdas[direccion];
    }

    public void escribir(int direccion, int dato) {
        celdas[direccion] = dato;
    }

    public int[] getEstadoCeldas() {
        return celdas;
    }
}
