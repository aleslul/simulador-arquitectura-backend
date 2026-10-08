package com.becerra.uss.simulador.model;

import com.becerra.uss.simulador.exception.FalloEjecucionException;

/**
 * Instrucción de 16 bits: [ opcode (4 bits) | operando (12 bits) ].
 * Ejemplo: LOAD 10 = 0001 0000 0000 1010 = 0x100A = 4106.
 */
public record Instruccion(Opcode opcode, int operando) {

    public static final int BITS_PALABRA = 16;
    public static final int BITS_OPERANDO = 12;
    public static final int MAX_OPERANDO = (1 << BITS_OPERANDO) - 1; // 4095
    private static final int MASCARA_PALABRA = (1 << BITS_PALABRA) - 1;

    public Instruccion {
        if (operando < 0 || operando > MAX_OPERANDO) {
            throw new IllegalArgumentException("El operando debe estar entre 0 y " + MAX_OPERANDO);
        }
    }

    public int codificar() {
        return (opcode.getCodigo() << BITS_OPERANDO) | operando;
    }

    /** Decodifica una palabra; lanza FalloEjecucionException(OPCODE_INVALIDO) si el opcode no existe. */
    public static Instruccion decodificar(int palabra) {
        int w = palabra & MASCARA_PALABRA;
        int codigo = w >>> BITS_OPERANDO;
        Opcode op = Opcode.desdeCodigo(codigo);
        if (op == null) {
            throw new FalloEjecucionException("OPCODE_INVALIDO",
                    "Opcode inválido " + codigo + " en la palabra 0x" + String.format("%04X", w) + ".");
        }
        return new Instruccion(op, w & MAX_OPERANDO);
    }

    /** Texto legible de una palabra de instrucción, o null si no es una instrucción válida. */
    public static String desensamblar(int palabra) {
        try {
            return decodificar(palabra).toString();
        } catch (FalloEjecucionException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return opcode.tieneOperando() ? opcode.name() + " " + operando : opcode.name();
    }
}
