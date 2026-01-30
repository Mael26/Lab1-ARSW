/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

public class Main {

    public static void main(String args[]) throws InterruptedException {
        // Determinamos los núcleos para una de las pruebas del lab
        int núcleos = Runtime.getRuntime().availableProcessors();

        HostBlackListsValidator validator = new HostBlackListsValidator();

        // 1. Pausa inicial: Dale tiempo a VisualVM para detectar el proceso
        System.out.println("Esperando 5 segundos para conectar VisualVM");
        Thread.sleep(5000);

        // 2. Ejecución y toma de tiempo
        long startTime = System.currentTimeMillis();

        // Prueba
        validator.checkHost("202.24.34.55", 100);

        long endTime = System.currentTimeMillis();

        // 3. Resultado del experimento
        System.out.println("Tiempo de ejecución: " + (endTime - startTime) + "ms");

        System.out.println("Prueba terminada. Tienes 10 segundos adicionales para ver las gráficas.");
        Thread.sleep(10000);
    }
}