package com.becerra.uss.simulador.model;

import com.becerra.uss.simulador.exception.SolicitudInvalidaException;
import java.util.Locale;

// almacena y valida todos los parametros de hardware y tiempos de reloj de la simulacion
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
        int latenciaCache,
        int lineasCacheL2,
        int latenciaCacheL2,
        String algoritmoReemplazo) {

    // valida que todos los parametros esten dentro de los rangos permitidos
    public ConfiguracionSimulador {
        if (tipo == null || modo == null) {
            throw new SolicitudInvalidaException("CONFIGURACION_INVALIDA", "Faltan el tipo o el modo de ejecucion");
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
        rango("lineasCacheL2", lineasCacheL2, 1, 2048);
        rango("latenciaCacheL2", latenciaCacheL2, 1, 100);

        // normaliza el nombre del algoritmo de reemplazo a mayusculas y asigna LRU si viene vacio
        algoritmoReemplazo = (algoritmoReemplazo == null || algoritmoReemplazo.isBlank())
                ? "LRU"
                : algoritmoReemplazo.trim().toUpperCase(Locale.ROOT);
        if (!algoritmoReemplazo.equals("LRU") && !algoritmoReemplazo.equals("FIFO")) {
            throw new SolicitudInvalidaException("CONFIGURACION_INVALIDA",
                    "El algoritmo de reemplazo debe ser LRU o FIFO (recibido: " + algoritmoReemplazo + ")");
        }
    }

    // crea una configuracion base con 32 celdas de memoria L1 de 4 lineas y L2 de 16 lineas
    public static ConfiguracionSimulador porDefecto(TipoArquitectura tipo, ModoEjecucion modo) {
        return new ConfiguracionSimulador(tipo, modo, 6, 1, 1, 1, 16, 32, 32, 32, 4, 1, 16, 2, "LRU");
    }

    // clona la configuracion actual cambiando solo la arquitectura y el modo de ejecucion
    public ConfiguracionSimulador con(TipoArquitectura nuevoTipo, ModoEjecucion nuevoModo) {
        return new ConfiguracionSimulador(nuevoTipo, nuevoModo, latenciaMemoria, tiempoDecodificacion, tiempoAlu,
                tiempoSalto, anchoDatos, tamMemoriaInstrucciones, tamMemoriaDatos, tamMemoriaUnificada,
                lineasCache, latenciaCache, lineasCacheL2, latenciaCacheL2, algoritmoReemplazo);
    }

    // devuelve el ancho en bits que usara la ALU segun la arquitectura elegida
    public int anchoAlu() {
        return tipo == TipoArquitectura.HARVARD ? anchoDatos : Instruccion.BITS_PALABRA;
    }

    // verifica que un parametro numerico no salga de su minimo y maximo
    private static void rango(String nombre, int valor, int min, int max) {
        if (valor < min || valor > max) {
            throw new SolicitudInvalidaException("CONFIGURACION_INVALIDA",
                    "El parametro '" + nombre + "' debe estar entre " + min + " y " + max + " (recibido: " + valor + ")");
        }
    }
}