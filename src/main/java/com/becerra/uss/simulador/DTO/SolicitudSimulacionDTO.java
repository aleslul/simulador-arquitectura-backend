package com.becerra.uss.simulador.DTO;

// recibe los parametros de configuracion enviados desde el cliente para iniciar o comparar
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
        Integer lineasCache,
        Integer latenciaCache,
        Integer lineasCacheL2,
        Integer latenciaCacheL2,
        String algoritmoReemplazo) {
}