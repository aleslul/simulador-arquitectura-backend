package com.becerra.uss.simulador.model;

/** Registro de banderas de la CPU. */
public class Flags {
    private boolean z;
    private boolean n;
    private boolean c;
    private boolean v;

    public void actualizar(Alu.Resultado r) {
        this.z = r.z();
        this.n = r.n();
        this.c = r.c();
        this.v = r.v();
    }

    /** LOAD solo actualiza Z y N (no toca C ni V). */
    public void actualizarZN(int valor) {
        this.z = valor == 0;
        this.n = valor < 0;
    }

    public boolean isZ() { return z; }
    public boolean isN() { return n; }
    public boolean isC() { return c; }
    public boolean isV() { return v; }
}
