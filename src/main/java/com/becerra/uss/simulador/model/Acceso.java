package com.becerra.uss.simulador.model;

/**
 * Resultado de temporizar un acceso a memoria.
 *
 * @param inicio  ciclo en que realmente empieza (puede ser posterior a la solicitud si el recurso estaba ocupado)
 * @param fin     ciclo en que termina
 * @param recurso recurso utilizado (BUS_UNICO, BUS_INSTRUCCIONES, BUS_DATOS, L1I, L1D, ...)
 * @param acierto true/false si pasó por una caché; null si no aplica
 */
public record Acceso(int inicio, int fin, String recurso, Boolean acierto) {
}
