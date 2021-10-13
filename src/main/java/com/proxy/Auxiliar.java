package com.proxy;

import java.net.Socket;
import java.util.Objects;
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
                    mensaje += (mensaje.length() > 0) ? "\n" + linea : linea;
            }
            System.out.println("Servidor acaba de recibir \'" + mensaje + "\' desde: " + this.cliente.toString());
        } catch (Exception e) {
            // TODO definir que hacer aca (Error con la lectura)
        }
    }

    @Override
    public void run() {
        this.resolverPeticion();
    }


    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Auxiliar)) {
            return false;
        }
        Auxiliar auxiliar = (Auxiliar) o;
        return Objects.equals(cliente, auxiliar.cliente);
    }

    public Auxiliar(Socket cliente) {
        this.cliente = cliente;
    }
}
