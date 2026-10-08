package com.becerra.uss.simulador.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Caché totalmente asociativa con reemplazo LRU y líneas de una palabra.
 * Solo guarda las direcciones presentes (los valores se leen de la memoria; con política
 * write-through la caché nunca está "sucia", así que no hace falta duplicar datos).
 */
public class Cache {
    private final String nombre;
    private final int capacidad;
    private final LinkedHashMap<Integer, Boolean> lineas;
    private int aciertos;
    private int fallos;

    public Cache(String nombre, int capacidad) {
        if (capacidad < 1) {
            throw new IllegalArgumentException("La capacidad de la caché debe ser >= 1");
        }
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.lineas = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Boolean> eldest) {
                return size() > Cache.this.capacidad;
            }
        };
    }

    /** Consulta la dirección, actualiza LRU y contadores. Devuelve true si es acierto. */
    public boolean acceder(int direccion) {
        boolean acierto = lineas.containsKey(direccion);
        if (acierto) {
            lineas.get(direccion); // marca como usada recientemente
            aciertos++;
        } else {
            fallos++;
        }
        return acierto;
    }

    public void insertar(int direccion) {
        lineas.put(direccion, Boolean.TRUE);
    }

    public void invalidar(int direccion) {
        lineas.remove(direccion);
    }

    public String getNombre() { return nombre; }
    public int getAciertos() { return aciertos; }
    public int getFallos() { return fallos; }
}
