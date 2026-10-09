package com.becerra.uss.simulador.model;

import com.becerra.uss.simulador.exception.SolicitudInvalidaException;
import java.util.Locale;

/**
 * Cómo se solapan las instrucciones en el tiempo.
 *   SECUENCIAL: una instrucción termina por completo antes de empezar el fetch de la siguiente.
 *       En este modo Harvard NO es más rápida que Von Neumann (el dato se necesita después del fetch).
 *   SEGMENTADO: 2 etapas (Fetch || Decode/Execute). El fetch de la instrucción i+1 se solapa con la
 *       ejecución de la i. Aquí aparece la ventaja de Harvard: con un único bus (Von Neumann) el fetch
 *       debe esperar a que el bus quede libre de accesos a datos (riesgo estructural).
 */
public enum ModoEjecucion {
    SECUENCIAL,
    SEGMENTADO;

    /** Si no se indica, se usa SEGMENTADO. */
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
