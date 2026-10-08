package com.becerra.uss.simulador.DTO;

/**
 * Una fase dentro del paso (para dibujar un diagrama de tiempos). En modo SEGMENTADO los eventos de un paso pueden
 * solaparse en el tiempo con los del paso anterior (el fetch de la instrucción i+1 ocurre mientras se ejecuta la i).
 *
 * @param fase        FETCH, DECODE, MEM_LECTURA, MEM_ESCRITURA, ALU o SALTO
 * @param recurso     recurso utilizado (BUS_UNICO, BUS_INSTRUCCIONES, BUS_DATOS, L1I, L1D, UNIDAD_CONTROL, ...)
 * @param cicloInicio ciclo de inicio (inclusive)
 * @param cicloFin    ciclo de fin (exclusive): duración = cicloFin - cicloInicio
 * @param direccion   valor del MAR en esa fase (null si no aplica)
 * @param dato        valor del MDR en esa fase (null si no aplica)
 */
public record EventoFaseDTO(
        String fase,
        String recurso,
        int cicloInicio,
        int cicloFin,
        Integer direccion,
        Integer dato,
        String detalle) {
}
