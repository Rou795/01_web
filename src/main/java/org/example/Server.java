package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Server {

    static int port = 9999;

    volatile static HashMap<String, Handler> handlers = new HashMap<>();

    static List<String> messages = new ArrayList<>();

    public void initial() {
        List<Thread> connectPool = new ArrayList<>();
        //messages.add("test");
        try (final var serverSocket = new ServerSocket(port)) {
            Runnable connect = () -> {
                while (true) {
                    try (
                            final var socket = serverSocket.accept();
                            final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            final var out = new BufferedOutputStream(socket.getOutputStream())
                    ) {
                        // Читаем весь запрос и раскладываем его по частям в объект Request
                        StringBuilder builder = new StringBuilder();
                        while (in.ready()) {
                            builder.append(in.readLine());
                            builder.append("\n");
                        }
                        String fullRequest = builder.toString();
                        System.out.println(fullRequest);
                        String body = "";
                        //проверка на существование body
                        if (fullRequest.contains("\r\n\r\n")) {
                            body = fullRequest.split("\r\n\r\n")[1];
                            fullRequest = fullRequest.replaceFirst(body, "");
                        }
                        String[] parts = fullRequest.split("\n");
                        HashMap<String, String> headers = new HashMap<>();
                        String requestLine = "";
                        for (int i = 0; i < parts.length; i++) {
                            if (i == 0) {
                                requestLine = parts[i];
                            } else {
                                String[] pair = parts[i].split(":", 2);
                                headers.put(pair[0], pair[1].strip());
                            }
                        }
                        //выделение первой строки запроса для создания объекта Request
                        System.out.println(requestLine);
                        String method;
                        String path;
                        String protocol;
                        final String[] partsForBuilder = requestLine.split(" ");
                        synchronized (partsForBuilder) {
                            method = partsForBuilder[0];
                            path = partsForBuilder[1];
                            protocol = partsForBuilder[2];
                        }
                        Request request = new Request(method, path, protocol, headers, body);
                        System.out.println("good connect");
                    // просмотр handler'ов по ключу и задействование нужного
                        synchronized (handlers) {
                            String findKey = request.getMethod() + request.getPath();
                            for (String key : handlers.keySet()) {
                                if (key.equals(findKey)) {
                                    handlers.get(key).handle(request, out);
                                }
                            }
                            out.write(headersResponse404());
                            out.flush();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            for (int i = 0; i < 64; i++) {
                connectPool.add(new Thread(connect));
                connectPool.get(i).start();
                connectPool.get(i).join();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static byte[] headersResponse(int code, String mimeType, long length) {
        System.out.println("200");
        return ("HTTP/1.1 " + code + " OK\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Content-Length: " + length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n").getBytes();
    }

    public static byte[] headersResponse404() {
        return ("HTTP/1.1 404 Not Found\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n").getBytes();
    }

    public static byte[] headersResponse201() {
        return ("HTTP/1.1 201 Message saved\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n").getBytes();
    }

    public void addHandler(String method, String rout, Handler handler) {
        synchronized (handlers) {
            handlers.put(method + rout, handler);
        }
    }

    public void listen(int port) {
        Server.port = port;
    }
}
