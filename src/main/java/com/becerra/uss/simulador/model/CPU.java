package com.becerra.uss.simulador.model;

/** Registros visibles de la CPU */
public class CPU {
    private int pc;               // Contador de programa
    private String ir = "NOP";    // Registro de instrucción (decodificada)
    private int acumulador;
    private int mar;              // Registro de dirección de memoria (último acceso)
    private int mdr;              // Registro de datos de memoria (último acceso)
    private final Flags flags = new Flags();

    public int getPc() { return pc; }
    public void setPc(int pc) { this.pc = pc; }
    public void incrementarPc() { this.pc++; }

    public String getIr() { return ir; }
    public void setIr(String ir) { this.ir = ir; }

    public int getAcumulador() { return acumulador; }
    public void setAcumulador(int acumulador) { this.acumulador = acumulador; }

    public int getMar() { return mar; }
    public void setMar(int mar) { this.mar = mar; }

    public int getMdr() { return mdr; }
    public void setMdr(int mdr) { this.mdr = mdr; }

    public Flags getFlags() { return flags; }
}
