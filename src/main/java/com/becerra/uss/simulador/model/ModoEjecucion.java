package com.becerra.uss.simulador.model;

import com.becerra.uss.simulador.exception.SolicitudInvalidaException;
import java.util.Locale;

/**
 * DEFINE Q MODO SE VA A EJECUTAR Y YA
 */
public enum ModoEjecucion {
    SECUENCIAL,
    SEGMENTADO;

    /** SEGMENTADO POR DEFECTO */
    public static ModoEjecucion desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return SEGMENTADO;
        }
        String normalizado = texto.trim().toUpperCase(Locale.ROOT);
        for (ModoEjecucion modo : values()) {
            if (modo.name().equals(normalizado)) {
                return modo;
            }
        }
        throw new SolicitudInvalidaException("MODO_INVALIDO",
                "Modo de ejecución desconocido: '" + texto + "'. Valores válidos: SECUENCIAL, SEGMENTADO.");
    }
}
