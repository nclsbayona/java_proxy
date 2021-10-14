package com.proxy;

//Estas importaciones se realizan para manejar los sockets
import java.net.ServerSocket;
import java.net.Socket;
//Estas importaciones se realizan para administrar los threads que están en ejecución
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//Esta importación se realiza para definir un tiempo de espera para esperar a la finalización de los threads
import java.util.concurrent.TimeUnit;
//Esta importacion se realiza para manejar la señal de terminación del programa
import sun.misc.Signal;

public class Proxy {
    // Permite manejar la ejecucion de los hilos
    private final ExecutorService ES = Executors.newCachedThreadPool();
    private static final int PORT = 8080;
    private ServerSocket server_socket;
    private int socket_port;

    private void esperarFinalizacion() {
        // Este metodo define que no se recibiran más llamadas al proxy
        this.ES.shutdown();
        try {
            // Este metodo me permite esperar la finalización de la ejecución de los
            // diferentes hilos, aquí se está esperando y cada milisegundo revisa si ya se
            // terminó la ejecución para poder devolver el flujo al punto de terminación
            while (!this.ES.awaitTermination(1, TimeUnit.MILLISECONDS))
                ;
            return;
        } catch (InterruptedException e) {
            // Interrumpir todo
            this.ES.shutdownNow();
            // Interrumpir el proceso
            Thread.currentThread().interrupt();
            // Terminacion anomala de la ejecución del programa
            System.exit(1);
        }
    }

    public void atenderPeticiones() {
        /*
         * Esta función tiene como objetivo llevar a cabo el proceso de escucha de las
         * diferentes solicitudes, cuando llegue una solicitud, se creará un nuevo hilo,
         * en el cual se responderá a esta solicitud
         */
        // Define la forma en la que se maneja una señal de SIGINT, es decir cuando se
        // desea terminar el proceso por medio de ctrl+c
        Signal.handle(new Signal("INT"), // SIGINT
                signal -> {
                    System.out.println("\nApagando proxy...");
                    System.out.println(
                            "Por favor espere a que las peticiones realizadas hasta el momento sean resueltas");
                    this.esperarFinalizacion();
                    System.out.println("Adios...");
                    System.exit(0);
                });

        while (true) {
            try {
                // Acepto a un cliente que quiere enviarme datos, e inmediatamente redirijo esta
                // petición a otro hilo para que el Proxy pueda seguir funcionando con
                // normalidad. Adicionalmente
                Socket cliente = this.server_socket.accept();
                this.ES.execute(new Auxiliar(cliente));
            } catch (Exception e) {
                System.out.println("Ocurrio \'" + e.getMessage() + "\'");
                // TODO definir que hacer aqui (Fallo en la aceptación de la conexion)
                this.esperarFinalizacion();
                // Terminacion anomala de la ejecución del programa
                System.exit(1);
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
