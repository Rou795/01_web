package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();

        // добавление хендлеров (обработчиков)

        server.addHandler("GET", "/messages", (request, responseStream) -> {
            // TODO: handlers code
            try {
                String response;
                if (Server.messages.isEmpty()) {
                    response = "\r\n\r\nMessages list are empty";
                    responseStream.write(Server.headersResponse(200, "text", response.length()));
                    responseStream.write(response.getBytes());
                    responseStream.flush();
                } else {
                    StringBuilder builder = new StringBuilder();
                    builder.append("\r\n\r\n");
                    for (String mes : Server.messages) {
                        builder.append(mes);
                    }
                    String content = builder.toString();
                    responseStream.write(Server.headersResponse(200, "text", content.length()));
                    responseStream.write(content.getBytes());
                    responseStream.flush();
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        server.addHandler("POST", "/messages", (request, responseStream) -> {
            // TODO: handlers code
            Server.messages.add(request.getBody());
            try {
                responseStream.write(Server.headersResponse201());
                responseStream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

// handler'ы для дз с формами
        server.addHandler("POST", "/", (request, responseStream) -> {
            // TODO: handlers code
            try {
                responseStream.write(Server.headersResponse201());
                responseStream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        server.addHandler("GET", "/", (request, responseStream) -> {
            // TODO: handlers code
            try {
                responseStream.write(Server.headersResponse201());
                responseStream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        // код инициализации сервера (из вашего предыдущего ДЗ)
        server.listen(9999);
        server.initial();
    }
}