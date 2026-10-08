package com.becerra.uss.simulador.model;

import com.becerra.uss.simulador.exception.SolicitudInvalidaException;
import java.util.Locale;

public enum TipoArquitectura {
    VON_NEUMANN("Von Neumann"),
    HARVARD("Harvard"),
    HARVARD_MODIFICADA("Harvard modificada");

    private final String nombre;

    TipoArquitectura(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    /** Acepta "VON_NEUMANN", "von neumann", "Von-Neumann", "harvard modificada", etc. */
    public static TipoArquitectura desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new SolicitudInvalidaException("TIPO_INVALIDO",
                    "Debe indicar el tipo de arquitectura: VON_NEUMANN, HARVARD o HARVARD_MODIFICADA.");
        }
        String normalizado = texto.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z]+", "_");
        for (TipoArquitectura tipo : values()) {
            if (tipo.name().equals(normalizado)) {
                return tipo;
            }
        }
        throw new SolicitudInvalidaException("TIPO_INVALIDO",
                "Tipo de arquitectura desconocido: '" + texto + "'. Valores válidos: VON_NEUMANN, HARVARD, HARVARD_MODIFICADA.");
    }
}
