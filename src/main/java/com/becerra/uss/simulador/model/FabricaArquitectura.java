package com.becerra.uss.simulador.model;

/** Crea la arquitectura que corresponde a la configuración. */
public final class FabricaArquitectura {

    private FabricaArquitectura() {
    }

    public static ArquitecturaBase crear(ConfiguracionSimulador config, ProgramaEnsamblado programa) {
        return switch (config.tipo()) {
            case VON_NEUMANN -> new VonNeumann(config, programa);
            case HARVARD -> new Harvard(config, programa);
            case HARVARD_MODIFICADA -> new HarvardModificada(config, programa);
        };
    }
}
