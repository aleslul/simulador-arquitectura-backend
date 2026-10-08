package com.becerra.uss.simulador.DTO;

import java.util.List;

/**
 * Estado completo de la máquina tras un paso (los campos originales conservan su nombre).
 * <ul>
 *   <li>memoriaPrincipal: Von Neumann y Harvard modificada (null en Harvard)</li>
 *   <li>memoriaInstrucciones / memoriaDatos: solo Harvard pura (null en las demás)</li>
 *   <li>desensamblado: texto de las primeras {@code longitudPrograma} palabras de la memoria que contiene el código;
 *       el resto de posiciones es null</li>
 *   <li>ciclosReloj: ciclo en que terminó la última instrucción ejecutada; ciclosPaso: ciclos que sumó este paso</li>
 *   <li>cpi: ciclos por instrucción acumulados; ciclosEsperaRecurso: ciclos perdidos esperando un bus/RAM ocupado</li>
 *   <li>error: null si todo va bien; si la máquina sufrió un fallo (dirección inválida, opcode inválido...)
 *       trae "CODIGO: mensaje" y finalizado = true</li>
 * </ul>
 */
public record EstadoSimulacionDTO(
        String arquitectura,
        String modo,
        int pc,
        String ir,
        int acumulador,
        int mar,
        int mdr,
        FlagsDTO flags,
        int ciclosReloj,
        int ciclosPaso,
        int instruccionesEjecutadas,
        double cpi,
        int ciclosEsperaRecurso,
        boolean finalizado,
        String error,
        int longitudPrograma,
        int[] memoriaPrincipal,
        int[] memoriaInstrucciones,
        int[] memoriaDatos,
        String[] desensamblado,
        String logOperacion,
        List<EventoFaseDTO> eventos,
        EstadisticasCacheDTO cache) {

    /** Copia del estado con otro texto de log. */
    public EstadoSimulacionDTO conLog(String nuevoLog) {
        return new EstadoSimulacionDTO(arquitectura, modo, pc, ir, acumulador, mar, mdr, flags, ciclosReloj,
                ciclosPaso, instruccionesEjecutadas, cpi, ciclosEsperaRecurso, finalizado, error, longitudPrograma,
                memoriaPrincipal, memoriaInstrucciones, memoriaDatos, desensamblado, nuevoLog, eventos, cache);
    }
}
