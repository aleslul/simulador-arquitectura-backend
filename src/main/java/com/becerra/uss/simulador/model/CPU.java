package com.becerra.uss.simulador.model;

public class CPU {
    private int pc; // Contador del programa
    private String ir; // Registro de instrucciones
    private int acumulador;

    public CPU() {
        this.pc = 0;
        this.ir = "NOP";
        this.acumulador = 0;
    }

    public int getPc() { return pc; }
    public void incrementarPc() { this.pc++; }

    public String getIr() { return ir; }
    public void setIr(String ir) { this.ir = ir; }

    public int getAcumulador() { return acumulador; }
    public void setAcumulador(int acumulador) { this.acumulador = acumulador; }

}
