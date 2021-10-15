package com.proxy;

//Estas importaciones se realizan para manejar los sockets
import java.net.Socket;

//Estas importaciones se realizan para poder leer el flujo de datos que se intenta enviar por el socket
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;

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
            // Separo la peticion para poder realizar diferentes operaciones con los datos
            String[] peticion = mensaje.split("\n");
            String[] nueva_peticion = new String[peticion.length + 2];
            String[] start = peticion[0].split(" ");
            System.arraycopy(start, 0, nueva_peticion, 0, start.length);
            System.arraycopy(peticion, 1, nueva_peticion, start.length, peticion.length - 1);
            peticion = null;
            start = null;
            String user_agent = null;
            for (int i = 0; user_agent == null && i < nueva_peticion.length; ++i) {
                String s = nueva_peticion[i];
                if (s.contains("User-Agent"))
                    user_agent = s.split(" ")[1];
            }
            String host = nueva_peticion[1];
            String method = nueva_peticion[0];
            // Este es un regex para dividir el url por si tiene un puerto
            host=host.split(":[0-9]*")[0];
            // Por si viene especificado el puerto, no tener en cuenta el puerto
            if (!host.contains("http://"))
                host = "http://" + host;
            host.replace("https://", "http://");
            System.out.println("El url web que detecto es: " + host);
            if (method.toLowerCase().equals("get") || method.toLowerCase().equals("connect"))
                mensaje=getRequest(host, user_agent);
            else if (method.toLowerCase().equals("post"))
                mensaje="POST missing";
            DataOutputStream response=new DataOutputStream(this.cliente.getOutputStream());
            System.out.println("Necesito enviar\n");
            System.out.println(mensaje);
            response.flush();
            response.writeBytes(mensaje);
            System.out.println("\nDone\n\n");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            // TODO definir que hacer aca (Error con la lectura)
        }
    }

    // Propia de get
    private static String getRequest(String host, String user_agent) {
        // TODO Falta cambiar la version del cliente para que use http 1.0
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
                // Extraigo la entidad HTTP de la respuesta (La respuesta)
                HttpEntity entity = httpResponse.getEntity();
                Header header = entity.getContentType();
                System.out.println(header);
                // Si la entidad (Respuesta) no es nula, entonces convierto esta en lo que debo
                // enviar de
                // vuelta
                if (entity != null)
                    result = EntityUtils.toString(entity);
            } catch (Exception e) {
                // Para que no continue con el flujo normal de la función
                return result;
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
