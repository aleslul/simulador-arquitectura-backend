package com.becerra.uss.simulador.service;

import com.becerra.uss.simulador.DTO.EstadisticasCacheDTO;
import com.becerra.uss.simulador.DTO.EstadoSimulacionDTO;
import com.becerra.uss.simulador.DTO.RespuestaInicioDTO;
import com.becerra.uss.simulador.DTO.ResultadoComparacionDTO;
import com.becerra.uss.simulador.DTO.SolicitudSimulacionDTO;
import com.becerra.uss.simulador.exception.SesionNoEncontradaException;
import com.becerra.uss.simulador.exception.SimuladorNoInicializadoException;
import com.becerra.uss.simulador.exception.SolicitudInvalidaException;
import com.becerra.uss.simulador.model.ArquitecturaBase;
import com.becerra.uss.simulador.model.ConfiguracionSimulador;
import com.becerra.uss.simulador.model.Ensamblador;
import com.becerra.uss.simulador.model.FabricaArquitectura;
import com.becerra.uss.simulador.model.ModoEjecucion;
import com.becerra.uss.simulador.model.ProgramaEnsamblado;
import com.becerra.uss.simulador.model.ProgramaPorDefecto;
import com.becerra.uss.simulador.model.TipoArquitectura;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Administra las simulaciones. Cada simulación vive en su propia sesión (identificada por un UUID), por lo que
 * varios usuarios o pestañas no se pisan entre sí. La sesión "default" existe solo por compatibilidad con clientes
 * antiguos que no envían sesionId: apunta a la última simulación creada con POST /iniciar.
 */
@Service
public class SimuladorService {

    public static final String SESION_POR_DEFECTO = "default";
    public static final int MAX_PASOS_POR_DEFECTO = 10_000;
    public static final int MAX_PASOS_LIMITE = 1_000_000;

    private static final int VALOR_A_POR_DEFECTO = 5;
    private static final int VALOR_B_POR_DEFECTO = 7;

    private final ConcurrentMap<String, Sesion> sesiones = new ConcurrentHashMap<>();
    private final int maxSesiones;
    private final long inactividadMaximaMs;

    public SimuladorService(@Value("${simulador.sesiones.max:500}") int maxSesiones,
                            @Value("${simulador.sesiones.inactividad-minutos:30}") long inactividadMinutos) {
        this.maxSesiones = maxSesiones;
        this.inactividadMaximaMs = inactividadMinutos * 60_000L;
    }

    // ------------------------------------------------------------------------------------------
    // Sesión
    // ------------------------------------------------------------------------------------------

    /** Estado mutable de una simulación; todos sus métodos están sincronizados. */
    private static final class Sesion {
        private final ConfiguracionSimulador config;
        private final String programa;
        private ArquitecturaBase arquitectura;
        private volatile long ultimoUsoMs = System.currentTimeMillis();

        Sesion(ConfiguracionSimulador config, String programa, ArquitecturaBase arquitectura) {
            this.config = config;
            this.programa = programa;
            this.arquitectura = arquitectura;
        }

        void tocar() {
            ultimoUsoMs = System.currentTimeMillis();
        }

        synchronized EstadoSimulacionDTO paso() {
            return arquitectura.ejecutarPaso();
        }

        synchronized EstadoSimulacionDTO estado() {
            return arquitectura.estadoActual();
        }

        synchronized EstadoSimulacionDTO ejecutarTodo(int maxPasos) {
            int pasos = 0;
            while (!arquitectura.isFinalizado() && pasos < maxPasos) {
                arquitectura.ejecutarPaso();
                pasos++;
            }
            EstadoSimulacionDTO estado = arquitectura.estadoActual();
            if (!arquitectura.isFinalizado()) {
                return estado.conLog("Se alcanzó el límite de " + maxPasos
                        + " pasos sin terminar (¿bucle infinito?). Puede continuar con /paso o /ejecutar-todo.");
            }
            if (arquitectura.getError() != null) {
                return estado.conLog("La ejecución terminó con un fallo tras " + pasos + " pasos: "
                        + arquitectura.getError());
            }
            return estado.conLog("Ejecución completa en " + pasos + " pasos. Ciclos totales: "
                    + estado.ciclosReloj() + ", CPI: " + estado.cpi() + ".");
        }

        synchronized void reiniciar() {
            arquitectura = FabricaArquitectura.crear(config, Ensamblador.ensamblar(programa));
        }
    }

    // ------------------------------------------------------------------------------------------
    // Operaciones
    // ------------------------------------------------------------------------------------------

    /** @param comoPorDefecto si es true, también se registra como la sesión "default" (compatibilidad). */
    public RespuestaInicioDTO iniciar(SolicitudSimulacionDTO solicitud, boolean comoPorDefecto) {
        TipoArquitectura tipo = TipoArquitectura.desdeTexto(solicitud.tipo());
        ModoEjecucion modo = ModoEjecucion.desdeTexto(solicitud.modo());
        ConfiguracionSimulador config = construirConfiguracion(tipo, modo, solicitud);
        String programa = programaDe(solicitud, tipo);
        ArquitecturaBase arquitectura = FabricaArquitectura.crear(config, Ensamblador.ensamblar(programa));

        limpiarSesiones();
        Sesion sesion = new Sesion(config, programa, arquitectura);
        String id = UUID.randomUUID().toString();
        sesiones.put(id, sesion);
        if (comoPorDefecto) {
            sesiones.put(SESION_POR_DEFECTO, sesion);
        }
        return new RespuestaInicioDTO(id,
                "Simulador " + tipo.name() + " (" + modo.name() + ") iniciado correctamente",
                tipo.name(), modo.name(), programa);
    }

    public EstadoSimulacionDTO ejecutarPaso(String sesionId) {
        return obtener(sesionId).paso();
    }

    public EstadoSimulacionDTO estado(String sesionId) {
        return obtener(sesionId).estado();
    }

    public EstadoSimulacionDTO ejecutarTodo(String sesionId, int maxPasos) {
        if (maxPasos < 1 || maxPasos > MAX_PASOS_LIMITE) {
            throw new SolicitudInvalidaException("MAX_PASOS_INVALIDO",
                    "maxPasos debe estar entre 1 y " + MAX_PASOS_LIMITE + ".");
        }
        return obtener(sesionId).ejecutarTodo(maxPasos);
    }

    /** Vuelve al estado inicial de la misma simulación (mismo programa y configuración). */
    public EstadoSimulacionDTO reiniciar(String sesionId) {
        Sesion sesion = obtener(sesionId);
        sesion.reiniciar();
        return sesion.estado();
    }

    /**
     * Ejecuta el mismo programa hasta el final en las tres arquitecturas y los dos modos. Los campos tipo y modo de
     * la solicitud se ignoran. Si no se envía programa, se usa la suma (con las direcciones propias de cada arquitectura).
     */
    public List<ResultadoComparacionDTO> comparar(SolicitudSimulacionDTO solicitud) {
        ProgramaEnsamblado programaPropio = esVacio(solicitud.programa()) ? null : Ensamblador.ensamblar(solicitud.programa());

        record Medicion(TipoArquitectura tipo, ModoEjecucion modo, int ciclos, int instrucciones, double cpi,
                        int espera, boolean completado, String error, EstadisticasCacheDTO cache) {
        }

        List<Medicion> mediciones = new ArrayList<>();
        for (ModoEjecucion modo : ModoEjecucion.values()) {
            for (TipoArquitectura tipo : TipoArquitectura.values()) {
                try {
                    ConfiguracionSimulador config = construirConfiguracion(tipo, modo, solicitud);
                    ProgramaEnsamblado programa = programaPropio != null
                            ? programaPropio
                            : Ensamblador.ensamblar(programaDe(solicitud, tipo));
                    ArquitecturaBase arq = FabricaArquitectura.crear(config, programa);
                    int pasos = 0;
                    while (!arq.isFinalizado() && pasos < MAX_PASOS_POR_DEFECTO) {
                        arq.ejecutarPaso();
                        pasos++;
                    }
                    boolean completado = arq.isFinalizado() && arq.getError() == null;
                    String error = arq.getError();
                    if (!arq.isFinalizado()) {
                        error = "LIMITE_DE_PASOS: no terminó en " + MAX_PASOS_POR_DEFECTO + " pasos.";
                    }
                    mediciones.add(new Medicion(tipo, modo, arq.getCiclosReloj(), arq.getInstruccionesEjecutadas(),
                            arq.cpi(), arq.getCiclosEsperaRecurso(), completado, error, arq.getEstadisticasCache()));
                } catch (SolicitudInvalidaException e) {
                    mediciones.add(new Medicion(tipo, modo, 0, 0, 0.0, 0, false,
                            e.getCodigo() + ": " + e.getMessage(), null));
                }
            }
        }

        Map<ModoEjecucion, Integer> baseVonNeumann = new EnumMap<>(ModoEjecucion.class);
        for (Medicion m : mediciones) {
            if (m.tipo() == TipoArquitectura.VON_NEUMANN && m.completado()) {
                baseVonNeumann.put(m.modo(), m.ciclos());
            }
        }

        List<ResultadoComparacionDTO> filas = new ArrayList<>();
        for (Medicion m : mediciones) {
            Integer base = baseVonNeumann.get(m.modo());
            Double aceleracion = (base != null && m.completado() && m.ciclos() > 0)
                    ? Math.round(100.0 * base / m.ciclos()) / 100.0
                    : null;
            filas.add(new ResultadoComparacionDTO(m.tipo().getNombre(), m.modo().name(), m.ciclos(),
                    m.instrucciones(), m.cpi(), m.espera(), aceleracion, m.completado(), m.error(), m.cache()));
        }
        return filas;
    }

    public Map<String, String> ejemplos(String tipo, Integer valorA, Integer valorB) {
        TipoArquitectura t = TipoArquitectura.desdeTexto(tipo);
        return ProgramaPorDefecto.ejemplos(t,
                valorA != null ? valorA : VALOR_A_POR_DEFECTO,
                valorB != null ? valorB : VALOR_B_POR_DEFECTO);
    }

    // ------------------------------------------------------------------------------------------
    // Internos
    // ------------------------------------------------------------------------------------------

    private Sesion obtener(String sesionId) {
        String id = esVacio(sesionId) ? SESION_POR_DEFECTO : sesionId;
        Sesion sesion = sesiones.get(id);
        if (sesion == null) {
            if (SESION_POR_DEFECTO.equals(id)) {
                throw new SimuladorNoInicializadoException();
            }
            throw new SesionNoEncontradaException(id);
        }
        sesion.tocar();
        return sesion;
    }

    private ConfiguracionSimulador construirConfiguracion(TipoArquitectura tipo, ModoEjecucion modo,
                                                          SolicitudSimulacionDTO s) {
        ConfiguracionSimulador base = ConfiguracionSimulador.porDefecto(tipo, modo);
        return new ConfiguracionSimulador(tipo, modo,
                o(s.latenciaMemoria(), base.latenciaMemoria()),
                o(s.tiempoDecodificacion(), base.tiempoDecodificacion()),
                o(s.tiempoAlu(), base.tiempoAlu()),
                base.tiempoSalto(),
                o(s.anchoDatos(), base.anchoDatos()),
                base.tamMemoriaInstrucciones(),
                base.tamMemoriaDatos(),
                base.tamMemoriaUnificada(),
                o(s.lineasCache(), base.lineasCache()),
                base.latenciaCache());
    }

    private String programaDe(SolicitudSimulacionDTO s, TipoArquitectura tipo) {
        if (!esVacio(s.programa())) {
            return s.programa();
        }
        return ProgramaPorDefecto.para(tipo, o(s.valorA(), VALOR_A_POR_DEFECTO), o(s.valorB(), VALOR_B_POR_DEFECTO));
    }

    /** Elimina sesiones inactivas y, si aún hay demasiadas, las más antiguas. */
    private void limpiarSesiones() {
        long limite = System.currentTimeMillis() - inactividadMaximaMs;
        sesiones.entrySet().removeIf(e -> e.getValue().ultimoUsoMs < limite);
        while (!sesiones.isEmpty() && sesiones.size() >= maxSesiones) {
            sesiones.entrySet().stream()
                    .min((a, b) -> Long.compare(a.getValue().ultimoUsoMs, b.getValue().ultimoUsoMs))
                    .ifPresent(e -> sesiones.remove(e.getKey()));
        }
    }

    private static int o(Integer valor, int porDefecto) {
        return valor != null ? valor : porDefecto;
    }

    private static boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
