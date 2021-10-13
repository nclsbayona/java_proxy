package com.proxy;

import java.net.ServerSocket;
import java.net.Socket;

public class Proxy{
    private static final int PORT=8080;
    private ServerSocket server_socket;
    private int socket_port;

    public void atenderPeticiones(){
        /* Esta función tiene como objetivo llevar a cabo el proceso de escucha de las diferentes solicitudes, cuando llegue una solicitud, se creará un nuevo hilo, en el cual se responderá a esta solicitud */
        while (true){
            try {
                //Acepto a un cliente que quiere enviarme datos, e inmediatamente redirijo esta petición a otro hilo para que el Proxy pueda seguir funcionando con normalidad
                Socket cliente=this.server_socket.accept();
                Auxiliar aux=new Auxiliar(cliente);
                aux.start();
            } catch (Exception e) {
                //TODO definir que hacer aqui (Fallo en la aceptación de la conexion)
            }
        }
    }

    public Proxy(){
        try {
            this.server_socket=new ServerSocket(Proxy.PORT);
            this.socket_port=this.server_socket.getLocalPort();
            System.out.println("El proxy está corriendo en el puerto "+this.socket_port+" TCP");
        } catch (Exception e) {
            System.out.println("El proxy no pudo iniciar por \'"+e.getMessage()+"\'.\nPor favor intentelo nuevamente...");
            //Terminacion anomala de la ejecución del programa
            System.exit(1);
        }
    }
}
