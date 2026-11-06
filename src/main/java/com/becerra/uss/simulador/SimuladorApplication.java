package com.becerra.uss.simulador;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// punto de entrada principal de la aplicacion Spring Boot del simulador de memoria
@SpringBootApplication
public class SimuladorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimuladorApplication.class, args);
    }
}