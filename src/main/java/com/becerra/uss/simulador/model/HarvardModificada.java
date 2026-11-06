package com.becerra.uss.simulador.model;

import com.becerra.uss.simulador.DTO.EstadisticasCacheDTO;

// implementa la jerarquia de memoria con caches L1 separadas cache L2 unificada y memoria RAM
public final class HarvardModificada extends ArquitecturaBase {

    private final Memoria ram;
    private final Cache l1i;
    private final Cache l1d;
    private final Cache l2;
    private final Recurso puertoL1i = new Recurso("L1I");
    private final Recurso puertoL1d = new Recurso("L1D");
    private final Recurso puertoL2 = new Recurso("L2");
    private final Recurso busRam = new Recurso("RAM");

    // inicializa la memoria principal y los niveles de cache L1I L1D y L2 con el algoritmo elegido
    public HarvardModificada(ConfiguracionSimulador config, ProgramaEnsamblado programa) {
        super(config);
        this.ram = new Memoria("RAM", config.tamMemoriaUnificada(), Instruccion.BITS_PALABRA);
        this.l1i = new Cache("L1I", config.lineasCache(), config.algoritmoReemplazo());
        this.l1d = new Cache("L1D", config.lineasCache(), config.algoritmoReemplazo());
        this.l2 = new Cache("L2", config.lineasCacheL2(), config.algoritmoReemplazo());
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

    // busca la instruccion en L1I luego en L2 y si falla en ambas acude a la RAM
    @Override
    protected Acceso accesoInstruccion(int direccion, int solicitud) {
        // si acierta en L1I solo consume la latencia de L1
        if (l1i.acceder(direccion)) {
            return reservar(puertoL1i, solicitud, config.latenciaCache(), true);
        }
        // si falla en L1I pero acierta en L2 consume la latencia de L2 y sube el bloque a L1I
        if (l2.acceder(direccion)) {
            Acceso a = reservarConL2(puertoL1i, solicitud, config.latenciaCacheL2(), "L1I->L2", false);
            l1i.insertar(direccion);
            return a;
        }
        // si falla en L1I y en L2 va hasta la RAM y carga el bloque en L2 y en L1I
        Acceso a = reservarConRam(puertoL1i, solicitud, config.latenciaMemoria(), "L1I->L2->RAM", false);
        l2.insertar(direccion);
        l1i.insertar(direccion);
        return a;
    }

    // gestiona la lectura y escritura de datos atravesando L1D L2 y la RAM
    @Override
    protected Acceso accesoDato(int direccion, boolean escritura, int solicitud) {
        // aplica politica Write-Through con Write-Allocate actualizando L1D L2 y RAM
        if (escritura) {
            Acceso a = reservarConRam(puertoL1d, solicitud, config.latenciaMemoria(), "L1D->L2->RAM", null);
            l1d.insertar(direccion);
            l2.insertar(direccion);
            l1i.invalidar(direccion);
            return a;
        }
        // si es lectura y acierta en L1D responde con la latencia de L1
        if (l1d.acceder(direccion)) {
            return reservar(puertoL1d, solicitud, config.latenciaCache(), true);
        }
        // si falla en L1D pero acierta en L2 responde con la latencia de L2 y copia a L1D
        if (l2.acceder(direccion)) {
            Acceso a = reservarConL2(puertoL1d, solicitud, config.latenciaCacheL2(), "L1D->L2", false);
            l1d.insertar(direccion);
            return a;
        }
        // si falla en L1D y L2 lee de la RAM y guarda el dato en L2 y L1D
        Acceso a = reservarConRam(puertoL1d, solicitud, config.latenciaMemoria(), "L1D->L2->RAM", false);
        l2.insertar(direccion);
        l1d.insertar(direccion);
        return a;
    }

    // reserva simultaneamente el puerto de L1 y el puerto de L2 cuando hay acierto en L2
    private Acceso reservarConL2(Recurso puertoL1, int solicitud, int duracion, String nombre, Boolean acierto) {
        int inicio = Math.max(puertoL1.inicioDisponible(solicitud), puertoL2.inicioDisponible(solicitud));
        int fin = inicio + duracion;
        puertoL1.ocupar(fin);
        puertoL2.ocupar(fin);
        return new Acceso(inicio, fin, nombre, acierto);
    }

    // reserva el puerto L1 el puerto L2 y el bus de la RAM cuando ocurre un fallo total
    private Acceso reservarConRam(Recurso puertoL1, int solicitud, int duracion, String nombre, Boolean acierto) {
        int dispL1L2 = Math.max(puertoL1.inicioDisponible(solicitud), puertoL2.inicioDisponible(solicitud));
        int inicio = Math.max(dispL1L2, busRam.inicioDisponible(solicitud));
        int fin = inicio + duracion;
        puertoL1.ocupar(fin);
        puertoL2.ocupar(fin);
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

    // empaqueta las metricas actuales de L1I L1D y L2 para enviarlas en el estado
    @Override
    protected EstadisticasCacheDTO estadisticasCache() {
        return new EstadisticasCacheDTO(
                l1i.getAciertos(),
                l1i.getFallos(),
                l1d.getAciertos(),
                l1d.getFallos(),
                l2.getAciertos(),
                l2.getFallos(),
                l1i.getEstadoLineas(),
                l1d.getEstadoLineas(),
                l2.getEstadoLineas()
        );
    }
}