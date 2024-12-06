public class Main {
    public static void main(String[] args) {
        HTTP server = new HTTP(false);
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
