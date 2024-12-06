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
|``private boolean useCommandLinePhp``<br>El atributo `useCommandLinePhp` es un booleano que permite especificar si se debe usar el servidor `php` remoto o usar `php` CLI.|

### Constructores de class `HTTP`

|Constructor and Description|
|:---|
|`HTTP()` <br>El metodo crea un nuevo objeto HTTP. Los objetos HTTP crean un nuevo objeto de `ClientHandler` para manejar las peticiones HTTP de los clientes. Por defecto el servidor pondra `useCommandLinePhp = false`, `host_server_php = "127.0.0.1"` y `port_server_php = 8000`. |
|`HTTP(boolean useCommandLinePhp)`<br>El metodo crea un nuevo objeto HTTP que permite `useCommandLinePhp` para especificar si se debe usar el servidor ``php`` remoto o usar ``php`` CLI.|
|`HTTP(boolean useCommandLinePhp,String host_server_php,int port_server_php)`<br>El metodo crea un nuevo objeto HTTP que permite `useCommandLinePhp` para especificar si se debe usar el servidor ``php`` remoto o usar ``php`` CLI, permite especificar el host y el puerto del servidor ``php`` remoto.|


### Metodos de class `HTTP`

|Modifier and Type|Method and Description|
|:---|:----|
|``public void``|``addRouteHandler(String path, String handler)``|
|`public void`|`start(int port)`|


----