package com.becerra.uss.simulador.DTO;

/** Cuerpo estándar de las respuestas de error (la línea solo se informa en errores de ensamblado). */
public record ErrorDTO(String codigo, String mensaje, Integer linea) {
}
