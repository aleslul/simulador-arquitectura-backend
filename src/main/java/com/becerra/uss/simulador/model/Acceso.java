package com.becerra.uss.simulador.model;

/**
 * RESULTADO DE TEMPORIZADOR UN ACCESO A MEMORIA
 *
 * inicio: ciclo en que realmente empieza
 * fin: ciclo en que termina lol
 * recurso: RECURSO USADO COMO LOS BUSES, LA RAM O LA CACHE EN EL MODIFCADO
 * acierto: true/false si pasó por una cache pero no lo usamos pq harvar modificado zzz
 */
public record Acceso(int inicio, int fin, String recurso, Boolean acierto) {
}
