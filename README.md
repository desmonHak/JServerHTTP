# JServerHTTP

----

Servidor HTTP en java, permite usar php o java para procesar las peticiones de tipo `POST` que deseas.

Para poder usar php debes descargar el servidor php de su sitio oficial, descomprimirlo, copiando todo el contenido descomprimido al directorio `php` si es posible.
Una vez hecho, debe agregar `php.exe` a las variables de entorno, lo anterior no es estricatamente necesario ya que podra invocar a php desde cualquier lugar al agregarlo a las variables de entorno, pero es recomendable que si solo descargar `php` para usar este repositorio, centralize toda la instalacion.

Para poder invocar al servidor `php` por linea de comandos es necesario ejecutar:
```bash
php -S 127.0.0.1:8000
```
Esto pondra a la escucha el servidor en el puerto 8000.

## Class `HTTP`

|Constructor and Description|
|:---|
|``private ServerSocket serverSocket``<br>El atributo `serverSocket` es un objeto de tipo `ServerSocket` que permite escuchar las peticiones de los clientes.|
|``private final Map<String, String> routeHandlers``<br>El atributo `routeHandlers` es un mapa que permite almacenar las rutas de los recursos y los manejadores de las peticiones HTTP.|
|``ClientHandler clientHandler``<br>El atributo `clientHandler` es un objeto de tipo `ClientHandler` que permite manejar las peticiones HTTP de los clientes.|

### Constructores de class `HTTP`

|Constructor and Description|
|:---|
|`HTTP()` <br>El metodo crea un nuevo objeto HTTP. Los objetos HTTP crean un nuevo objeto de `ClientHandler` para manejar las peticiones HTTP de los clientes. Por defecto el servidor pondra `useCommandLinePhp = false`, `host_server_php = "127.0.0.1"` y `port_server_php = 8000`. |
|`HTTP(boolean useCommandLinePhp)`<br>El metodo crea un nuevo objeto HTTP que permite `useCommandLinePhp` para especificar si se debe usar el servidor ``php`` remoto o usar ``php`` CLI.|
|`HTTP(boolean useCommandLinePhp,String host_server_php,int port_server_php)`<br>El metodo crea un nuevo objeto HTTP que permite `useCommandLinePhp` para especificar si se debe usar el servidor ``php`` remoto o usar ``php`` CLI, permite especificar el host y el puerto del servidor ``php`` remoto.|


### Metodos de class `HTTP`

|Modifier and Type|Method and Description|
|:---|:----|
|``public void``|``addRouteHandler(String path, String handler)``<br>El metodo agrega una ruta y un manejador de peticiones HTTP. |
|`public void`|`start(int port)`<br>El metodo inicia el servidor HTTP en el puerto especificado.|
|`public void`|``stop()``<br>El metodo detiene el servidor HTTP.|


----


## Class `ClientHandler`


|Constructor and Description|
|:---|
|`boolean useCommandLinePhp`<br>`true` para usar `php` por CLI, `false` para usar `php` remoto.|
|`String host_server_php`<br>Host donde se encuentra el servidor php. Solo aplica si `useCommandLinePhp = false`.|
|`int    port_server_php`<br>Port para realizar la conexion con el servidor php. Solo aplica si `useCommandLinePhp = false`.|

### Constructores de class `ClientHandler`

|Constructor and Description|
|:---|
|`ClientHandler()` <br> |
|`public ClientHandler(Socket socket,Map<String, String> routeHandlers,String host_server_php,int port_server_php)`<br>|


### Metodos de class `ClientHandler`

|Modifier and Type|Method and Description|
|:---|:----|
|`public void`|`run()`<br>La clase `ClientHandler` es instanciada por `HTTP`, creando un nuevo hilo, siendo el nuevo `socket` de `ClientHandler` una instancia `Socket` para manejar la conexion del nuevo cliente.|
|`private String`|`processRequest(String request, String body)`<br>Procesa las peticciones `GET` (`handleGet`) y `POST`(`handlePost`). En caso de que una metodo http valido no este implementado, se devuelve `501`. En caso de que no exista se devuelve `405`|
|`private String`|`handleGet(String path)`<br>La ruta por defecto es `index.html` para `/`. Cuando se solicita un archivo via `GET` el archivo se lee en el momento de la peticion, en caso de que exista se genera una respuesta http `200`. En caso de que no exista el archivo se genera una respuesta http `404`. En caso de que se genere un error `IOException` se genera una respuesta http `500`.|
|`private String`|`handlePost(String path, String body)`<br>Este metodo se encarga de procesar las peticiones `POST`. Si existe la ruta se obtiene el `handler`, el `handler` especifica si tratar los datos usando `php` o usando `java`, esto se especifica por el programador en su funciona `main` o donde se instancie a `HTTP`. En caso de que el recurso no exista y no se alla añadido, se devuelve el codigo http `404`.|
|`private String`|`handlePhpPost(String path, String body)`<br>Este metodo procesa las peticiones `POST`, cuando se usa `php` con `useCommandLinePhp = true` se usa la CLI, cuando se usa `php` con `useCommandLinePhp = false` se realiza una conexion remota contra un servidor php. Se espera que php este en la ruta `php/php.exe`.|
|`private String`|`handleJavaPost(String path, String body)`<br>Procesa la peticion usando una clase Java para procesar los datos.|
|`private String`|`createHttpResponse(String status, String contentType, String content)`<br>El metodo crea una respuesta http con el codigo con el contenido deseado.|
|`private String`|`getContentType(String filePath)`<br>Permite obtener el tipo de contenido de un archivo.|

----


Example code:
```java
public class Main {
    public static void main(String[] args) {
        HTTP server = new HTTP(false, "127.0.0.1", 8000);
        /*
         * Cuando useCommandLinePhp es false, se usara una conexion remota
         * cuando es true se usa php via CLI.
         * Cuando se usa la opcion de conexion remota, se puede obtener los
         * datos de la peticion usando php://input, en cambio si se usa la opcion
         * de CLI, solo se puede obtener a traves de php://stdin
         *
         * Ejecutar servidor php: php -S 127.0.0.1:8000
         */
        server.addRouteHandler("/submit.php", "php");
        server.addRouteHandler("/SubmitHandler.java", "java");
        server.start(80);
    }
}
```

El metodo `server.addRouteHandler("/submit.php", "php");` agrega una ruta `submit.php`, esta a de encontrarse en el directorio en el cual se ejecuta el servidor `php`.
El metodo `server.addRouteHandler("/SubmitHandler.java", "java");` agrega una ruta `SubmitHandler.java`, esta a de encontrarse en el directorio en el cual se ejecuta el servidor `java`.

Estas rutas responde a los siguientes formularios:
```html
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8" />
        <title>Formulario de Ejemplo</title>
    </head>
    <body>
        <h1>Formulario de Ejemplo</h1>
        <form action="/submit.php" method="post">
            <label for="nombre1">Nombre:</label>
            <input type="text" id="nombre1" name="nombre" required /><br /><br />

            <label for="email1">Email:</label>
            <input type="email" id="email1" name="email" required /><br /><br />

            <input type="submit" value="Enviar a PHP" />
        </form>

        <form action="/SubmitHandler.java" method="post">
            <label for="nombre">Nombre:</label>
            <input type="text" id="nombre" name="nombre" required /><br /><br />

            <label for="email">Email:</label>
            <input type="email" id="email" name="email" required /><br /><br />

            <input type="submit" value="Enviar a Java" />
        </form>
    </body>
</html>
```

La clase `SubmitHandler` procesara los datos que se envia via `POST`, esta clase sera llamada por la clase `ClientHandler`. La clase `SubmitHandler` puede proporcioanar codigo ``html``, codigo `css` o codigo `js` entre otros:
```java
public class SubmitHandler {
    public static String handlePost(String body) {
        // Procesar los datos del cuerpo de la solicitud
        String[] params = body.split("&");
        StringBuilder response = new StringBuilder("<html><body><h1>Datos recibidos:</h1><ul>");

        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                response.append("<li>").append(keyValue[0]).append(": ").append(keyValue[1]).append("</li>");
            }
        }

        response.append("</ul></body></html>");
        return response.toString();
    }
}
```

El `submit.php` si se configura con `useCommandLinePhp = false` entonces no podra usar `php://stdin` y debera usar `php://input`, en caso constrario, `useCommandLinePhp = true` podra usar `php://stdin`.

para `useCommandLinePhp = true`:
```php
<?php
    // Read POST data from stdin
    $postData = file_get_contents("php://stdin");

    // Print raw data for debugging
    echo "Datos crudos recibidos: " . $postData . "\n\n";

    // Parse the POST data
    parse_str($postData, $params);

    // Generate HTML response
    echo "<html><body><h1>Datos recibidos en PHP:</h1><ul>";
    foreach ($params as $key => $value) {
        echo "<li>" . htmlspecialchars($key) . ": " . htmlspecialchars($value) . "</li>";
    }
    echo "</ul></body></html>";
?>
```
para `useCommandLinePhp = false`:
```php
<?php
    $postData = file_get_contents("php://input");

    echo "Datos crudos recibidos: " . $postData . "\n\n";

    parse_str($postData, $params);

    echo "<html><body><h1>Datos recibidos en PHP:</h1><ul>";
    foreach ($params as $key => $value) {
        echo "<li>" . htmlspecialchars($key) . ": " . htmlspecialchars($value) . "</li>";
    }
    echo "</ul></body></html>";
?>
```

----