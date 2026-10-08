package com.becerra.uss.simulador.model;

/**
 * Recurso de hardware que solo puede atender un acceso a la vez (bus, puerto de caché, RAM).
 * Los accesos se reservan en orden de programa, así que basta con recordar desde qué ciclo queda libre.
 */
public class Recurso {
    private final String nombre;
    private int libreDesde;

    public Recurso(String nombre) {
        this.nombre = nombre;
    }

    /** Primer ciclo >= solicitud en el que el recurso está libre. */
    public int inicioDisponible(int solicitud) {
        return Math.max(solicitud, libreDesde);
    }

    public void ocupar(int fin) {
        this.libreDesde = Math.max(this.libreDesde, fin);
    }

    public String getNombre() {
        return nombre;
    }
}
