package org.example;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class Request {
    private String method;
    final private String path;
    final private String protocol;
    private List<String> headers;
    private String body;

    public Request(String method, String path, String protocol, List<String> headers, String body) {
        this.method = method;
        this.path = path;
        this.protocol = protocol;
        this.headers = headers;
        this.body = body;
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

    public List<List<String>> getQueryParams() {
        List<List<String>> paramsList = new ArrayList<>();
        String params = "";
        if (method.equals("GET")) {
            params = URLDecoder.decode(path);
        } else {
            params = URLDecoder.decode(body);
        }
        System.out.println(params);
        return paramsList;
    }
}
