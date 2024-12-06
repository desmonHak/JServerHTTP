import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HTTP {
    private ServerSocket serverSocket;
    private final Map<String, String> routeHandlers;
    ClientHandler clientHandler;
    private boolean useCommandLinePhp;

    public HTTP() {
        this.serverSocket = null;
        this.routeHandlers = new HashMap<>();
        useCommandLinePhp = false;
        clientHandler = new ClientHandler(null, routeHandlers);
    }
    public HTTP(boolean useCommandLinePhp) {
        this.serverSocket = null;
        this.routeHandlers = new HashMap<>();
        clientHandler = new ClientHandler(null, routeHandlers);
        this.useCommandLinePhp = useCommandLinePhp;
        clientHandler.useCommandLinePhp = this.useCommandLinePhp;
    }
    public HTTP(
            boolean useCommandLinePhp,
            String host_server_php,
            int port_server_php
    ) {
        this.serverSocket = null;
        this.routeHandlers = new HashMap<>();
        clientHandler = new ClientHandler(null, routeHandlers, host_server_php, port_server_php);
        this.useCommandLinePhp = useCommandLinePhp;
        clientHandler.useCommandLinePhp = this.useCommandLinePhp;
    }


    public void addRouteHandler(String path, String handler) {
        routeHandlers.put(path, handler);
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            System.out.println("Servidor iniciado en el puerto: " + port);

            while (!serverSocket.isClosed()) {
                try {
                    Socket client = serverSocket.accept();
                    System.out.println("Cliente conectado: " + client.getInetAddress().getHostAddress());
                    clientHandler = new ClientHandler(client, routeHandlers);
                    clientHandler.useCommandLinePhp = this.useCommandLinePhp;
                    clientHandler.setClientSocket(client); // añadir el socket
                    new Thread(clientHandler).start();
                } catch (IOException e) {
                    System.out.println("Error al aceptar la conexión: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Servidor detenido.");
            }
        } catch (IOException e) {
            System.out.println("Error al detener el servidor: " + e.getMessage());
        }
    }
}
