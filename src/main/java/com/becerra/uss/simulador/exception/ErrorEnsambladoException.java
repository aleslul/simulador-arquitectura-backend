package com.becerra.uss.simulador.exception;

/** Error de sintaxis en el programa ensamblador, con el número de línea (1 = primera línea; 0 = sin línea). */
public class ErrorEnsambladoException extends SolicitudInvalidaException {
    private final int linea;

    public ErrorEnsambladoException(int linea, String mensaje) {
        super("ERROR_ENSAMBLADO", linea > 0 ? "Línea " + linea + ": " + mensaje : mensaje);
        this.linea = linea;
    }

    public int getLinea() {
        return linea;
    }
}
