package com.becerra.uss.simulador.model;

import com.becerra.uss.simulador.DTO.EstadoSimulacionDTO;

public class VonNeumann extends ArquitecturaBase {
    private Memoria memoriaPrincipal;
    private boolean finalizado = false;

    public VonNeumann(int tamano, int valorA, int valorB) {
        super();
        this.memoriaPrincipal = new Memoria(tamano);

        // SIMULACION DE SUMA DE VERDAD DE VERDAD ESTA VEZ SI ES DE VERDAD
        // Sumar dos números almacenados en memoria y guardar el resultado en una nueva dirección (C = A + B)

        // Instrucciones (Espacios 0 al 3)
        this.memoriaPrincipal.escribir(0, 1010); // LOAD 10 (Cargar dato de celda 10)
        this.memoriaPrincipal.escribir(1, 2011); // ADD 11  (Sumar dato de celda 11)
        this.memoriaPrincipal.escribir(2, 3012); // STORE 12(Guardar resultado en celda 12)
        this.memoriaPrincipal.escribir(3, 0000); // HALT    (Fin)

        // Datos iniciales (Espacios 10 y 11)
        this.memoriaPrincipal.escribir(10, valorA); // Valor A = 5
        this.memoriaPrincipal.escribir(11, valorB); // Valor B = 7
        // La celda 12 está vacía (0) esperando el resultado
    }

    @Override
    public EstadoSimulacionDTO ejecutarPaso() {
        EstadoSimulacionDTO dto = new EstadoSimulacionDTO();
        dto.arquitectura = "Von Neumann";

        if (finalizado) return empaquetarEstado(dto, "Programa finalizado.");

        // CONSTANTES DE REALISMO: En realidad la memoria RAM es más lenta que el procesador, para representar esto se agregaron estas constantes que determinan cuantos ciclos para acceder a la RAM y a la ALU
        final int LATENCIA_BUS_MEMORIA = 3; // Ir a RAM tarda 3 ciclos
        final int TIEMPO_PROCESAMIENTO_CPU = 1; // La ALU interna es rápida tardadno 1 ciclo

        // 1. Fetch: La CPU tiene que ir a la Memoria Principal por el bus único
        int instruccion = memoriaPrincipal.leer(cpu.getPc());
        this.ciclosReloj += LATENCIA_BUS_MEMORIA;

        int opcode = instruccion / 100;
        int direccion = instruccion % 100;
        String log = "";

        // 2. Decode y Execute
        switch (opcode) {
            case 0:
                cpu.setIr("HALT");
                finalizado = true;
                log = "Fin de ejecución.";
                break;
            case 10: // LOAD
                cpu.setIr("LOAD " + direccion);
                cpu.setAcumulador(memoriaPrincipal.leer(direccion));
                this.ciclosReloj += LATENCIA_BUS_MEMORIA; // Otro viaje lento por el dato
                log = "LOAD: Fetch (3) + Data Read (3). Total: 6 ciclos en este paso.";
                break;
            case 20: // ADD
                cpu.setIr("ADD " + direccion);
                int valorSumar = memoriaPrincipal.leer(direccion);
                cpu.setAcumulador(cpu.getAcumulador() + valorSumar);
                // Viaje lento por el dato + tiempo rápido de suma interna
                this.ciclosReloj += (LATENCIA_BUS_MEMORIA + TIEMPO_PROCESAMIENTO_CPU);
                log = "ADD: Fetch (3) + Data Read (3) + Suma ALU (1). Total: 7 ciclos.";
                break;
            case 30: // STORE
                cpu.setIr("STORE " + direccion);
                memoriaPrincipal.escribir(direccion, cpu.getAcumulador());
                this.ciclosReloj += LATENCIA_BUS_MEMORIA;
                log = "STORE: Fetch (3) + Data Write (3). Total: 6 ciclos.";
                break;
        }

        if (!finalizado) cpu.incrementarPc();
        return empaquetarEstado(dto, log);
    }

    private EstadoSimulacionDTO empaquetarEstado(EstadoSimulacionDTO dto, String log) {
        dto.pc = cpu.getPc();
        dto.ir = cpu.getIr();
        dto.acumulador = cpu.getAcumulador();
        dto.ciclosReloj = this.ciclosReloj;
        dto.memoriaPrincipal = memoriaPrincipal.getEstadoCeldas();
        dto.logOperacion = log;
        return dto;
    }
}
