package com.becerra.uss.simulador.DTO;

/**
 * Una fila de la comparación: el mismo programa ejecutado hasta el final en una arquitectura y modo.
 *
 * @param aceleracionVsVonNeumann ciclos de Von Neumann (mismo modo) / ciclos de esta fila; null si no se pudo calcular
 * @param completado              false si hubo un fallo, el programa no cargó o se alcanzó el límite de pasos
 */
public record ResultadoComparacionDTO(
        String arquitectura,
        String modo,
        int ciclosTotales,
        int instrucciones,
        double cpi,
        int ciclosEsperaRecurso,
        Double aceleracionVsVonNeumann,
        boolean completado,
        String error,
        EstadisticasCacheDTO cache) {
}
