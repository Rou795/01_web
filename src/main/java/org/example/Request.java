package org.example;

import java.net.*;
import java.util.*;


public class Request {
    private String method;
    final private String path;
    final private String protocol;
    private List<String> headers;
    private String body;

    public Request(String method, String path, String protocol, List<String> headers, String body) {
        this.method = method;
        this.path = URLDecoder.decode(path);
        this.protocol = protocol;
        this.headers = headers;
        this.body = URLDecoder.decode(body);

    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public String getPath() {
        return path;
    }

    public String getProtocol() {
        return protocol;
    }

    // методы для парсинга QueryString
    public List<List<String>> getQueryParams() {
        List<List<String>> paramsList = new ArrayList<>();
        String params = path.replaceFirst("^.{2}", "");
        String[] parts = params.split("&");
        for (String part : parts) {
            paramsList.add(List.of(part.split("=", 2)));
        }
        return paramsList;
    }

    // выбрал список, так как могут быть параметры с одинаковыми ключами
    public List<String> getQueryParam(String name) {
        List<List<String>> paramsList = this.getQueryParams();
        List<String> response = new ArrayList<>();
        for (List<String> paramSet : paramsList) {
            if (paramSet.getFirst().equals(name)) {
                response.add(paramSet.get(1));
            }
        }
        return response;
    }

    // методы для парсинга параметров из тела при методе Post
    public List<List<String>> getPostParams() {
        List<List<String>> paramsList = new ArrayList<>();
        if (body != null) {
            System.out.println();
            String[] parts = body.split("&");
            for (String part : parts) {
                paramsList.add(List.of(part.split("=", 2)));
            }
        }
        return paramsList;
    }

    public List<String> getPostParam(String name) {
        List<List<String>> paramsList = this.getPostParams();
        List<String> response = new ArrayList<>();
        for (List<String> paramSet : paramsList) {
            if (paramSet.getFirst().equals(name)) {
                response.add(paramSet.get(1));
            }
        }
        return response;
    }

}