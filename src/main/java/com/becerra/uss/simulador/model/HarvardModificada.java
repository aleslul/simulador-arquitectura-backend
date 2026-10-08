package com.becerra.uss.simulador.model;

import com.becerra.uss.simulador.DTO.EstadisticasCacheDTO;

/**
 * Harvard modificada: así son las CPU actuales. Hacia la CPU hay dos cachés L1 separadas (instrucciones y datos),
 * pero por detrás hay UNA sola RAM unificada.
 * <ul>
 *   <li>Acierto en L1: cuesta {@code latenciaCache} ciclos y solo ocupa el puerto de esa caché.</li>
 *   <li>Fallo: cuesta {@code latenciaMemoria + latenciaCache} ciclos y ocupa el puerto de la caché y la RAM
 *       (si L1I y L1D fallan a la vez, compiten por la RAM como en Von Neumann).</li>
 *   <li>Escritura: write-through con write-allocate (cuesta {@code latenciaMemoria}) e invalida la línea en L1I,
 *       así que el código automodificable sigue funcionando.</li>
 * </ul>
 */
public final class HarvardModificada extends ArquitecturaBase {

    private final Memoria ram;
    private final Cache l1i;
    private final Cache l1d;
    private final Recurso puertoL1i = new Recurso("L1I");
    private final Recurso puertoL1d = new Recurso("L1D");
    private final Recurso busRam = new Recurso("RAM");

    public HarvardModificada(ConfiguracionSimulador config, ProgramaEnsamblado programa) {
        super(config);
        this.ram = new Memoria("RAM", config.tamMemoriaUnificada(), Instruccion.BITS_PALABRA);
        this.l1i = new Cache("L1I", config.lineasCache());
        this.l1d = new Cache("L1D", config.lineasCache());
        cargarEnMemoriaUnificada(programa, ram);
    }

    @Override
    protected int leerInstruccion(int direccion) {
        return ram.leer(direccion);
    }

    @Override
    protected int leerDato(int direccion) {
        return ram.leer(direccion);
    }

    @Override
    protected void escribirDato(int direccion, int valor) {
        ram.escribir(direccion, valor);
    }

    @Override
    protected Acceso accesoInstruccion(int direccion, int solicitud) {
        boolean acierto = l1i.acceder(direccion);
        if (acierto) {
            return reservar(puertoL1i, solicitud, config.latenciaCache(), true);
        }
        Acceso a = reservarConRam(puertoL1i, solicitud, config.latenciaMemoria() + config.latenciaCache(), "L1I→RAM", false);
        l1i.insertar(direccion);
        return a;
    }

    @Override
    protected Acceso accesoDato(int direccion, boolean escritura, int solicitud) {
        if (escritura) {
            Acceso a = reservarConRam(puertoL1d, solicitud, config.latenciaMemoria(), "L1D→RAM", null);
            l1d.insertar(direccion);   // write-allocate
            l1i.invalidar(direccion);  // por si se modificó una instrucción
            return a;
        }
        boolean acierto = l1d.acceder(direccion);
        if (acierto) {
            return reservar(puertoL1d, solicitud, config.latenciaCache(), true);
        }
        Acceso a = reservarConRam(puertoL1d, solicitud, config.latenciaMemoria() + config.latenciaCache(), "L1D→RAM", false);
        l1d.insertar(direccion);
        return a;
    }

    /** Reserva a la vez el puerto de la caché y la RAM (empieza cuando ambos están libres). */
    private Acceso reservarConRam(Recurso puerto, int solicitud, int duracion, String nombre, Boolean acierto) {
        int inicio = Math.max(puerto.inicioDisponible(solicitud), busRam.inicioDisponible(solicitud));
        int fin = inicio + duracion;
        puerto.ocupar(fin);
        busRam.ocupar(fin);
        return new Acceso(inicio, fin, nombre, acierto);
    }

    @Override
    protected int[] memoriaPrincipal() {
        return ram.getEstadoCeldas();
    }

    @Override
    protected int[] memoriaInstrucciones() {
        return null;
    }

    @Override
    protected int[] memoriaDatos() {
        return null;
    }

    @Override
    protected EstadisticasCacheDTO estadisticasCache() {
        return new EstadisticasCacheDTO(l1i.getAciertos(), l1i.getFallos(), l1d.getAciertos(), l1d.getFallos());
    }
}
