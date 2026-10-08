package com.becerra.uss.simulador.exception;

/** El identificador de sesión no existe o ya expiró (HTTP 404). */
public class SesionNoEncontradaException extends RuntimeException {
    public SesionNoEncontradaException(String sesionId) {
        super("No existe la sesión '" + sesionId + "' (puede haber expirado). Inicie una nueva simulación.");
    }
}
