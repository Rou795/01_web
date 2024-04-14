package org.example;

import java.util.HashMap;

public class Request {
    private String method;
    private String path;
    private String protocol;
    private HashMap<String, String> headers;
    private String body;

    public Request(String method, String path, String protocol, HashMap<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.protocol = protocol;
        this.headers = headers;
        this.body = body;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public HashMap<String, String> getHeaders() {
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
}
