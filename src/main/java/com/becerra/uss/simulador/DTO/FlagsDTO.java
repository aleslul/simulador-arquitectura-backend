package com.becerra.uss.simulador.DTO;

/** Banderas de la CPU: Zero, Negative, Carry (acarreo/préstamo) y oVerflow */
public record FlagsDTO(boolean z, boolean n, boolean c, boolean v) {
}
