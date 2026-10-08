package com.becerra.uss.simulador.exception;

/**
 * Fallo producido DENTRO de la máquina simulada (equivale a una "excepción" o "trap" de hardware):
 * dirección fuera de rango, opcode inválido, PC fuera de la memoria de instrucciones, etc.
 * <p>
 * No es un error del servidor: la arquitectura lo captura, detiene la máquina y lo informa
 * en el estado devuelto al frontend.
 */
public class FalloEjecucionException extends RuntimeException {
    private final String codigo;

    public FalloEjecucionException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
