package com.becerra.uss.simulador.model;

import java.util.LinkedHashMap;
import java.util.Map;

// provee programas en lenguaje ensamblador predefinidos para probar localidad y rendimiento
public final class ProgramaPorDefecto {

    private ProgramaPorDefecto() {
    }

    // define la direccion inicial de datos según si la memoria es separada o unificada
    private static int baseDatos(TipoArquitectura tipo) {
        return tipo == TipoArquitectura.HARVARD ? 0 : 10;
    }

    // genera un programa secuencial simple que suma dos numeros almacenados en memoria
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

    // genera un bucle de multiplicacion por sumas repetidas ideal para evaluar localidad en L1 y L2
    public static String multiplicacion(TipoArquitectura tipo, int a, int b) {
        int d = baseDatos(tipo);
        int contador = d, factor = d + 1, uno = d + 2, resultado = d + 3;
        return String.join("\n",
                "; RES = A * B mediante sumas repetidas",
                "INICIO: LOAD " + contador + "    ; ACC = contador (A)",
                "        JZ FIN        ; si el contador llego a 0 terminar",
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

    // demuestra como una escritura en memoria unificada puede modificar una instruccion e invalidar L1I
    public static String autoModificable() {
        return String.join("\n",
                "; Demostracion de codigo automodificable",
                "LOAD 10        ; ACC = 0 (palabra de la instruccion HALT)",
                "STORE 3        ; sobrescribe la instruccion de la direccion 3",
                "ADD 11         ; ACC += 5",
                "ADD 11         ; en memoria unificada ya fue reemplazada por HALT",
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