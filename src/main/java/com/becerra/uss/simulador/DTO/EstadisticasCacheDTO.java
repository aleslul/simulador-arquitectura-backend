package com.becerra.uss.simulador.DTO;

import java.util.List;

public record EstadisticasCacheDTO(
        int aciertosL1I,
        int fallosL1I,
        int aciertosL1D,
        int fallosL1D,
        List<Integer> estadoL1I,
        List<Integer> estadoL1D) {
}