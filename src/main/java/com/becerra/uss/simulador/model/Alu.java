package com.becerra.uss.simulador.model;

/**
 * Unidad aritmético-lógica con ancho de palabra configurable (1..32 bits), complemento a dos y flags.
 *   Z: resultado = 0
 *   N: resultado negativo
 *   C: acarreo en la suma, o préstamo (borrow) en la resta
 *   V: desbordamiento con signo
 */
public final class Alu {

    private Alu() {
    }

    public record Resultado(int valor, boolean z, boolean n, boolean c, boolean v) {
    }

    /** ENTRA UN ENTERO DE 32 BITS QUE ES EN LO QUE FUNCIONA LA VM DE JAVA Y LO CONVIERTE A MENOS PARA Q LO LEA EL ALU*/
    public static int normalizar(int valor, int bits) {
        int desplazamiento = 32 - bits;
        return (valor << desplazamiento) >> desplazamiento;
    }

    public static Resultado sumar(int a, int b, int bits) {
        long mascara = mascara(bits);
        long suma = (a & mascara) + (b & mascara);
        int resultado = normalizar((int) suma, bits);
        //FLAGS
        boolean c = suma > mascara;
        boolean sa = normalizar(a, bits) < 0;
        boolean sb = normalizar(b, bits) < 0;
        boolean v = (sa == sb) && ((resultado < 0) != sa);
        return new Resultado(resultado, resultado == 0, resultado < 0, c, v);
    }

    public static Resultado restar(int a, int b, int bits) {
        long mascara = mascara(bits);
        long diferencia = (a & mascara) - (b & mascara);
        int resultado = normalizar((int) diferencia, bits);
        // FLAGS
        boolean c = diferencia < 0; // hubo préstamo
        boolean sa = normalizar(a, bits) < 0;
        boolean sb = normalizar(b, bits) < 0;
        boolean v = (sa != sb) && ((resultado < 0) != sa);
        return new Resultado(resultado, resultado == 0, resultado < 0, c, v);
    }

    /** LA MASCARA LLENA DE PUROS 1 PARA EL BUS ASI NOS ASEGURAMOS Q LA SUMA O RESTA FUNCIONEN BIEN A PESAR DE LOS 32 BITS MIN EN LOS Q FUNCIONA LA VM DE JAVA*/
    private static long mascara(int bits) {
        return (1L << bits) - 1;
    }
}
