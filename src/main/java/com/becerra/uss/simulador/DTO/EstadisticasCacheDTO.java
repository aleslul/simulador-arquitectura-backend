package com.becerra.uss.simulador.DTO;

import java.util.List;

// transporta los aciertos fallos y el estado de las lineas de L1I L1D y L2 hacia el frontend
public record EstadisticasCacheDTO(
        int aciertosL1I,
        int fallosL1I,
        int aciertosL1D,
        int fallosL1D,
        int aciertosL2,
        int fallosL2,
        List<Integer> estadoL1I,
        List<Integer> estadoL1D,
        List<Integer> estadoL2) {
}