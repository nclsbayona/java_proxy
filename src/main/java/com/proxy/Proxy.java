package com.proxy;

//JSON
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

//Estas importaciones se realizan para manejar los sockets
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Paths;
//Estas importaciones se realizan para administrar los threads que están en ejecución
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Esta importación se realiza para definir un tiempo de espera para esperar a la finalización de los threads
import java.util.concurrent.TimeUnit;

//Esta importacion se realiza para manejar la señal de terminación del programa
import sun.misc.Signal;

public class Proxy {
    private static final String VIRTUAL_HOSTS_FILE = "hosts.json";
    private static final String DENIED_HOSTS_FILE = "denied.json";
    private static HashMap<String, VirtualHost> vHosts;
    private static HashSet<String> dPages;
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
                try {
                    cliente.setKeepAlive(true);
                } catch (SocketException e) {
                }
                this.ES.execute(new Auxiliar(cliente));
            } catch (Exception e) {
                // Descomentar para ver que pasa System.out.println("Ocurrio \'" +
                // e.getMessage() + "\'");
                // TODO definir que hacer aqui (Fallo en la aceptación de la conexion)
                this.esperarFinalizacion();
                // Terminacion anomala de la ejecución del programa
                System.exit(1);
            }
        }
    }

    private static HashMap<String, VirtualHost> getVirtualHosts() {
        HashMap<String, VirtualHost> hosts = new HashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            VirtualHost[] vHosts = mapper.readValue(
                    Paths.get(Proxy.class.getResource(Proxy.VIRTUAL_HOSTS_FILE).toURI()).toFile(), VirtualHost[].class);
            for (int i = 0; i < vHosts.length; ++i)
                hosts.put(vHosts[i].getVirtual_host(), vHosts[i]);
        } catch (Exception ex) {
        }
        return hosts;
    }

    private static HashSet<String> getDeniedPages() {
        HashSet<String> pages = new HashSet<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<?, ?> map = mapper
                    .readValue(Paths.get(Proxy.class.getResource(Proxy.DENIED_HOSTS_FILE).toURI()).toFile(), Map.class);
            String[] s = (map.values().toArray()[0]).toString().replace("[", "").replace("]", "").split(", ");
            for (int i = 0; i < s.length; ++i)
                pages.add(s[i]);
        } catch (Exception ex) {
        }
        return pages;
    }

    public Proxy() {
        try {
            Proxy.vHosts = Proxy.getVirtualHosts();
            Proxy.dPages = Proxy.getDeniedPages();
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

    @Override
    public String toString() {
        return "{" + " vHosts=" + vHosts.toString() + ", " + " dPages=" + dPages.toString() + "}";
    }

}
