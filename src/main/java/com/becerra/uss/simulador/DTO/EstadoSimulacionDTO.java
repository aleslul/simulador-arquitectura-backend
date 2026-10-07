package com.becerra.uss.simulador.DTO;

public class EstadoSimulacionDTO {
    public String arquitectura;
    public int pc;
    public String ir;
    public int acumulador;
    public int ciclosReloj;

    public int[] memoriaPrincipal;
    public int[] memoriaInstrucciones;
    public int[] memoriaDatos;
    public String logOperacion;
}
