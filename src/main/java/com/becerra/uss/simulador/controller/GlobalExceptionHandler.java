package com.becerra.uss.simulador.controller;

import com.becerra.uss.simulador.DTO.ErrorDTO;
import com.becerra.uss.simulador.exception.ErrorEnsambladoException;
import com.becerra.uss.simulador.exception.SesionNoEncontradaException;
import com.becerra.uss.simulador.exception.SimuladorNoInicializadoException;
import com.becerra.uss.simulador.exception.SolicitudInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce las excepciones de dominio a respuestas HTTP con un cuerpo JSON uniformE */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ErrorEnsambladoException.class)
    public ResponseEntity<ErrorDTO> alEnsamblar(ErrorEnsambladoException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDTO(e.getCodigo(), e.getMessage(), e.getLinea() > 0 ? e.getLinea() : null));
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<ErrorDTO> alValidar(SolicitudInvalidaException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDTO(e.getCodigo(), e.getMessage(), null));
    }

    @ExceptionHandler(SesionNoEncontradaException.class)
    public ResponseEntity<ErrorDTO> sesionInexistente(SesionNoEncontradaException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDTO("SESION_NO_ENCONTRADA", e.getMessage(), null));
    }

    @ExceptionHandler(SimuladorNoInicializadoException.class)
    public ResponseEntity<ErrorDTO> sinIniciar(SimuladorNoInicializadoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorDTO("SIMULADOR_NO_INICIALIZADO", e.getMessage(), null));
    }
}
