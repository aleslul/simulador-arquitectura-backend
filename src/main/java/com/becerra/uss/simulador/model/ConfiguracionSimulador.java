package com.becerra.uss.simulador.model;

import com.becerra.uss.simulador.exception.SolicitudInvalidaException;

/**
 * Parámetros de hardware de una simulación. Todos los tiempos están en ciclos de reloj.
 *
 * @param latenciaMemoria          ciclos de un acceso a la memoria principal/bus (RAM)
 * @param tiempoDecodificacion     ciclos de decodificación en la unidad de control
 * @param tiempoAlu                ciclos de la ALU (ADD, SUB, CMP)
 * @param tiempoSalto              ciclos para evaluar/ejecutar un salto
 * @param anchoDatos               bits de la palabra de DATOS en Harvard pura (Von Neumann y Harvard modificada usan 16,
 *                                 porque instrucciones y datos comparten la misma memoria)
 * @param tamMemoriaInstrucciones  palabras de la memoria de instrucciones (Harvard)
 * @param tamMemoriaDatos          palabras de la memoria de datos (Harvard)
 * @param tamMemoriaUnificada      palabras de la memoria única (Von Neumann y Harvard modificada)
 * @param lineasCache              líneas de cada caché L1 (Harvard modificada)
 * @param latenciaCache            ciclos de un acierto en L1; un fallo cuesta latenciaMemoria + latenciaCache
 */
public record ConfiguracionSimulador(
        TipoArquitectura tipo,
        ModoEjecucion modo,
        int latenciaMemoria,
        int tiempoDecodificacion,
        int tiempoAlu,
        int tiempoSalto,
        int anchoDatos,
        int tamMemoriaInstrucciones,
        int tamMemoriaDatos,
        int tamMemoriaUnificada,
        int lineasCache,
        int latenciaCache) {

    public ConfiguracionSimulador {
        if (tipo == null || modo == null) {
            throw new SolicitudInvalidaException("CONFIGURACION_INVALIDA", "Faltan el tipo o el modo de ejecución.");
        }
        rango("latenciaMemoria", latenciaMemoria, 1, 100);
        rango("tiempoDecodificacion", tiempoDecodificacion, 0, 100);
        rango("tiempoAlu", tiempoAlu, 0, 100);
        rango("tiempoSalto", tiempoSalto, 0, 100);
        rango("anchoDatos", anchoDatos, 8, 32);
        rango("tamMemoriaInstrucciones", tamMemoriaInstrucciones, 4, Instruccion.MAX_OPERANDO + 1);
        rango("tamMemoriaDatos", tamMemoriaDatos, 4, Instruccion.MAX_OPERANDO + 1);
        rango("tamMemoriaUnificada", tamMemoriaUnificada, 4, Instruccion.MAX_OPERANDO + 1);
        rango("lineasCache", lineasCache, 1, 1024);
        rango("latenciaCache", latenciaCache, 1, 100);
    }

    public static ConfiguracionSimulador porDefecto(TipoArquitectura tipo, ModoEjecucion modo) {
        return new ConfiguracionSimulador(tipo, modo, 3, 1, 1, 1, 16, 16, 16, 32, 8, 1);
    }

    /** Misma configuración de hardware pero con otra arquitectura/modo (para comparar). */
    public ConfiguracionSimulador con(TipoArquitectura nuevoTipo, ModoEjecucion nuevoModo) {
        return new ConfiguracionSimulador(nuevoTipo, nuevoModo, latenciaMemoria, tiempoDecodificacion, tiempoAlu,
                tiempoSalto, anchoDatos, tamMemoriaInstrucciones, tamMemoriaDatos, tamMemoriaUnificada,
                lineasCache, latenciaCache);
    }

    /** Ancho (bits) con el que opera la ALU y el acumulador. */
    public int anchoAlu() {
        return tipo == TipoArquitectura.HARVARD ? anchoDatos : Instruccion.BITS_PALABRA;
    }

    private static void rango(String nombre, int valor, int min, int max) {
        if (valor < min || valor > max) {
            throw new SolicitudInvalidaException("CONFIGURACION_INVALIDA",
                    "El parámetro '" + nombre + "' debe estar entre " + min + " y " + max + " (recibido: " + valor + ").");
        }
    }
}
