package com.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import sun.misc.Signal;

public class Proxy {
    // Permite manejar la ejecucion de los hilos
    private final ExecutorService ES = Executors.newCachedThreadPool();
    private static final int PORT = 8080;
    private ServerSocket server_socket;
    private int socket_port;

    public void atenderPeticiones() {
        /*
         * Esta función tiene como objetivo llevar a cabo el proceso de escucha de las
         * diferentes solicitudes, cuando llegue una solicitud, se creará un nuevo hilo,
         * en el cual se responderá a esta solicitud
         */
        Signal.handle(new Signal("INT"), // SIGINT
                signal -> {
                    System.out.println("\nApagando proxy...");
                    System.out.println("Por favor espere a que las peticiones realizadas hasta el momento sean resueltas");
                    this.ES.shutdown();
                    System.out.println("Adios...");
                    System.exit(0);
                });

        while (true) {
            try {
                // Acepto a un cliente que quiere enviarme datos, e inmediatamente redirijo esta
                // petición a otro hilo para que el Proxy pueda seguir funcionando con
                // normalidad
                Socket cliente = this.server_socket.accept();
                this.ES.execute(new Auxiliar(cliente));
            } catch (IOException e) {
                // TODO definir que hacer aqui (Fallo en la aceptación de la conexion)
                System.out.println(e.getMessage());
                // Espera a que los hilos hayan terminado su ejecución para acabar
                this.ES.shutdown();
                System.exit(0);
            }
        }
    }

    public Proxy() {
        try {
            this.server_socket = new ServerSocket(Proxy.PORT);
            this.socket_port = this.server_socket.getLocalPort();
            System.out.println("El proxy está corriendo en el puerto " + this.socket_port + " TCP");
        } catch (Exception e) {
            System.out.println(
                    "El proxy no pudo iniciar por \'" + e.getMessage() + "\'.\nPor favor intentelo nuevamente...");
            // Terminacion anomala de la ejecución del programa
            System.exit(1);
        }
    }
}
