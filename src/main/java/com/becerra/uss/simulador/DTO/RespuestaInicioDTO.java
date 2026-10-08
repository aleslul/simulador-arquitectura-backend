package com.becerra.uss.simulador.DTO;

/** Respuesta al crear una simulación: conserve el sesionId y envíelo en las siguientes llamadas. */
public record RespuestaInicioDTO(String sesionId, String mensaje, String tipo, String modo, String programa) {
}
