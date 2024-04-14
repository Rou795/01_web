package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Server {

    final static List<String> VALID_RATHS = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    final static int PORT = 9999;

    public static void main(String[] args) throws InterruptedException {

        List<Thread> connectPool = new ArrayList<>();
        try (final var serverSocket = new ServerSocket(PORT)) {
            Runnable connect = () -> {
                while (true) {
                    try (
                            final var socket = serverSocket.accept();
                            final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            final var out = new BufferedOutputStream(socket.getOutputStream());
                    ) {
                        // read only request line for simplicity
                        // must be in form GET /path HTTP/1.1
                        final var requestLine = in.readLine();
                        final var parts = requestLine.split(" ");
                        System.out.println("good connect");
                        if (!checkLength(parts)) {
                            // just close socket
                            continue;
                        }

                        final var path = parts[1];
                        if (!checkPaths(path, out)) {
                            continue;
                        }

                        final var filePath = Path.of(".", "public", path);
                        final var mimeType = Files.probeContentType(filePath);

                        // special case for classic
                        if (path.equals("/classic.html")) {
                            final var template = Files.readString(filePath);
                            final var content = template.replace(
                                    "{time}",
                                    LocalDateTime.now().toString()
                            ).getBytes();
                            out.write(headersResponse(200, mimeType, content.length));
                            out.write(content);
                            out.flush();
                            continue;
                        }
                        System.out.println("send image...");
                        final var length = Files.size(filePath);
                        out.write(headersResponse(200, mimeType, length));
                        Files.copy(filePath, out);
                        out.flush();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkLength(String[] parts) {
        // just close socket
        return parts.length == 3;
    }

    public static boolean checkPaths(String path, BufferedOutputStream out) throws IOException {
        if (VALID_RATHS.contains(path)) {
            return true;
        } else {
            out.write(headersResponse404());
            out.flush();
            return false;
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

}
