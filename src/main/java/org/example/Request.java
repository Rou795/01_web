package org.example;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.*;
import java.nio.charset.Charset;
import java.util.*;


public class Request {
    private String method;
    final private String path;
    final private String protocol;
    private HashMap<String, String> headers = new HashMap<>();
    private String body;
    private List<NameValuePair> queryParams;

    public Request(String method, String path, String protocol, List<String> headers, String body) {
        this.method = method;
        this.path = path;
        this.protocol = protocol;
        headers.stream().forEach(o -> this.headers.put(o.split(": ")[0], o.split(": ")[1]));
        this.body = body;
        this.queryParams = URLEncodedUtils.parse(path.replaceFirst("/\\?", ""), Charset.defaultCharset());
    }

    public void setMethod(String method) {
        this.method = method;
    }

    /*public void setHeaders(List<String> headers) {
        this.headers = headers;
    }
     */

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

    // методы для парсинга QueryString
    public List<NameValuePair> getQueryParams() {
        return this.queryParams;
        /*
        List<List<String>> paramsList = new ArrayList<>();
        String params = path.replaceFirst("^.{2}", "");
        String[] parts = params.split("&");
        for (String part : parts) {
            paramsList.add(List.of(part.split("=", 2)));
        }
        return paramsList;
         */
    }

    // выбрал список, так как могут быть параметры с одинаковыми ключами
    public List<NameValuePair> getQueryParam(String name) {
        return this.queryParams.stream()
                .filter(o -> o.getName().equals(name))
                .toList();
        /*
        List<List<String>> paramsList = this.getQueryParams();
        List<String> response = new ArrayList<>();
        for (List<String> paramSet : paramsList) {
            if (paramSet.getFirst().equals(name)) {
                response.add(paramSet.get(1));
            }
        }
        return response;
         */
    }

    // методы для парсинга параметров из тела при методе Post
    public List<NameValuePair> getPostParams() {
        List<NameValuePair> paramsList = new ArrayList<>();
        /*
        String contentType = this.headers.stream()
                .filter(o -> o.startsWith("Content-Type"))
                .map(o -> o.split(": ")[1])
                .toString();
        */
        if (this.headers.get("Content-Type").equals("application/x-www-form-urlencoded")) {
            if (body != null) {
                System.out.println();
                paramsList = URLEncodedUtils.parse(body, Charset.defaultCharset());
            }
        }
        return paramsList;
    }

    public List<String> getPostParam(String name) {
        List<NameValuePair> paramsList = this.getPostParams();
        List<String> response = new ArrayList<>();
        for (NameValuePair paramSet : paramsList) {
            if (paramSet.getName().equals(name)) {
                response.add(paramSet.getValue());
            }
        }
        return response;
    }

}