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