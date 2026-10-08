package com.becerra.uss.simulador.DTO;

/** Aciertos y fallos de las cachés L1 (solo en Harvard modificada). */
public record EstadisticasCacheDTO(int aciertosL1I, int fallosL1I, int aciertosL1D, int fallosL1D) {
}
