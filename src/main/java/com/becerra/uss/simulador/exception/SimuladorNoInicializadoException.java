package com.becerra.uss.simulador.exception;

/** Se pidió un paso sin haber iniciado ningún simulador (HTTP 409). */
public class SimuladorNoInicializadoException extends RuntimeException {
    public SimuladorNoInicializadoException() {
        super("El simulador no ha sido inicializado. Llame primero a POST /api/simulador/iniciar.");
    }
}
