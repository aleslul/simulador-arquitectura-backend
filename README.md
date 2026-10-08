# Simulador de Arquitecturas de Computadoras (Backend)

Backend de un simulador didáctico de las arquitecturas **Von Neumann**, **Harvard** y
**Harvard modificada** (cachés L1 separadas sobre una RAM unificada, como las CPU actuales). Calcula, ciclo a ciclo,
los registros de la CPU, la memoria, las banderas y el uso de buses/caché, y permite comparar las arquitecturas
ejecutando el mismo programa.

## Qué modela

| Aspecto | Modelo |
|---|---|
| ISA | `HALT, LOAD, ADD, STORE, SUB, CMP, JMP, JZ` (palabra de 16 bits: 4 de opcode + 12 de operando) |
| Registros | PC, IR, ACC, MAR, MDR y banderas Z, N, C (acarreo/préstamo), V (desbordamiento) |
| ALU | Complemento a dos con ancho configurable (8–32 bits en datos de Harvard pura; 16 en las demás) |
| Fases | FETCH → DECODE → MEM_LECTURA / ALU / MEM_ESCRITURA / SALTO, cada una con ciclo de inicio y fin |
| Von Neumann | Una memoria y **un solo bus**: fetch y datos no pueden solaparse. Un `STORE` puede sobrescribir código |
| Harvard | Memorias y buses separados de instrucciones y datos. El código no se puede modificar |
| Harvard modificada | L1I + L1D (LRU, write-through) sobre una RAM única; fallos de ambas cachés compiten por la RAM |
| Modo `SECUENCIAL` | Una instrucción termina antes de empezar el fetch de la siguiente. **Harvard no gana nada** |
| Modo `SEGMENTADO` | 2 etapas (Fetch ‖ Execute). El fetch siguiente se solapa con la ejecución; con un solo bus aparecen esperas |

Con los tiempos por defecto (RAM = 3 ciclos, decodificación = 1, ALU = 1) el programa `C = A + B` da:

| Arquitectura | Secuencial | Segmentado | Ciclos perdidos esperando bus (segmentado) |
|---|---|---|---|
| Von Neumann | 26 | 25 | 12 |
| Harvard | 26 | 17 | 0 |
| Harvard modificada | 32 | 31 | 14 |

La Harvard modificada pierde aquí porque el código es lineal (todo son fallos "en frío"). Con el bucle de
multiplicación (`/ejemplos`) sí gana: 266 (Von Neumann) → 183 (Harvard) → 164 (Harvard modificada) en modo segmentado.

### Simplificaciones declaradas
- Tras un salto tomado, el siguiente fetch espera a que el salto termine (el fetch descartado no ocupa recursos).
- Coherencia ideal: una instrucción prefetcheada ve los `STORE` de las anteriores.
- Cachés de líneas de una palabra, totalmente asociativas.

## Estructura

```
controller/  SimuladorController, GlobalExceptionHandler
service/     SimuladorService      sesiones independientes (UUID), pasos, ejecutar-todo, reset, comparar
config/      WebConfig             CORS configurable
model/       ArquitecturaBase      ciclo fetch/decode/execute y temporización (Template Method)
             VonNeumann, Harvard, HarvardModificada   solo definen dónde están los datos y cuánto cuesta acceder
             Ensamblador, ProgramaPorDefecto, Instruccion, Opcode, Alu, Flags, CPU, Memoria, Cache, Recurso, ...
DTO/         EstadoSimulacionDTO (record) y demás
exception/   excepciones de dominio
```

## API

Todas las rutas cuelgan de `/api/simulador`. `sesionId` es opcional: si se omite se usa la última simulación creada con
`/iniciar` (compatibilidad con el cliente original).

| Método y ruta | Descripción |
|---|---|
| `POST /iniciar?tipo=&modo=&valorA=&valorB=` | Inicia con el programa por defecto. `tipo`: `VON_NEUMANN`, `HARVARD`, `HARVARD_MODIFICADA`; `modo`: `SECUENCIAL`, `SEGMENTADO` (por defecto) |
| `POST /sesiones` | Inicia con cuerpo JSON (programa propio y parámetros de hardware, ver abajo) |
| `POST /paso?sesionId=` | Ejecuta una instrucción (`GET /paso` sigue existiendo pero está **deprecado**) |
| `GET /estado?sesionId=` | Estado actual sin avanzar |
| `POST /ejecutar-todo?sesionId=&maxPasos=10000` | Ejecuta hasta HALT, fallo o límite de pasos |
| `POST /reset?sesionId=` | Vuelve al estado inicial (mismo programa y configuración) |
| `GET /comparar?valorA=&valorB=` / `POST /comparar` | Mismo programa en las 3 arquitecturas × 2 modos: ciclos, CPI, esperas y aceleración respecto a Von Neumann |
| `GET /ejemplos?tipo=` | Programas de ejemplo: `SUMA`, `MULTIPLICACION` (bucle), `AUTOMODIFICABLE` |

Cuerpo de `POST /sesiones` (todo opcional salvo `tipo`):

```json
{
  "tipo": "HARVARD_MODIFICADA",
  "modo": "SEGMENTADO",
  "programa": "INICIO: LOAD 10\n JZ FIN\n SUB 12\n STORE 10\n JMP INICIO\nFIN: HALT\n.DATO 10 5\n.DATO 12 1",
  "latenciaMemoria": 3, "tiempoDecodificacion": 1, "tiempoAlu": 1, "anchoDatos": 16, "lineasCache": 8
}
```

### Ensamblador
```
; comentario (también // o #)
INICIO: LOAD 10     ; etiqueta + instrucción; los saltos aceptan etiqueta o número
        ADD 11
        JZ FIN
FIN:    HALT
.DATO 10 5          ; MEM_DATOS[10] = 5  (en Von Neumann / Harvard modificada: memoria única)
```
Las instrucciones se cargan desde la dirección 0. En Von Neumann y Harvard modificada los datos no pueden pisar el código.

### Respuesta de estado (campos principales)
`arquitectura, modo, pc, ir, acumulador, mar, mdr, flags{z,n,c,v}, ciclosReloj, ciclosPaso, instruccionesEjecutadas, cpi,
ciclosEsperaRecurso, finalizado, error, longitudPrograma, memoriaPrincipal | memoriaInstrucciones + memoriaDatos,
desensamblado, logOperacion, eventos[{fase, recurso, cicloInicio, cicloFin, direccion, dato, detalle}], cache`.

Si la máquina simulada sufre un fallo (dirección fuera de rango, opcode inválido, PC fuera de la memoria) la respuesta es
`200 OK` con `finalizado=true` y `error="CODIGO: mensaje"`; no hay errores 500.

## Ejecutar
```bash
./mvnw spring-boot:run        # Linux/macOS
mvnw.cmd spring-boot:run      # Windows
./mvnw test
```
