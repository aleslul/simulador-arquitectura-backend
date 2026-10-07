package com.becerra.uss.simulador.service;

import com.becerra.uss.simulador.DTO.EstadoSimulacionDTO;
import com.becerra.uss.simulador.model.ArquitecturaBase;
import com.becerra.uss.simulador.model.Harvard;
import com.becerra.uss.simulador.model.VonNeumann;
import org.springframework.stereotype.Service;

@Service
public class SimuladorService {
    private ArquitecturaBase simuladorActivo;

    public void iniciarSimulador(String tipo) {
        if ("HARVARD".equalsIgnoreCase(tipo)) {
            simuladorActivo = new Harvard(16, 16); // MEMORIAS SEPARADAS DE 16 ESPACIOS CADA UNA
        } else {
            simuladorActivo = new VonNeumann(32); // UNA SOLA MEMORIA DE 32 ESPACIOS
        }
    }

    public EstadoSimulacionDTO ejecutarPaso() {
        if (simuladorActivo == null) {
            throw new IllegalStateException("El simulador no ha sido inicializado, atarantao");
        }
        return simuladorActivo.ejecutarPaso();
    }
}
