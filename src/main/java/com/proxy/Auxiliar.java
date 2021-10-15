package com.proxy;

//Estas importaciones se realizan para manejar los sockets
import java.net.Socket;

//Estas importaciones se realizan para poder leer el flujo de datos que se intenta enviar por el socket
import java.io.BufferedReader;
import java.io.InputStreamReader;

//Estas importaciones se realizan para poder realizar las peticiones HTTP
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpEntity;
import org.apache.http.Header;

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
                if (linea == null || linea.length() == 0)
                    fin = true;
                else
                    // Si el mensaje no tiene nada, únicamente inserto la linea, en caso contrario
                    // inserto un salto de linea y la linea
                    mensaje += (mensaje.length() > 0) ? "\n" + linea : linea;
            }
            lectura.close(); // Cierro el buffer de lectura actual
            // Separo la peticion para poder realizar diferentes operaciones con los datos
            String[] peticion = mensaje.split("\n");
            String[] nueva_peticion = new String[peticion.length + 2];
            String[] start = peticion[0].split(" ");
            System.arraycopy(start, 0, nueva_peticion, 0, start.length);
            System.arraycopy(peticion, 1, nueva_peticion, start.length, peticion.length - 1);
            peticion = null;
            start = null;
            String agent = null;
            for (String s : nueva_peticion)
                if (s.contains("User-Agent:"))
                    agent = s.split(" ")[2];
            String host = nueva_peticion[1];
            String method = nueva_peticion[0];
            // Por si viene especificado el puerto, no tener en cuenta el puerto
            host = host.split(":")[0];
            System.out.println("El url web que detecto es: " + host);
            if (!host.contains("http://"))
                host = "http://" + host;
            host.replace("https://", "http://");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            // TODO definir que hacer aca (Error con la lectura)
        }
    }

    // Propia de get
    private static String makeRequest(String host, String user_agent) {
        // Inicializo el resultado de la solicitud para devolverlo más adelante
        String result = null;
        // Inicializo la solicitud a realizar
        HttpGet request = new HttpGet(host);
        request.addHeader(HttpHeaders.USER_AGENT, user_agent);
        // Creo una instancia de cliente http para poder realizar la solicitud
        try (CloseableHttpClient httpClient = HttpClients.createDefault();) {
            // Creo una instancia de respuesta http para guardar el resultado de la
            // solicitud
            try (CloseableHttpResponse httpResponse = httpClient.execute(request);) {
                System.out.println(httpResponse.getStatusLine().toString());
                HttpEntity entity = httpResponse.getEntity();
                Header header = entity.getContentType();
                System.out.println(header);
                if (entity != null)
                    result = EntityUtils.toString(entity);
            } catch (Exception e) {
                // TODO Definir que hacer
            }
        } catch (Exception e) {
            // TODO Definir que hacer
        }
        return result;
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
