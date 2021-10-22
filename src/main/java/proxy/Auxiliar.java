package proxy;

//Estas importaciones se realizan para manejar los sockets
import java.net.Socket;

//Estas importaciones se realizan para poder leer el flujo de datos que se intenta enviar por el socket
import java.io.BufferedReader;
import java.io.InputStreamReader;

//Estas importaciones se realizan para poder escribir em el flujo de salida los datos que se recuperaron de la solicitud
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

//Guardar paginas bloqueadas y virtual hosts
import java.util.HashMap;
import java.util.HashSet;

//Estas importaciones se realizan para poder realizar las peticiones HTTP
//Primero aquellos usados para el GET request (Puede no ser exclusivo del GET)
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpEntity;
//Posteriormente aquellos usados únicamente para el POST
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

//Importo ArrayList para poder guardar los parametros de un post request
import java.util.ArrayList;

// Añadiendo el soporte para multi-hilos
public class Auxiliar extends Thread {
    private Socket cliente;
    private HashMap<String, VirtualHost> vHosts;
    private HashSet<String> dPages;

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
                if (linea == null || linea.length() == 0 || linea.equals(""))
                    fin = true;
                else {
                    // Si el mensaje no tiene nada, únicamente inserto la linea, en caso contrario
                    // inserto un salto de linea y la linea
                    mensaje += (mensaje.length() > 0) ? "\r\n" + linea : linea;
                }
            }
            // Separo la peticion para poder realizar diferentes operaciones con los datos
            String[] peticion = mensaje.split("\n");
            String[] nueva_peticion = new String[peticion.length + 2];
            String[] start = peticion[0].split(" ");
            System.arraycopy(start, 0, nueva_peticion, 0, start.length);
            System.arraycopy(peticion, 1, nueva_peticion, start.length, peticion.length - 1);
            peticion = null;
            start = null;
            String host = nueva_peticion[1];
            String method = nueva_peticion[0];
            logWarnMessage("El cliente en " + this.cliente.getInetAddress().getCanonicalHostName().toString() + ":"
                    + this.cliente.getPort() + " intenta que se lleve a cabo " + mensaje);
            if (host.contains("http://"))
                host = host.replace("http://", "");
            if (this.vHosts.containsKey(host.split("/")[0])) {
                VirtualHost to_replace = this.vHosts.get(host.split("/")[0]);
                logWarnMessage("El cliente en " + this.cliente.getInetAddress().getCanonicalHostName().toString() + ":"
                        + this.cliente.getPort() + " quiere acceder a " + to_replace.getReal_host() + "/"
                        + to_replace.getRoot_directory());
                host = to_replace.getReal_host() + "/" + to_replace.getRoot_directory();
            }
            fin = false;
            for (int i = 0; i < this.dPages.size() && !fin; ++i) {
                String h = (String) this.dPages.toArray()[i];
                System.out.println("Comparo " + host.split("/")[0] + " con " + h + ":" + h.equals(host.split("/")[0]));
                if (h.equals(host.split("/")[0])) {
                    fin = true;
                    System.out.println("Ok");
                }
            }

            if (fin) {
                logErrorMessage("El cliente en " + this.cliente.getInetAddress().getAddress().toString() + ":"
                        + this.cliente.getPort() + " intenta que se lleve a cabo una solicitud al host prohibido "
                        + host);
                return;
            }
            // Si es válido
            host = "http://" + host;
            logWarnMessage("Se va a ejecutar una solicitud " + method + " dirigida al host " + host);
            if (method.toLowerCase().equals("get")) {
                String user_agent = null;
                for (int i = 0; user_agent == null && i < nueva_peticion.length; ++i) {
                    String s = nueva_peticion[i];
                    if (s == null)
                        continue;
                    if (s.contains("User-Agent"))
                        user_agent = s.split(" ")[1];
                }
                mensaje = getRequest(host, user_agent);

            } else if (method.toLowerCase().equals("post")) {
                linea = lectura.readLine();
                nueva_peticion = linea.split("&&");
                ArrayList<NameValuePair> arrayList = new ArrayList<>();
                for (int i = 0; i < nueva_peticion.length; ++i) {
                    String[] params = nueva_peticion[i].split("=");
                    arrayList.add(new BasicNameValuePair(params[0], params[1]));
                }
                mensaje = postRequest(host, arrayList);
            }
            logInfoMessage("La respuesta tras ejecutar una solicitud de tipo " + method + " dirigida al host " + host
                    + " es:\n" + mensaje + "\n");
            PrintWriter escritura = new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(this.cliente.getOutputStream())), true);
            escritura.println(mensaje);
        } catch (Exception e) {
            logErrorMessage("Ocurrio \'" + e.getMessage() + "\'");
        }
    }

    // Estos métodos me permiten generar logs, tanto en un archivo como en stdout
    public static void logWarnMessage(String message) {
        Proxy.getLogger().warn(message);
    }

    public static void logInfoMessage(String message) {
        Proxy.getLogger().info(message);
    }

    public static void logErrorMessage(String message) {
        Proxy.getLogger().error(message);
    }

    public static void logDebugMessage(String message) {
        Proxy.getLogger().debug(message);
    }

    // Propia de get
    private static String getRequest(String host, String user_agent) {
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
                /*
                 * // Extraigo el estado de la petición String status =
                 * httpResponse.getStatusLine().toString();
                 */
                // Extraigo la entidad HTTP de la respuesta (La respuesta)
                HttpEntity entity = httpResponse.getEntity();
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
            logErrorMessage("Ocurrio \'" + e.getMessage() + "\'");
        }
        return result;
    }

    // Propia de post
    private static String postRequest(String host, ArrayList<NameValuePair> parameters) {
        // Inicializo el resultado de la solicitud para devolverlo más adelante
        String result = null;
        // Creo una instancia de cliente http para poder realizar la solicitud
        try (CloseableHttpClient httpClient = HttpClients.createDefault();) {
            // Inicializo la solicitud a realizar
            HttpPost request = new HttpPost(host);
            request.setEntity(new UrlEncodedFormEntity(parameters));
            // Creo una instancia de respuesta http para guardar el resultado de la
            // solicitud
            try (CloseableHttpResponse httpResponse = httpClient.execute(request);) {
                /*
                 * // Extraigo el estado de la petición String status =
                 * httpResponse.getStatusLine().toString();
                 */
                // Extraigo la entidad HTTP de la respuesta (La respuesta)
                HttpEntity entity = httpResponse.getEntity();
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
            logErrorMessage("Ocurrio \'" + e.getMessage() + "\'");
        }
        return result;
    }

    @Override
    public void run() {
        this.resolverPeticion();
    }

    public Auxiliar(Socket cliente, HashMap<String, VirtualHost> vHosts, HashSet<String> dPages) {
        super();
        this.cliente = cliente;
        this.vHosts = vHosts;
        this.dPages = dPages;
    }

}