package com.becerra.uss.simulador.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Caché L1 totalmente asociativa con soporte para reemplazo LRU y FIFO. */
public class Cache {
    private final String nombre;
    private final int capacidad;
    private final String algoritmo;
    private final boolean esLru;
    private final LinkedHashMap<Integer, Boolean> lineas;
    private int aciertos;
    private int fallos;

    public Cache(String nombre, int capacidad) {
        this(nombre, capacidad, "LRU");
    }

    public Cache(String nombre, int capacidad, String algoritmo) {
        if (capacidad < 1) {
            throw new IllegalArgumentException("La capacidad de la caché debe ser >= 1");
        }
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.algoritmo = (algoritmo == null || algoritmo.isBlank()) ? "LRU" : algoritmo.trim().toUpperCase();
        this.esLru = !"FIFO".equals(this.algoritmo);
        this.lineas = new LinkedHashMap<>(16, 0.75f, this.esLru) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Boolean> eldest) {
                return size() > Cache.this.capacidad;
            }
        };
    }

    /** Consulta la dirección, actualiza orden (si es LRU) y contadores. Devuelve true si es acierto. */
    public boolean acceder(int direccion) {
        boolean acierto = lineas.containsKey(direccion);
        if (acierto) {
            if (esLru) {
                lineas.get(direccion); // En LRU mueve la dirección al final (más reciente)
            }
            aciertos++;
        } else {
            fallos++;
        }
        return acierto;
    }

    public void insertar(int direccion) {
        if (!esLru && lineas.containsKey(direccion)) {
            return; // En FIFO, si el bloque ya está en caché, conserva su posición original de llegada
        }
        lineas.put(direccion, Boolean.TRUE);
    }

    public void invalidar(int direccion) {
        lineas.remove(direccion);
    }

    public String getNombre() {
        return nombre;
    }

    public String getAlgoritmo() {
        return algoritmo;
    }

    public int getAciertos() {
        return aciertos;
    }

    public int getFallos() {
        return fallos;
    }

    public List<Integer> getEstadoLineas() {
        return new ArrayList<>(lineas.keySet());
    }
}