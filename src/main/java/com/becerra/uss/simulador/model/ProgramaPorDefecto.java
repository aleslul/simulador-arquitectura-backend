package com.becerra.uss.simulador.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** Programas de ejemplo en ensamblador. Las direcciones de datos dependen de la arquitectura. */
public final class ProgramaPorDefecto {

    private ProgramaPorDefecto() {
    }

    /** En Harvard pura los datos viven en su propia memoria (desde 0); en las unificadas van después del código. */
    private static int baseDatos(TipoArquitectura tipo) {
        return tipo == TipoArquitectura.HARVARD ? 0 : 10;
    }

    /** C = A + B (el programa original del simulador). */
    public static String suma(TipoArquitectura tipo, int a, int b) {
        int d = baseDatos(tipo);
        return String.join("\n",
                "; C = A + B",
                "LOAD " + d + "        ; ACC = A",
                "ADD " + (d + 1) + "         ; ACC = A + B",
                "STORE " + (d + 2) + "       ; C = ACC",
                "HALT",
                ".DATO " + d + " " + a,
                ".DATO " + (d + 1) + " " + b,
                "");
    }

    /** RES = A * B por sumas repetidas: ejercita saltos, flags y reutilización (aprovecha las cachés). */
    public static String multiplicacion(TipoArquitectura tipo, int a, int b) {
        int d = baseDatos(tipo);
        int contador = d, factor = d + 1, uno = d + 2, resultado = d + 3;
        return String.join("\n",
                "; RES = A * B mediante sumas repetidas",
                "INICIO: LOAD " + contador + "    ; ACC = contador (A)",
                "        JZ FIN        ; si el contador llegó a 0, terminar",
                "        SUB " + uno + "       ; contador - 1",
                "        STORE " + contador + "     ; guardar contador",
                "        LOAD " + resultado + "     ; ACC = RES",
                "        ADD " + factor + "       ; RES + B",
                "        STORE " + resultado + "    ; guardar RES",
                "        JMP INICIO",
                "FIN:    HALT",
                ".DATO " + contador + " " + a,
                ".DATO " + factor + " " + b,
                ".DATO " + uno + " 1",
                "");
    }

    /**
     * Intenta sobrescribir la instrucción de la dirección 3 con HALT mediante STORE.
     * En Von Neumann (memoria unificada) funciona: el programa termina antes. En Harvard, STORE escribe en la
     * memoria de DATOS y el código queda intacto, así que el segundo ADD sí se ejecuta.
     */
    public static String autoModificable() {
        return String.join("\n",
                "; Demostración de código automodificable",
                "LOAD 10        ; ACC = 0 (la palabra de la instrucción HALT)",
                "STORE 3        ; intenta sobrescribir la instrucción de la dirección 3",
                "ADD 11         ; ACC += 5",
                "ADD 11         ; ¿se ejecuta? En Von Neumann ya fue reemplazada por HALT",
                "HALT",
                ".DATO 10 0",
                ".DATO 11 5",
                "");
    }

    public static String para(TipoArquitectura tipo, int a, int b) {
        return suma(tipo, a, b);
    }

    public static Map<String, String> ejemplos(TipoArquitectura tipo, int a, int b) {
        Map<String, String> mapa = new LinkedHashMap<>();
        mapa.put("SUMA", suma(tipo, a, b));
        mapa.put("MULTIPLICACION", multiplicacion(tipo, a, b));
        mapa.put("AUTOMODIFICABLE", autoModificable());
        return mapa;
    }
}
