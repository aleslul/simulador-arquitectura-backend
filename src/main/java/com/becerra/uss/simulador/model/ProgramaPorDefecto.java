package com.becerra.uss.simulador.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** PROFGRAMAS POR DEFECTO YA ENSAMBLADOS PARA Q NO SUFRA */
public final class ProgramaPorDefecto {

    private ProgramaPorDefecto() {
    }

    /** En Harvard pura los datos viven en su propia memoria (desde 0); en las unificadas van después del código. */
    private static int baseDatos(TipoArquitectura tipo) {
        return tipo == TipoArquitectura.HARVARD ? 0 : 10;
    }

    /** PROGRAMA ORIGINAL (C = A + B) */
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

    /** MULTIPLICACION POR SUMAS REPETIDAS QUE NO SE USA PORQUE ES DE HARVARD MODIFICADO Y HRARVARD MODIFICADO NO FUNCIONA PORQ ME DA PEREZA ARREGLARLO */
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
     * ESTE ES UN EJEMPLO DE LA SOBREESCRITURA QUE SE PUEDE HACER EN NEUMANN PORQ LAS INSTRUCCIONES Y LOS DATOS VIVEN
     * EN LA MISMA MEMORIA
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
