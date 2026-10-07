package com.becerra.uss.simulador.model;
import com.becerra.uss.simulador.DTO.EstadoSimulacionDTO;

public abstract class ArquitecturaBase {
    protected CPU cpu;
    protected int ciclosReloj;

    public ArquitecturaBase() {
        this.cpu = new CPU();
        this.ciclosReloj = 0;
    }

    public abstract EstadoSimulacionDTO ejecutarPaso();
}
