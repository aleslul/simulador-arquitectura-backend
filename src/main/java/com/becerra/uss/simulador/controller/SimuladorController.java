package com.becerra.uss.simulador.controller;

import com.becerra.uss.simulador.DTO.EstadoSimulacionDTO;
import com.becerra.uss.simulador.service.SimuladorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulador")
@CrossOrigin("*")
public class SimuladorController {
    @Autowired
    private SimuladorService simuladorService;

    @PostMapping("/iniciar")
    public ResponseEntity<String> iniciar(
            @RequestParam String tipo,
            @RequestParam(defaultValue = "5") int valorA,
            @RequestParam(defaultValue = "7") int valorB) {
        try {
            simuladorService.iniciarSimulador(tipo, valorA, valorB);
            return ResponseEntity.ok("Simulador " + tipo + " iniciado correctamente con los valores " + valorA + " y " + valorB);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al iniciar: " + e.getMessage());
        }
    }

    @GetMapping("/paso")
    public ResponseEntity<EstadoSimulacionDTO> siguientePaso() {
        try {
            return ResponseEntity.ok(simuladorService.ejecutarPaso());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
