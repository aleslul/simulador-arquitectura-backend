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

        if (finalizado) {
            return empaquetarEstado(dto, "Programa finalizado. No hay más ciclos.");
        }

        int instruccion = memoriaInstrucciones.leer(cpu.getPc());
        int opcode = instruccion / 100;
        int direccion = instruccion % 100; // Esta dirección ahora apunta a memoriaDatos

        String log = "";

        switch (opcode) {
            case 0: // HALT
                cpu.setIr("HALT");
                finalizado = true;
                log = "Fin de ejecución.";
                break;
            case 10: // LOAD
                cpu.setIr("LOAD " + direccion);
                cpu.setAcumulador(memoriaDatos.leer(direccion));
                this.ciclosReloj += 1; // Acceso en paralelo
                log = "Carga " + cpu.getAcumulador() + " al Acumulador. (Bus separado: 1 ciclo)";
                break;
            case 20: // ADD
                cpu.setIr("ADD " + direccion);
                int valorSumar = memoriaDatos.leer(direccion);
                cpu.setAcumulador(cpu.getAcumulador() + valorSumar);
                this.ciclosReloj += 1;
                log = "Suma " + valorSumar + " al Acumulador. (Bus separado: 1 ciclo)";
                break;
            case 30: // STORE
                cpu.setIr("STORE " + direccion);
                memoriaDatos.escribir(direccion, cpu.getAcumulador());
                this.ciclosReloj += 1;
                log = "Guarda " + cpu.getAcumulador() + " en memoria de datos. (Bus separado: 1 ciclo)";
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
