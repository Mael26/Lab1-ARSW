/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.threads;

/**
 *
 * @author hcadavid
 */
public class CountThreadsMain {
    
    public static void main(String [] args){
        // Se definen los threads con sus rangos
        CountThread thread1 = new CountThread(0,99);
        CountThread thread2 = new CountThread(99,199);
        CountThread thread3 = new CountThread(200, 299);

        // Se da inicio a cada thread usando start
        // Al usar el metodo start se inicia un nuevo thread en estado runnable, en ese punto el sistema operativo comienza a
        // repartir cada nuevo thread en un nucleo distinto del procesador,
        // permitiendo que corran en paralelo de ser posible
        thread1.start();
        thread2.start();
        thread3.start();

        //Se da inicio a cada thread usando run.
        // Los threads son objetos runneables, lo que les permite heredar el metodo run de forma nativa,
        // a diferencia de llamar al metodo start, el metodo run ejecuta cada thread de forma sincrona, por lo cual
        // se terminan ejecutando en cola
//        thread1.run();
//        thread2.run();
//        thread3.run();
    }
    
}
