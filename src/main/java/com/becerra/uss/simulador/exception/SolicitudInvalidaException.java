package com.becerra.uss.simulador.exception;

/** Parámetros o programa inválidos enviados por el cliente (se traduce a HTTP 400). */
public class SolicitudInvalidaException extends RuntimeException {
    private final String codigo;

    public SolicitudInvalidaException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
