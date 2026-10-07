package com.becerra.uss.simulador.model;

import com.becerra.uss.simulador.DTO.EstadoSimulacionDTO;

public class VonNeumann extends ArquitecturaBase {
    private Memoria memoriaPrincipal;
    private boolean finalizado = false;

    public VonNeumann(int tamano) {
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
        this.memoriaPrincipal.escribir(10, 5); // Valor A = 5
        this.memoriaPrincipal.escribir(11, 7); // Valor B = 7
        // La celda 12 está vacía (0) esperando el resultado
    }

    @Override
    public EstadoSimulacionDTO ejecutarPaso() {
        EstadoSimulacionDTO dto = new EstadoSimulacionDTO();
        dto.arquitectura = "Von Neumann";

        if (finalizado) {
            return empaquetarEstado(dto, "Programa finalizado. No hay más ciclos.");
        }

        // 1. Fetch de la instrucción
        int instruccion = memoriaPrincipal.leer(cpu.getPc());
        int opcode = instruccion / 100;     // Ej: 1010 / 100 = 10
        int direccion = instruccion % 100;  // Ej: 1010 % 100 = 10

        String log = "";

        // 2. Decode & Execute
        switch (opcode) {
            case 0: // HALT
                cpu.setIr("HALT");
                finalizado = true;
                log = "Fin de ejecución.";
                break;
            case 10: // LOAD
                cpu.setIr("LOAD " + direccion);
                cpu.setAcumulador(memoriaPrincipal.leer(direccion));
                this.ciclosReloj += 2; // 1 ciclo para instrucción + 1 para dato
                log = "Carga " + cpu.getAcumulador() + " al Acumulador. (Cuello de botella: 2 ciclos)";
                break;
            case 20: // ADD
                cpu.setIr("ADD " + direccion);
                int valorSumar = memoriaPrincipal.leer(direccion);
                cpu.setAcumulador(cpu.getAcumulador() + valorSumar);
                this.ciclosReloj += 2;
                log = "Suma " + valorSumar + " al Acumulador. (Cuello de botella: 2 ciclos)";
                break;
            case 30: // STORE
                cpu.setIr("STORE " + direccion);
                memoriaPrincipal.escribir(direccion, cpu.getAcumulador());
                this.ciclosReloj += 2;
                log = "Guarda " + cpu.getAcumulador() + " en memoria principal. (Cuello de botella: 2 ciclos)";
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
