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
    public ResponseEntity<String> iniciar(@RequestParam String tipo) {
        try {
            simuladorService.iniciarSimulador(tipo);
            return ResponseEntity.ok("Simulador " + tipo + " iniciado correctamente");
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
