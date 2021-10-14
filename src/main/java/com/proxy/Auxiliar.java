package com.proxy;

//Estas importaciones se realizan para manejar los sockets
import java.net.Socket;
//Estas importaciones se realizan para poder leer el flujo de datos que se intenta enviar por el socket
import java.io.BufferedReader;
import java.io.InputStreamReader;

// Añadiendo el soporte para multi-hilos
public class Auxiliar extends Thread {
    private Socket cliente;

    private void resolverPeticion() {
        String mensaje = "";
        String linea;
        // cliente.getInputStream() --> Obtiene en bytes el mensaje que el cliente está
        // enviando
        // InputStreamReader --> Clase encargada de transformar bytes en caracteres
        // siguiendo un 'charset' especifico
        // BufferedReader --> Clase que proporciona una forma simple y eficiente de leer
        // caracteres (Por lineas)
        try {
            BufferedReader lectura = new BufferedReader(new InputStreamReader(this.cliente.getInputStream()));
            boolean fin = false;
            while (!fin) {
                // Leo todas las lineas del mensaje que intenta enviar el cliente
                linea = lectura.readLine();
                // Si la linea está vacia, no la meto en el mensaje y ya acabe, en caso
                // contrario la añado al mensaje
                if (linea.length() == 0)
                    fin = true;
                else
                    // Si el mensaje no tiene nada, únicamente inserto la linea, en caso contrario
                    // inserto un salto de linea y la linea
                    mensaje += (mensaje.length() > 0) ? "\n" + linea : linea;
            }
            // Separo la peticion para poder realizar diferentes operaciones con los datos
            String[] peticion = mensaje.split("\n");
            String[] nueva_peticion = new String[peticion.length + 2];
            String[] start = peticion[0].split(" ");
            System.arraycopy(start, 0, nueva_peticion, 0, start.length);
            System.arraycopy(peticion, 1, nueva_peticion, start.length, peticion.length - 1);
            peticion = null;
            start = null;
            for (String s : nueva_peticion)
                System.out.println(s);
            System.out.println();
            /* Ejemplos
            GET http://myblog.com/hola/vpn HTTP/1.1
            Host: myblog.com
            Proxy-Connection: keep-alive
            Upgrade-Insecure-Requests: 1
            User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.71 Safari/537.36 OPR/80.0.4170.40
            Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng;q=0.8,application/signed-exchange;v=b3;q=0.9
            Accept-Encoding: gzip, deflate
            Accept-Language: en-US,en;q=0.9
            y
            CONNECT wiki.ubuntu.com:443 HTTP/1.1 
            User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:93.0) Gecko/20100101 Firefox/93.0
            Proxy-Connection: keep-alive 
            Connection: keep-alive 
            Host: wiki.ubuntu.com:443
            */
        } catch (Exception e) {
            System.out.println(e.getMessage());
            // TODO definir que hacer aca (Error con la lectura)
        }
    }

    @Override
    public void run() {
        this.resolverPeticion();
    }

    public Auxiliar(Socket cliente) {
        super();
        this.cliente = cliente;
    }
}
