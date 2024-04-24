package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

import static java.lang.Thread.sleep;

public class Server {

    static int port = 9999;
    final static String GET = "GET";
    final static String POST = "POST";
    final static int limit = 4096;
    volatile static HashMap<String, Handler> handlers = new HashMap<>();
    String fullRequest = "";
    static List<String> messages = new ArrayList<>();

    public void initial() {
        final List<String> allowedMethods = List.of(GET, POST);
        List<Thread> connectPool = new ArrayList<>();
        //messages.add("test");
        try (final var serverSocket = new ServerSocket(port)) {
            Runnable connect = () -> {
                while (true) {
                    try (
                            final var socket = serverSocket.accept();
                            final var in = new BufferedInputStream(socket.getInputStream());
                            final var out = new BufferedOutputStream(socket.getOutputStream());
                    ) {

                        // помечаем первый блок данных,в котором должен находится requestLine
                        in.mark(limit);
                        final byte[] buffer = new byte[limit];
                        final int end = in.read(buffer);

                        // проверка корректности размера requestLine
                        final byte[] requestLineDelimiter = new byte[]{'\r', '\n'};
                        final int requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, end);
                        if (requestLineEnd == -1) {
                            out.write(headersResponse404());
                            continue;
                        }

                        // проверка метода
                        String[] requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
                        System.out.println(Arrays.toString(requestLine));
                        String method = requestLine[0];
                        if (!allowedMethods.contains(method)) {
                            out.write(headersResponse400());
                            continue;
                        }
                        System.out.println(method);

                        // проверка корректности указания пути (с '/')
                        String path = requestLine[1];
                        if (!path.startsWith("/")) {
                            out.write(headersResponse400());
                            continue;
                        }
                        System.out.println(path);
                        String protocol = requestLine[2];

                        final byte[] headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
                        final int headersStart = requestLineEnd + requestLineDelimiter.length;
                        final int headersEnd = indexOf(buffer, headersDelimiter, headersStart, end);

                        // проверка наличия заголовков
                        if (headersEnd == -1) {
                            out.write(headersResponse400());
                            continue;
                        }

                        // вытаскивание заголовков
                        in.reset();
                        in.skip(headersStart);
                        final byte[] headersByte = in.readNBytes(headersEnd - headersStart);
                        final List<String> headers = Arrays.asList(new String(headersByte).split("\r\n"));
                        System.out.println(headers);

                        String body = "";
                        if (!method.equals(GET)) {
                            in.skip(headersDelimiter.length);
                            final Optional<String> contentLength = extractHeader(headers, "Content-Length");
                            if (contentLength.isPresent()) {
                                final int length = Integer.parseInt(contentLength.get());
                                final byte[] bodyBytes = in.readNBytes(length);
                                body = new String(bodyBytes);
                                System.out.println(body);
                            }
                        }

                        //выделение первой строки запроса для создания объекта Request

                        Request request = new Request(method, path, protocol, headers, body);
                       // System.out.println("good connect");

                        // проверочные выводы для методов getQueryParams() и getQueryParam(String name)
                        if (request.getMethod().equals(GET)) {
                            List<List<String>> params = request.getQueryParams();
                            System.out.println(params);
                            System.out.println(request.getQueryParam("title"));
                            System.out.println(request.getQueryParam("value"));
                        }

                        if (request.getMethod().equals(POST)) {
                            List<List<String>> params = request.getPostParams();
                            System.out.println(params);
                            System.out.println(request.getPostParam("title"));
                            System.out.println(request.getPostParam("value"));
                        }
                        // просмотр handler'ов по ключу и задействование нужного
                        // доработал, чтобы реагировал на любые наборы параметров в QueryString
                        synchronized (handlers) {
                            path = request.getPath();
                            String findKey = request.getMethod() + path.substring(0, path.indexOf("?"));
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
            }
            for (Thread thread : connectPool) {
                thread.join();
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

    public static byte[] headersResponse200() {
        System.out.println("200");
        return ("HTTP/1.1 200 OK\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n").getBytes();
    }

    public static byte[] headersResponse400() {
        return ("HTTP/1.1 400 Bad request\r\n" +
                "Content-Length: 0\r\n" +
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

    public static int indexOf(byte[] array, byte[] target, int start, int end) {
        outer:
        for (int i = start; i < end - target.length +1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

}
