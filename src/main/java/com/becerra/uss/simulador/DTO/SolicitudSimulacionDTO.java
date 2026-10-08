package com.becerra.uss.simulador.DTO;

/**
 * Solicitud para crear una simulación. Todo es opcional salvo {@code tipo} (excepto en /comparar, que lo ignora).
 *
 * @param tipo                  VON_NEUMANN, HARVARD o HARVARD_MODIFICADA
 * @param modo                  SECUENCIAL o SEGMENTADO (por defecto SEGMENTADO)
 * @param valorA                valor del dato A del programa por defecto (por defecto 5)
 * @param valorB                valor del dato B del programa por defecto (por defecto 7)
 * @param programa              código ensamblador propio; si es null se usa la suma C = A + B
 * @param latenciaMemoria       ciclos de acceso a RAM/bus (por defecto 3)
 * @param tiempoDecodificacion  ciclos de decodificación (por defecto 1)
 * @param tiempoAlu             ciclos de la ALU (por defecto 1)
 * @param anchoDatos            bits de la palabra de datos en Harvard pura (por defecto 16)
 * @param lineasCache           líneas de cada caché L1 en Harvard modificada (por defecto 8)
 */
public record SolicitudSimulacionDTO(
        String tipo,
        String modo,
        Integer valorA,
        Integer valorB,
        String programa,
        Integer latenciaMemoria,
        Integer tiempoDecodificacion,
        Integer tiempoAlu,
        Integer anchoDatos,
        Integer lineasCache) {
}
