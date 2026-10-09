package com.becerra.uss.simulador.controller;

import com.becerra.uss.simulador.DTO.EstadoSimulacionDTO;
import com.becerra.uss.simulador.DTO.RespuestaInicioDTO;
import com.becerra.uss.simulador.DTO.ResultadoComparacionDTO;
import com.becerra.uss.simulador.DTO.SolicitudSimulacionDTO;
import com.becerra.uss.simulador.service.SimuladorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** API REST del simulador. CORS se configura en {WebConfig}. */
@RestController
@RequestMapping("/api/simulador")
public class SimuladorController {

    private final SimuladorService simuladorService;

    public SimuladorController(SimuladorService simuladorService) {
        this.simuladorService = simuladorService;
    }

    /** Inicia una simulación con el programa por defecto (C = A + B) */
    @PostMapping("/iniciar")
    public RespuestaInicioDTO iniciar(
            @RequestParam("tipo") String tipo,
            @RequestParam(value = "modo", required = false) String modo,
            @RequestParam(value = "valorA", defaultValue = "5") int valorA,
            @RequestParam(value = "valorB", defaultValue = "7") int valorB) {
        SolicitudSimulacionDTO solicitud = new SolicitudSimulacionDTO(
                tipo, modo, valorA, valorB, null, null, null, null, null, null);
        return simuladorService.iniciar(solicitud, true);
    }

    /** Crea una simulación con programa propio y parámetros de hardware */
    @PostMapping("/sesiones")
    public RespuestaInicioDTO crearSesion(@RequestBody SolicitudSimulacionDTO solicitud) {
        return simuladorService.iniciar(solicitud, false);
    }

    @PostMapping("/paso")
    public EstadoSimulacionDTO paso(@RequestParam(value = "sesionId", required = false) String sesionId) {
        return simuladorService.ejecutarPaso(sesionId);
    }

    /** @deprecated Modifica el estado, por lo que debe ser POST. Se mantiene solo por compatibilidad */
    @Deprecated
    @GetMapping("/paso")
    public EstadoSimulacionDTO pasoGet(@RequestParam(value = "sesionId", required = false) String sesionId) {
        return simuladorService.ejecutarPaso(sesionId);
    }

    /** Estado actual sin avanzar la simulación */
    @GetMapping("/estado")
    public EstadoSimulacionDTO estado(@RequestParam(value = "sesionId", required = false) String sesionId) {
        return simuladorService.estado(sesionId);
    }

    /** Ejecuta hasta HALT, un fallo o el límite de pasos */
    @PostMapping("/ejecutar-todo")
    public EstadoSimulacionDTO ejecutarTodo(
            @RequestParam(value = "sesionId", required = false) String sesionId,
            @RequestParam(value = "maxPasos", defaultValue = "" + SimuladorService.MAX_PASOS_POR_DEFECTO) int maxPasos) {
        return simuladorService.ejecutarTodo(sesionId, maxPasos);
    }

    /** Vuelve al estado inicial (mismo programa y configuración) */
    @PostMapping("/reset")
    public EstadoSimulacionDTO reset(@RequestParam(value = "sesionId", required = false) String sesionId) {
        return simuladorService.reiniciar(sesionId);
    }

    /** Compara las 3 arquitecturas en los 2 modos con el programa por defecto */
    @GetMapping("/comparar")
    public List<ResultadoComparacionDTO> compararPorDefecto(
            @RequestParam(value = "valorA", defaultValue = "5") int valorA,
            @RequestParam(value = "valorB", defaultValue = "7") int valorB) {
        return simuladorService.comparar(new SolicitudSimulacionDTO(
                null, null, valorA, valorB, null, null, null, null, null, null));
    }

    /** Compara con un programa propio y/o parámetros de hardware (cuerpo JSON) */
    @PostMapping("/comparar")
    public List<ResultadoComparacionDTO> comparar(@RequestBody SolicitudSimulacionDTO solicitud) {
        return simuladorService.comparar(solicitud);
    }

    /** Programas de ejemplo (suma, multiplicación con bucle, código automodificable) */
    @GetMapping("/ejemplos")
    public Map<String, String> ejemplos(
            @RequestParam("tipo") String tipo,
            @RequestParam(value = "valorA", required = false) Integer valorA,
            @RequestParam(value = "valorB", required = false) Integer valorB) {
        return simuladorService.ejemplos(tipo, valorA, valorB);
    }
}

