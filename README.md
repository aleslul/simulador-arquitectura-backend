# Simulador de Arquitecturas de Computadoras (Backend)

Este repositorio contiene el backend del simulador de arquitecturas clásicas de computadoras (Von Neumann y Harvard). Está desarrollado en Java utilizando el framework Spring Boot. Este proyecto sirve como API para el frontend del simulador, encargado de calcular los estados de la memoria, los registros de la CPU y los ciclos de reloj.

## Características Principales

* Simulación de la Arquitectura Von Neumann (memoria unificada, cuello de botella de 2 ciclos por operación con datos).
* Simulación de la Arquitectura Harvard (memorias de datos e instrucciones separadas, paralelismo de 1 ciclo).
* Ejecución paso a paso de un programa en lenguaje ensamblador simulado (Suma de dos números: C = A + B).
* API REST para interactuar de forma aislada con la interfaz gráfica del simulador.

## Tecnologías Utilizadas

* Java 17
* Spring Boot 4.1.1
* Maven (Wrapper incluido)

## Estructura del Proyecto

El proyecto sigue una arquitectura de capas estándar de Spring Boot orientada a objetos:

* `controller/`: Contiene `SimuladorController`, que expone los endpoints REST y permite CORS.
* `service/`: Contiene `SimuladorService`, que maneja la lógica de sesión y mantiene la instancia activa de la simulación.
* `model/`: Contiene las clases núcleo del hardware (`CPU`, `Memoria`, `ArquitecturaBase`, `VonNeumann`, `Harvard`).
* `DTO/`: Contiene `EstadoSimulacionDTO`, utilizado para empaquetar y transferir el estado exacto de la máquina al frontend en cada ciclo.

## API Endpoints

La API está configurada con `@CrossOrigin("*")` para facilitar la integración directa con clientes web.

### 1. Iniciar Simulador
* **Método:** `POST`
* **Ruta:** `/api/simulador/iniciar`
* **Parámetros:** `tipo` (Valores aceptados: `VON_NEUMANN` o `HARVARD`)
* **Respuesta Exitosa:** `200 OK` (Ejemplo: "Simulador HARVARD iniciado correctamente").

### 2. Ejecutar Siguiente Paso
* **Método:** `GET`
* **Ruta:** `/api/simulador/paso`
* **Respuesta Exitosa:** `200 OK` (Retorna un JSON con la estructura de `EstadoSimulacionDTO` detallando PC, IR, Acumulador, estado de los arreglos de memoria y ciclos consumidos).

## Instrucciones de Ejecución

Para levantar el servidor localmente, asegúrese de tener Java 17 o superior instalado y ejecute el siguiente comando en la terminal desde la raíz del proyecto:

### En Windows
```bash
mvnw.cmd spring-boot:run
```

### En Linux/macOS
```bash
./mvnw spring-boot:run
```

El servidor Tomcat integrado se iniciará en el puerto `8080` y estará listo para recibir peticiones en `http://localhost:8080`.