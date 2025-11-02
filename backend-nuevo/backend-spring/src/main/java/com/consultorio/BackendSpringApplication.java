package com.consultorio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.Arrays;

@SpringBootApplication
public class BackendSpringApplication {

    public static void main(String[] args) {
        System.out.println("=== INICIANDO SPRING BOOT ===");
        System.out.println("Argumentos: " + Arrays.toString(args));

        SpringApplication.run(BackendSpringApplication.class, args);

        System.out.println("=== SPRING BOOT INICIADO ===");
    }
}
