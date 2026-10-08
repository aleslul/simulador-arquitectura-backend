package com.becerra.uss.simulador.model;

/**
 * Von Neumann: UNA memoria para instrucciones y datos y UN solo bus.
 * Cualquier acceso (fetch, lectura o escritura de datos) usa el mismo bus, así que no pueden solaparse:
 * es el "cuello de botella de Von Neumann". Como el código está en la misma memoria que los datos, un STORE
 * puede sobrescribir instrucciones (código automodificable).
 */
public final class VonNeumann extends ArquitecturaBase {

    private final Memoria memoria;
    private final Recurso bus = new Recurso("BUS_UNICO");

    public VonNeumann(ConfiguracionSimulador config, ProgramaEnsamblado programa) {
        super(config);
        this.memoria = new Memoria("memoria principal", config.tamMemoriaUnificada(), Instruccion.BITS_PALABRA);
        cargarEnMemoriaUnificada(programa, memoria);
    }

    @Override
    protected int leerInstruccion(int direccion) {
        return memoria.leer(direccion);
    }

    @Override
    protected int leerDato(int direccion) {
        return memoria.leer(direccion);
    }

    @Override
    protected void escribirDato(int direccion, int valor) {
        memoria.escribir(direccion, valor);
    }

    @Override
    protected Acceso accesoInstruccion(int direccion, int solicitud) {
        return reservar(bus, solicitud, config.latenciaMemoria(), null);
    }

    @Override
    protected Acceso accesoDato(int direccion, boolean escritura, int solicitud) {
        return reservar(bus, solicitud, config.latenciaMemoria(), null);
    }

    @Override
    protected int[] memoriaPrincipal() {
        return memoria.getEstadoCeldas();
    }

    @Override
    protected int[] memoriaInstrucciones() {
        return null;
    }

    @Override
    protected int[] memoriaDatos() {
        return null;
    }
}
