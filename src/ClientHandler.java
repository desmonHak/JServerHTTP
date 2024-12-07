import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.Map;

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private final Map<String, String> routeHandlers;
    // Cambia esta variable según la opción elegida en Main
    boolean useCommandLinePhp; // Cambia a true para usar la línea de comandos

    String host_server_php; // host donde se encuentra el servidor php
    int    port_server_php; // puerto donde se ejecuta

    void setClientSocket(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    public ClientHandler(Socket socket, Map<String, String> routeHandlers) {
        this.clientSocket      = socket;
        this.routeHandlers     = routeHandlers;
        this.useCommandLinePhp = false;
        this.host_server_php   = "127.0.0.1";
        this.port_server_php   = 8000;
    }

    public ClientHandler(
            Socket socket,
            Map<String, String> routeHandlers,
            String host_server_php,
            int port_server_php) {
        this.clientSocket      = socket;
        this.routeHandlers     = routeHandlers;
        this.useCommandLinePhp = false;
        this.host_server_php   = host_server_php;
        this.port_server_php   = port_server_php;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String inputLine;
            StringBuilder request = new StringBuilder();
            while ((inputLine = in.readLine()) != null && !inputLine.isEmpty()) {
                request.append(inputLine).append("\n");
            }

            StringBuilder body = new StringBuilder();
            if (in.ready()) {
                while (in.ready()) {
                    body.append((char) in.read());
                }
            }

            System.out.println("Solicitud recibida:\n" + request);
            System.out.println("Cuerpo de la solicitud:\n" + body);

            String response = processRequest(request.toString(), body.toString());
            out.println(response);

        } catch (IOException e) {
            System.out.println("Error en la comunicación con el cliente: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error al cerrar el socket del cliente: " + e.getMessage());
            }
        }
    }

    private String processRequest(String request, String body) {
        String[] requestLines = request.split("\n");
        if (requestLines.length == 0) {
            return createHttpResponse("400 Bad Request", "text/plain", "400 - Solicitud mal formada");
        }

        String[] requestLine = requestLines[0].split(" ");
        if (requestLine.length < 2) {
            return createHttpResponse("400 Bad Request", "text/plain", "400 - Solicitud mal formada");
        }

        String method = requestLine[0];
        String path = requestLine[1];

        if ("GET".equals(method)) {
            return handleGet(path);
        } else if ("POST".equals(method)) {
            return handlePost(path, body);
        } else {
            switch (method) {
                case "HEAD":
                case "OPTIONS":
                case "TRACE":
                case "CONNECT":
                case "PATCH":
                case "DELETE":
                case "PUT":
                case "QUERY":
                    return createHttpResponse("501 Not Implemented", "text/plain", "501 - Método no implementado");
                default: break;
            }
            return createHttpResponse("405 Method Not Allowed", "text/plain", "405 - Método no permitido");
        }
    }

    private String handleGet(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (path.isEmpty()) {
            path = "index.html";
        }

        File file = new File(path);
        if (file.exists() && !file.isDirectory()) {
            try {
                String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                return createHttpResponse("200 OK", getContentType(path), content);
            } catch (IOException e) {
                return createHttpResponse("500 Internal Server Error", "text/plain", "500 - Error al leer el archivo");
            }
        } else {
            return createHttpResponse("404 Not Found", "text/plain", "404 - Archivo no encontrado: " + path);
        }
    }

    private String handlePost(String path, String body) {
        System.out.println("Procesando POST para la ruta: " + path);

        String handler = routeHandlers.get(path);
        if (handler != null) {
            if ("php".equals(handler)) {
                return handlePhpPost(path, body);
            } else if ("java".equals(handler)) {
                return handleJavaPost(path, body);
            }
        }

        return createHttpResponse("404 Not Found", "text/plain", "404 - Ruta no encontrada para POST: " + path);
    }

    private String handlePhpPost(String path, String body) {
        try {

            if (useCommandLinePhp) { // Ejecutar PHP desde la línea de comandos
                String phpPath = "php/php.exe";
                ProcessBuilder processBuilder = new ProcessBuilder(phpPath, "-f", path.substring(1));
                processBuilder.redirectErrorStream(true);
                processBuilder.directory(new File(System.getProperty("user.dir")));

                // Set environment variables
                Map<String, String> env = processBuilder.environment();
                env.put("REQUEST_METHOD", "POST");
                env.put("CONTENT_LENGTH", String.valueOf(body.length()));
                env.put("CONTENT_TYPE", "application/x-www-form-urlencoded");

                Process process = processBuilder.start();

                // Write POST data to PHP's stdin
                try (OutputStream os = process.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                StringBuilder output = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                }

                int exitCode = process.waitFor();
                System.out.println("Salida del proceso PHP: " + output.toString());

                if (exitCode == 0) {
                    return createHttpResponse("200 OK", "text/html", output.toString());
                } else {
                    String errorMessage = "Error al ejecutar PHP (Código de salida: " + exitCode + "):\n" + output.toString();
                    System.err.println(errorMessage);
                    return createHttpResponse("500 Internal Server Error", "text/plain", errorMessage);
                }

            } else {// Usar un servidor web para ejecutar PHP
                // Conectar al servidor PHP usando sockets
                try (Socket socket = new Socket("127.0.0.1", 8000)) {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    // Crear la solicitud HTTP
                    String request = "POST " + path + " HTTP/1.1\r\n" +
                            "Host: 127.0.0.1\r\n" +
                            "Content-Type: application/x-www-form-urlencoded\r\n" +
                            "Content-Length: " + body.length() + "\r\n" +
                            "Connection: close\r\n" + // Este encabezado es importante
                            "\r\n" +
                            body;


                    // Enviar la solicitud
                    out.println(request);
                    out.flush();

                    // Leer la respuesta
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line).append("\n");
                        if (line.isEmpty()) { // Fin de los encabezados
                            break;
                        }
                    }
                    // Leer el cuerpo si existe
                    // Leer el cuerpo de la respuesta
                    StringBuilder bodys = new StringBuilder();
                    while ((line = in.readLine()) != null) {
                        bodys.append(line).append("\n");
                    }

                    return createHttpResponse("200 OK", "text/html", bodys.toString());
                } catch (IOException e) {
                    String errorMessage = "Error al conectar con el servidor PHP: " + e.getMessage();
                    System.err.println(errorMessage);
                    return createHttpResponse("500 Internal Server Error", "text/plain", errorMessage);
                }
            }

        } catch (IOException e) {
            String errorMessage = "Error al procesar PHP: " + e.getMessage();
            System.err.println(errorMessage);
            return createHttpResponse("500 Internal Server Error", "text/plain", errorMessage);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



    private String handleJavaPost(String path, String body) {
        String response = SubmitHandler.handlePost(body);
        return createHttpResponse("200 OK", "text/html", response);
    }

    private String createHttpResponse(String status, String contentType, String content) {
        return "HTTP/1.1 " + status + "\r\n" +
                "Content-Type: " + contentType + "; charset=UTF-8\r\n" +
                "Date: " + new Date() + "\r\n" +
                "Server: JavaHTTPServer\r\n" +
                "Content-Length: " + content.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                "\r\n" +
                content;
    }

    private String getContentType(String filePath) {
        if (filePath.endsWith(".html") || filePath.endsWith(".htm")) {
            return "text/html";
        } else if (filePath.endsWith(".css")) {
            return "text/css";
        } else if (filePath.endsWith(".js")) {
            return "application/javascript";
        } else if (filePath.endsWith(".png")) {
            return "image/png";
        } else if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filePath.endsWith(".gif")) {
            return "image/gif";
        } else if (filePath.endsWith(".txt")) {
            return "text/plain";
        } else {
            return "application/octet-stream";
        }
    }
}