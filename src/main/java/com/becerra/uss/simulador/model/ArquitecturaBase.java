package com.becerra.uss.simulador.model;

import com.becerra.uss.simulador.DTO.EstadisticasCacheDTO;
import com.becerra.uss.simulador.DTO.EstadoSimulacionDTO;
import com.becerra.uss.simulador.DTO.EventoFaseDTO;
import com.becerra.uss.simulador.DTO.FlagsDTO;
import com.becerra.uss.simulador.exception.FalloEjecucionException;
import com.becerra.uss.simulador.exception.SolicitudInvalidaException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// clase base que implementa el ciclo fetch decode execute y la temporizacion de recursos
public abstract class ArquitecturaBase {

    protected final ConfiguracionSimulador config;
    protected final CPU cpu = new CPU();

    private int ciclosReloj;
    private int instruccionesEjecutadas;
    private int ciclosEsperaRecurso;
    private int inicioEjecucionAnterior;
    private int finEjecucionAnterior;
    private boolean saltoTomadoAnterior;
    private boolean finalizado;
    private String error;
    private int longitudPrograma;
    private int ultimoFinEvento;

    protected ArquitecturaBase(ConfiguracionSimulador config) {
        this.config = config;
    }

    protected abstract int leerInstruccion(int direccion);

    protected abstract int leerDato(int direccion);

    protected abstract void escribirDato(int direccion, int valor);

    protected abstract Acceso accesoInstruccion(int direccion, int solicitud);

    protected abstract Acceso accesoDato(int direccion, boolean escritura, int solicitud);

    protected abstract int[] memoriaPrincipal();

    protected abstract int[] memoriaInstrucciones();

    protected abstract int[] memoriaDatos();

    protected EstadisticasCacheDTO estadisticasCache() {
        return null;
    }

    // ejecuta una instruccion completa calculando los ciclos de reloj de cada fase
    public final EstadoSimulacionDTO ejecutarPaso() {
        if (finalizado) {
            return construirEstado(error != null
                    ? "La maquina esta detenida por un fallo: " + error
                    : "Programa finalizado", List.of(), 0);
        }

        final int ciclosAntes = ciclosReloj;
        final List<EventoFaseDTO> eventos = new ArrayList<>();
        ultimoFinEvento = ciclosReloj;
        Instruccion instruccion = null;

        try {
            // fase de busqueda de instruccion en memoria o cache
            final int pc = cpu.getPc();
            final int solicitudFetch = (config.modo() == ModoEjecucion.SECUENCIAL || saltoTomadoAnterior)
                    ? finEjecucionAnterior
                    : inicioEjecucionAnterior;
            final int palabra = capturarInstruccion(pc);
            final Acceso fetch = accesoInstruccion(pc, solicitudFetch);
            int esperaFetch = fetch.inicio() - solicitudFetch;
            if (esperaFetch > 0) {
                ciclosEsperaRecurso += esperaFetch;
            }
            cpu.setMar(pc);
            cpu.setMdr(palabra);
            registrar(eventos, "FETCH", fetch.recurso(), fetch.inicio(), fetch.fin(), pc, palabra,
                    "Lee la instruccion de la direccion " + pc + detalleCache(fetch)
                            + esperaTexto(esperaFetch));

            // fase de decodificacion de la instruccion en la unidad de control
            final int inicioEjecucion = Math.max(fetch.fin(), finEjecucionAnterior);
            int t = inicioEjecucion + config.tiempoDecodificacion();
            ultimoFinEvento = Math.max(ultimoFinEvento, t);
            instruccion = Instruccion.decodificar(palabra);
            cpu.setIr(instruccion.toString());
            if (config.tiempoDecodificacion() > 0) {
                registrar(eventos, "DECODE", "UNIDAD_CONTROL", inicioEjecucion, t, null, palabra,
                        "Decodifica " + instruccion);
            }

            // fase de ejecucion segun el codigo de operacion
            final Opcode op = instruccion.opcode();
            final int dir = instruccion.operando();
            boolean saltoTomado = false;

            switch (op) {
                case HALT -> finalizado = true;

                case LOAD -> {
                    int valor = leerDato(dir);
                    Acceso a = accesoDato(dir, false, t);
                    int esperaLoad = a.inicio() - t;
                    if (esperaLoad > 0) {
                        ciclosEsperaRecurso += esperaLoad;
                    }
                    cpu.setMar(dir);
                    cpu.setMdr(valor);
                    cpu.setAcumulador(valor);
                    cpu.getFlags().actualizarZN(valor);
                    registrar(eventos, "MEM_LECTURA", a.recurso(), a.inicio(), a.fin(), dir, valor,
                            "ACC <- MEM[" + dir + "] = " + valor + detalleCache(a) + esperaTexto(esperaLoad));
                    t = a.fin();
                }

                case ADD, SUB, CMP -> {
                    int valor = leerDato(dir);
                    Acceso a = accesoDato(dir, false, t);
                    int esperaAlu = a.inicio() - t;
                    if (esperaAlu > 0) {
                        ciclosEsperaRecurso += esperaAlu;
                    }
                    cpu.setMar(dir);
                    cpu.setMdr(valor);
                    registrar(eventos, "MEM_LECTURA", a.recurso(), a.inicio(), a.fin(), dir, valor,
                            "Lee MEM[" + dir + "] = " + valor + detalleCache(a) + esperaTexto(esperaAlu));
                    t = a.fin();

                    int bits = config.anchoAlu();
                    Alu.Resultado r = (op == Opcode.ADD)
                            ? Alu.sumar(cpu.getAcumulador(), valor, bits)
                            : Alu.restar(cpu.getAcumulador(), valor, bits);
                    cpu.getFlags().actualizar(r);
                    if (op != Opcode.CMP) {
                        cpu.setAcumulador(r.valor());
                    }
                    int finAlu = t + config.tiempoAlu();
                    if (config.tiempoAlu() > 0) {
                        registrar(eventos, "ALU", "ALU", t, finAlu, null, r.valor(),
                                op + ": resultado " + r.valor() + " (Z=" + bit(r.z()) + " N=" + bit(r.n())
                                        + " C=" + bit(r.c()) + " V=" + bit(r.v()) + ")");
                    }
                    t = finAlu;
                }

                case STORE -> {
                    int valor = cpu.getAcumulador();
                    escribirDato(dir, valor);
                    Acceso a = accesoDato(dir, true, t);
                    int esperaStore = a.inicio() - t;
                    if (esperaStore > 0) {
                        ciclosEsperaRecurso += esperaStore;
                    }
                    cpu.setMar(dir);
                    cpu.setMdr(valor);
                    registrar(eventos, "MEM_ESCRITURA", a.recurso(), a.inicio(), a.fin(), dir, valor,
                            "MEM[" + dir + "] <- ACC = " + valor + " [Write-Through L1D->L2->RAM]" + esperaTexto(esperaStore));
                    t = a.fin();
                }

                case JMP, JZ -> {
                    saltoTomado = (op == Opcode.JMP) || cpu.getFlags().isZ();
                    int finSalto = t + config.tiempoSalto();
                    if (config.tiempoSalto() > 0) {
                        registrar(eventos, "SALTO", "UNIDAD_CONTROL", t, finSalto, dir, null,
                                saltoTomado ? "Salto tomado: PC <- " + dir
                                        : "Salto NO tomado (Z=0): continua en PC+1");
                    }
                    if (saltoTomado) {
                        cpu.setPc(dir);
                    }
                    t = finSalto;
                }
            }

            // actualiza el contador de programa y los contadores globales del paso
            if (!finalizado && !saltoTomado) {
                cpu.incrementarPc();
            }
            inicioEjecucionAnterior = inicioEjecucion;
            finEjecucionAnterior = t;
            saltoTomadoAnterior = saltoTomado;
            ciclosReloj = Math.max(ciclosReloj, t);
            instruccionesEjecutadas++;

            int ciclosPaso = ciclosReloj - ciclosAntes;
            return construirEstado(describirPaso(instruccion, eventos, ciclosPaso), eventos, ciclosPaso);

        } catch (FalloEjecucionException e) {
            finalizado = true;
            error = e.getCodigo() + ": " + e.getMessage();
            ciclosReloj = Math.max(ciclosReloj, ultimoFinEvento);
            int ciclosPaso = ciclosReloj - ciclosAntes;
            return construirEstado("FALLO [" + e.getCodigo() + "]: " + e.getMessage()
                    + " La maquina se detuvo en el PC=" + cpu.getPc(), eventos, ciclosPaso);
        }
    }

    public final EstadoSimulacionDTO estadoActual() {
        String log = finalizado
                ? (error != null ? "La maquina esta detenida por un fallo: " + error : "Programa finalizado")
                : "Estado inicial listo para ejecutar";
        return construirEstado(log, List.of(), 0);
    }

    public final boolean isFinalizado() { return finalizado; }
    public final String getError() { return error; }
    public final int getCiclosReloj() { return ciclosReloj; }
    public final int getInstruccionesEjecutadas() { return instruccionesEjecutadas; }
    public final int getCiclosEsperaRecurso() { return ciclosEsperaRecurso; }
    public final ConfiguracionSimulador getConfig() { return config; }
    public final EstadisticasCacheDTO getEstadisticasCache() { return estadisticasCache(); }

    public final double cpi() {
        return instruccionesEjecutadas == 0 ? 0.0 : Math.round(100.0 * ciclosReloj / instruccionesEjecutadas) / 100.0;
    }

    // escribe las instrucciones y datos iniciales en una memoria unificada
    protected final void cargarEnMemoriaUnificada(ProgramaEnsamblado programa, Memoria memoria) {
        int n = programa.palabras().length;
        if (n > memoria.getTamano()) {
            throw new SolicitudInvalidaException("PROGRAMA_DEMASIADO_GRANDE",
                    "El programa tiene " + n + " instrucciones y la memoria solo tiene " + memoria.getTamano() + " posiciones");
        }
        for (int i = 0; i < n; i++) {
            memoria.escribir(i, programa.palabras()[i]);
        }
        for (Map.Entry<Integer, Integer> d : programa.datos().entrySet()) {
            int direccion = d.getKey();
            if (direccion >= memoria.getTamano()) {
                throw new SolicitudInvalidaException("DATO_FUERA_DE_RANGO",
                        "La direccion de dato " + direccion + " no existe en la memoria");
            }
            if (direccion < n) {
                throw new SolicitudInvalidaException("DATO_SOBRE_CODIGO",
                        "La direccion de dato " + direccion + " pisa el codigo del programa");
            }
            validarValor(direccion, d.getValue(), memoria.getBits());
            memoria.escribir(direccion, d.getValue());
        }
        longitudPrograma = n;
    }

    // escribe las instrucciones y datos en memorias separadas para Harvard pura
    protected final void cargarEnHarvard(ProgramaEnsamblado programa, Memoria instrucciones, Memoria datos) {
        int n = programa.palabras().length;
        if (n > instrucciones.getTamano()) {
            throw new SolicitudInvalidaException("PROGRAMA_DEMASIADO_GRANDE",
                    "El programa supera el tamano de la memoria de instrucciones");
        }
        for (int i = 0; i < n; i++) {
            instrucciones.escribir(i, programa.palabras()[i]);
        }
        for (Map.Entry<Integer, Integer> d : programa.datos().entrySet()) {
            int direccion = d.getKey();
            if (direccion >= datos.getTamano()) {
                throw new SolicitudInvalidaException("DATO_FUERA_DE_RANGO",
                        "La direccion de dato " + direccion + " supera la memoria de datos");
            }
            validarValor(direccion, d.getValue(), datos.getBits());
            datos.escribir(direccion, d.getValue());
        }
        longitudPrograma = n;
    }

    private static void validarValor(int direccion, int valor, int bits) {
        long min = -(1L << (bits - 1));
        long max = (1L << bits) - 1;
        if (valor < min || valor > max) {
            throw new SolicitudInvalidaException("VALOR_FUERA_DE_RANGO",
                    "El valor " + valor + " no cabe en una palabra de " + bits + " bits");
        }
    }

    protected static Acceso reservar(Recurso recurso, int solicitud, int duracion, Boolean acierto) {
        int inicio = recurso.inicioDisponible(solicitud);
        int fin = inicio + duracion;
        recurso.ocupar(fin);
        return new Acceso(inicio, fin, recurso.getNombre(), acierto);
    }

    private int capturarInstruccion(int pc) {
        try {
            return leerInstruccion(pc);
        } catch (FalloEjecucionException e) {
            throw new FalloEjecucionException("PC_FUERA_DE_RANGO",
                    "El PC=" + pc + " apunta fuera de la memoria de instrucciones");
        }
    }

    private void registrar(List<EventoFaseDTO> eventos, String fase, String recurso, int inicio, int fin,
                           Integer direccion, Integer dato, String detalle) {
        eventos.add(new EventoFaseDTO(fase, recurso, inicio, fin, direccion, dato, detalle));
        ultimoFinEvento = Math.max(ultimoFinEvento, fin);
    }

    // devuelve un texto descriptivo indicando en que nivel de cache hubo acierto o fallo
    private static String detalleCache(Acceso a) {
        if (a.acierto() == null) {
            return "";
        }
        if (a.acierto()) {
            return " [Hit L1]";
        }
        if (a.recurso() != null && a.recurso().endsWith("->L2")) {
            return " [Miss L1 -> Hit L2]";
        }
        return " [Miss L1 y L2 -> RAM]";
    }

    private static String esperaTexto(int espera) {
        return espera > 0 ? " (espero " + espera + " ciclos por recurso ocupado)" : "";
    }

    private static String bit(boolean valor) {
        return valor ? "1" : "0";
    }

    private String describirPaso(Instruccion instruccion, List<EventoFaseDTO> eventos, int ciclosPaso) {
        StringBuilder sb = new StringBuilder(instruccion.toString()).append(": ");
        if (instruccion.opcode() == Opcode.HALT) {
            sb.append("fin de ejecucion ");
        }
        for (int i = 0; i < eventos.size(); i++) {
            EventoFaseDTO e = eventos.get(i);
            if (i > 0) {
                sb.append(" -> ");
            }
            sb.append(e.fase()).append(" (").append(e.recurso()).append(") [")
                    .append(e.cicloInicio()).append('-').append(e.cicloFin()).append(']');
        }
        sb.append(" | Paso: +").append(ciclosPaso).append(" ciclos (Total: ").append(ciclosReloj).append(")");
        return sb.toString();
    }

    private EstadoSimulacionDTO construirEstado(String log, List<EventoFaseDTO> eventos, int ciclosPaso) {
        int[] principal = memoriaPrincipal();
        int[] instrucciones = memoriaInstrucciones();
        int[] datos = memoriaDatos();

        int[] fuenteCodigo = principal != null ? principal : instrucciones;
        String[] desensamblado = new String[fuenteCodigo.length];
        for (int i = 0; i < Math.min(longitudPrograma, fuenteCodigo.length); i++) {
            desensamblado[i] = Instruccion.desensamblar(fuenteCodigo[i]);
        }

        Flags f = cpu.getFlags();
        return new EstadoSimulacionDTO(
                config.tipo().getNombre(),
                config.modo().name(),
                cpu.getPc(),
                cpu.getIr(),
                cpu.getAcumulador(),
                cpu.getMar(),
                cpu.getMdr(),
                new FlagsDTO(f.isZ(), f.isN(), f.isC(), f.isV()),
                ciclosReloj,
                ciclosPaso,
                instruccionesEjecutadas,
                cpi(),
                ciclosEsperaRecurso,
                finalizado,
                error,
                longitudPrograma,
                principal,
                instrucciones,
                datos,
                desensamblado,
                log,
                List.copyOf(eventos),
                estadisticasCache());
    }
}