package com.becerra.uss.simulador.model;

import com.becerra.uss.simulador.DTO.EstadoSimulacionDTO;

public class Harvard extends ArquitecturaBase {
    private Memoria memoriaInstrucciones;
    private Memoria memoriaDatos;
    private boolean finalizado = false;

    public Harvard(int tamanoInst, int tamanoDat, int valorA, int valorB) {
        super();

        this.memoriaInstrucciones = new Memoria(tamanoInst);
        this.memoriaDatos = new Memoria(tamanoDat);

        // LA MISMA PUTA SIMULACION PEDORRA
        this.memoriaInstrucciones.escribir(0, 1000); // LOAD 0 (Dirección 0 de Mem. Datos)
        this.memoriaInstrucciones.escribir(1, 2001); // ADD 1  (Dirección 1 de Mem. Datos)
        this.memoriaInstrucciones.escribir(2, 3002); // STORE 2(Dirección 2 de Mem. Datos)
        this.memoriaInstrucciones.escribir(3, 0000); // HALT

        // --- CARGAMOS LOS DATOS EN MEMORIA DE DATOS ---
        this.memoriaDatos.escribir(0, valorA);
        this.memoriaDatos.escribir(1, valorB);
        // La celda 2 está vacía (0) esperando el resultado
    }

    @Override
    public EstadoSimulacionDTO ejecutarPaso() {
        EstadoSimulacionDTO dto = new EstadoSimulacionDTO();
        dto.arquitectura = "Harvard";

        if (finalizado) return empaquetarEstado(dto, "Programa finalizado.");

        // CONSTANTES DE REALISMO
        final int LATENCIA_MEMORIA = 3;
        final int TIEMPO_PROCESAMIENTO_CPU = 1;

        // Fetch de la instrucción
        int instruccion = memoriaInstrucciones.leer(cpu.getPc());
        int opcode = instruccion / 100;
        int direccion = instruccion % 100;

        String log = "";

        switch (opcode) {
            case 0:
                cpu.setIr("HALT");
                finalizado = true;
                log = "Fin de ejecución.";
                break;
            case 10: // LOAD
                cpu.setIr("LOAD " + direccion);
                cpu.setAcumulador(memoriaDatos.leer(direccion));
                // Solapamiento: Fetch y Data Read ocurren en paralelo en esos 3 ciclos
                this.ciclosReloj += LATENCIA_MEMORIA;
                log = "LOAD: Fetch y Data Read paralelos. Total: 3 ciclos en este paso.";
                break;
            case 20: // ADD
                cpu.setIr("ADD " + direccion);
                int valorSumar = memoriaDatos.leer(direccion);
                cpu.setAcumulador(cpu.getAcumulador() + valorSumar);
                // Solapamiento (3) + Tiempo de ALU (1)
                this.ciclosReloj += (LATENCIA_MEMORIA + TIEMPO_PROCESAMIENTO_CPU);
                log = "ADD: Paralelismo (3) + Suma ALU (1). Total: 4 ciclos.";
                break;
            case 30: // STORE
                cpu.setIr("STORE " + direccion);
                memoriaDatos.escribir(direccion, cpu.getAcumulador());
                this.ciclosReloj += LATENCIA_MEMORIA;
                log = "STORE: Fetch y Data Write paralelos. Total: 3 ciclos.";
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
        dto.memoriaInstrucciones = memoriaInstrucciones.getEstadoCeldas();
        dto.memoriaDatos = memoriaDatos.getEstadoCeldas();
        dto.logOperacion = log;
        return dto;
    }
}
