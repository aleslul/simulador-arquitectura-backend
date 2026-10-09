package com.becerra.uss.simulador.model;

/**
 * EL QUE TIENE DOS BUSES Y ABRIO SU EMPRESA DE BUSES
 */
public final class Harvard extends ArquitecturaBase {

    private final Memoria memoriaInstrucciones;
    private final Memoria memoriaDatos;
    private final Recurso busInstrucciones = new Recurso("BUS_INSTRUCCIONES");
    private final Recurso busDatos = new Recurso("BUS_DATOS");

    public Harvard(ConfiguracionSimulador config, ProgramaEnsamblado programa) {
        super(config);
        this.memoriaInstrucciones = new Memoria("memoria de instrucciones", config.tamMemoriaInstrucciones(),
                Instruccion.BITS_PALABRA);
        this.memoriaDatos = new Memoria("memoria de datos", config.tamMemoriaDatos(), config.anchoDatos());
        cargarEnHarvard(programa, memoriaInstrucciones, memoriaDatos);
    }

    @Override
    protected int leerInstruccion(int direccion) {
        return memoriaInstrucciones.leer(direccion);
    }

    @Override
    protected int leerDato(int direccion) {
        return memoriaDatos.leer(direccion);
    }

    @Override
    protected void escribirDato(int direccion, int valor) {
        memoriaDatos.escribir(direccion, valor);
    }

    @Override
    protected Acceso accesoInstruccion(int direccion, int solicitud) {
        return reservar(busInstrucciones, solicitud, config.latenciaMemoria(), null);
    }

    @Override
    protected Acceso accesoDato(int direccion, boolean escritura, int solicitud) {
        return reservar(busDatos, solicitud, config.latenciaMemoria(), null);
    }

    @Override
    protected int[] memoriaPrincipal() {
        return null;
    }

    @Override
    protected int[] memoriaInstrucciones() {
        return memoriaInstrucciones.getEstadoCeldas();
    }

    @Override
    protected int[] memoriaDatos() {
        return memoriaDatos.getEstadoCeldas();
    }
}
