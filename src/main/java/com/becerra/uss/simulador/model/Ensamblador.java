package com.becerra.uss.simulador.model;

import com.becerra.uss.simulador.exception.ErrorEnsambladoException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Ensamblador de dos pasadas.
 * <pre>
 * ; comentario (también // o #)
 * INICIO: LOAD 10        ; etiqueta + instrucción
 *         ADD  11
 *         JZ   FIN       ; los saltos aceptan etiqueta o dirección numérica
 *         STORE 12
 * FIN:    HALT
 * .DATO 10 5             ; directiva: MEM_DATOS[10] = 5 (decimal, 0x.. o negativo)
 * </pre>
 * Las instrucciones se cargan desde la dirección 0 en el orden en que aparecen.
 */
public final class Ensamblador {

    private static final int MAX_CARACTERES = 20_000;
    private static final Pattern IDENTIFICADOR = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private Ensamblador() {
    }

    private record Linea(int numero, Opcode opcode, String operando) {
    }

    public static ProgramaEnsamblado ensamblar(String fuente) {
        if (fuente == null || fuente.isBlank()) {
            throw new ErrorEnsambladoException(0, "El programa está vacío.");
        }
        if (fuente.length() > MAX_CARACTERES) {
            throw new ErrorEnsambladoException(0, "El programa supera el máximo de " + MAX_CARACTERES + " caracteres.");
        }

        List<Linea> instrucciones = new ArrayList<>();
        Map<String, Integer> etiquetas = new HashMap<>();
        Map<Integer, Integer> datos = new LinkedHashMap<>();

        // --- Pasada 1: etiquetas, directivas e instrucciones ---
        String[] crudas = fuente.split("\\R", -1);
        for (int i = 0; i < crudas.length; i++) {
            int numero = i + 1;
            String texto = quitarComentario(crudas[i]).trim();
            if (texto.isEmpty()) {
                continue;
            }

            int dosPuntos = texto.indexOf(':');
            if (dosPuntos >= 0) {
                String etiqueta = texto.substring(0, dosPuntos).trim();
                if (!IDENTIFICADOR.matcher(etiqueta).matches()) {
                    throw new ErrorEnsambladoException(numero, "Etiqueta inválida '" + etiqueta + "'.");
                }
                if (etiquetas.putIfAbsent(etiqueta.toUpperCase(Locale.ROOT), instrucciones.size()) != null) {
                    throw new ErrorEnsambladoException(numero, "Etiqueta duplicada '" + etiqueta + "'.");
                }
                texto = texto.substring(dosPuntos + 1).trim();
                if (texto.isEmpty()) {
                    continue;
                }
            }

            String[] partes = texto.split("[\\s,]+");
            String cabecera = partes[0].toUpperCase(Locale.ROOT);

            if (cabecera.startsWith(".")) {
                procesarDirectiva(numero, cabecera, partes, datos);
                continue;
            }

            Opcode opcode = Opcode.desdeMnemonico(cabecera);
            if (opcode == null) {
                throw new ErrorEnsambladoException(numero, "Instrucción desconocida '" + partes[0] + "'.");
            }
            if (opcode.tieneOperando()) {
                if (partes.length < 2) {
                    throw new ErrorEnsambladoException(numero, opcode + " requiere un operando.");
                }
                if (partes.length > 2) {
                    throw new ErrorEnsambladoException(numero, "Demasiados operandos para " + opcode + ".");
                }
            } else if (partes.length > 1) {
                throw new ErrorEnsambladoException(numero, opcode + " no admite operandos.");
            }
            instrucciones.add(new Linea(numero, opcode, partes.length > 1 ? partes[1] : null));
        }

        if (instrucciones.isEmpty()) {
            throw new ErrorEnsambladoException(0, "El programa no contiene instrucciones.");
        }

        // --- Pasada 2: resolver operandos y codificar ---
        int[] palabras = new int[instrucciones.size()];
        for (int i = 0; i < instrucciones.size(); i++) {
            Linea l = instrucciones.get(i);
            int operando = 0;
            if (l.opcode().tieneOperando()) {
                operando = resolverOperando(l, etiquetas);
            }
            palabras[i] = new Instruccion(l.opcode(), operando).codificar();
        }
        return new ProgramaEnsamblado(palabras, datos);
    }

    private static void procesarDirectiva(int numero, String cabecera, String[] partes, Map<Integer, Integer> datos) {
        if (!cabecera.equals(".DATO") && !cabecera.equals(".DATA")) {
            throw new ErrorEnsambladoException(numero, "Directiva desconocida '" + partes[0] + "' (use .DATO dirección valor).");
        }
        if (partes.length != 3) {
            throw new ErrorEnsambladoException(numero, ".DATO requiere dos valores: .DATO dirección valor");
        }
        int direccion = parsearNumero(numero, partes[1]);
        int valor = parsearNumero(numero, partes[2]);
        if (direccion < 0) {
            throw new ErrorEnsambladoException(numero, "La dirección de .DATO no puede ser negativa.");
        }
        if (datos.putIfAbsent(direccion, valor) != null) {
            throw new ErrorEnsambladoException(numero, "La dirección " + direccion + " ya fue definida con .DATO.");
        }
    }

    private static int resolverOperando(Linea l, Map<String, Integer> etiquetas) {
        String texto = l.operando();
        if (l.opcode().esSalto()) {
            Integer destino = etiquetas.get(texto.toUpperCase(Locale.ROOT));
            if (destino != null) {
                return destino;
            }
            if (IDENTIFICADOR.matcher(texto).matches()) {
                throw new ErrorEnsambladoException(l.numero(), "Etiqueta no definida '" + texto + "'.");
            }
        }
        int valor = parsearNumero(l.numero(), texto);
        if (valor < 0 || valor > Instruccion.MAX_OPERANDO) {
            throw new ErrorEnsambladoException(l.numero(),
                    "Operando fuera de rango (0-" + Instruccion.MAX_OPERANDO + "): " + valor + ".");
        }
        return valor;
    }

    private static int parsearNumero(int numeroLinea, String texto) {
        try {
            return Integer.decode(texto);
        } catch (NumberFormatException e) {
            throw new ErrorEnsambladoException(numeroLinea, "Número inválido '" + texto + "'.");
        }
    }

    private static String quitarComentario(String linea) {
        int corte = linea.length();
        for (String marca : new String[]{";", "//", "#"}) {
            int i = linea.indexOf(marca);
            if (i >= 0 && i < corte) {
                corte = i;
            }
        }
        return linea.substring(0, corte);
    }
}
