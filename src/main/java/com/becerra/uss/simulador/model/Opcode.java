package com.becerra.uss.simulador.model;

import java.util.Locale;

/** Conjunto de instrucciones (ISA) del simulador. El opcode ocupa los 4 bits altos de la palabra de 16 bits. */
public enum Opcode {
    HALT(0, false),
    LOAD(1, true),   // ACC <- MEM[dir]
    ADD(2, true),    // ACC <- ACC + MEM[dir]
    STORE(3, true),  // MEM[dir] <- ACC
    SUB(4, true),    // ACC <- ACC - MEM[dir]
    CMP(5, true),    // flags <- ACC - MEM[dir] (no modifica ACC)
    JMP(6, true),    // PC <- dir
    JZ(7, true);     // si Z=1, PC <- dir

    private static final Opcode[] VALORES = values();

    private final int codigo;
    private final boolean tieneOperando;

    Opcode(int codigo, boolean tieneOperando) {
        this.codigo = codigo;
        this.tieneOperando = tieneOperando;
    }

    public int getCodigo() {
        return codigo;
    }

    public boolean tieneOperando() {
        return tieneOperando;
    }

    public boolean esSalto() {
        return this == JMP || this == JZ;
    }

    public boolean usaAlu() {
        return this == ADD || this == SUB || this == CMP;
    }

    /** Devuelve null si el código no corresponde a ninguna instrucción. */
    public static Opcode desdeCodigo(int codigo) {
        for (Opcode op : VALORES) {
            if (op.codigo == codigo) {
                return op;
            }
        }
        return null;
    }

    /** Devuelve null si el mnemónico no existe. */
    public static Opcode desdeMnemonico(String mnemonico) {
        if (mnemonico == null) {
            return null;
        }
        String m = mnemonico.trim().toUpperCase(Locale.ROOT);
        for (Opcode op : VALORES) {
            if (op.name().equals(m)) {
                return op;
            }
        }
        return null;
    }
}
