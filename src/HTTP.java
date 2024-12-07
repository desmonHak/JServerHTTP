import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HTTP {
    private ServerSocket serverSocket;
    private final Map<String, String> routeHandlers;
    ClientHandler clientHandler;
    private boolean useCommandLinePhp;
    private String host_server_php;
    private int port_server_php;

    public HTTP() {
        this.serverSocket = null;
        this.routeHandlers = new HashMap<>();
        this.useCommandLinePhp = false;
        this.host_server_php   = "127.0.0.1";
        this.port_server_php   = 8000;
        clientHandler = new ClientHandler(null, routeHandlers);
    }
    public HTTP(boolean useCommandLinePhp) {
        this.serverSocket = null;
        this.routeHandlers = new HashMap<>();
        clientHandler = new ClientHandler(null, routeHandlers);
        this.host_server_php   = "127.0.0.1";
        this.port_server_php   = 8000;
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
        this.host_server_php   = host_server_php;
        this.port_server_php   = port_server_php;
        clientHandler = new ClientHandler(null, routeHandlers, this.host_server_php, this.port_server_php);
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
                    clientHandler = new ClientHandler(client, routeHandlers, this.host_server_php, this.port_server_php);
                    clientHandler.useCommandLinePhp = this.useCommandLinePhp;
                    //clientHandler.setClientSocket(client); // añadir el socket
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
