package com.becerra.uss.simulador.model;

import java.util.Map;

/**
 * Resultado de ensamblar un programa.
 *
 * @param palabras instrucciones codificadas (16 bits), se cargan desde la dirección 0
 * @param datos    valores iniciales de datos: dirección -> valor (directiva .DATO)
 */
public record ProgramaEnsamblado(int[] palabras, Map<Integer, Integer> datos) {
}
