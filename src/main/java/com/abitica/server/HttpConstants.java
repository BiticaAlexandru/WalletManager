package com.abitica.server;

public class HttpConstants {
    public static final String URI_HEADER_KEY = "URI";
    public static final String METHOD_HEADER_KEY = "METHOD";
    public static final String STATUS_HEADER_KEY = "STATUS";
    public static final String CONTENT_LENGTH_HEADER = "Content-Length";
    public static final String KEY_VALUE_HEADER_SEPARATOR = ":";


    public static final String STATUS_OK = "HTTP/1.1 200 OK";
    public static final String STATUS_CREATED = "HTTP/1.1 201 CREATED";
    public static final String STATUS_DUPLICATED = "HTTP/1.1 202 OK";
    public static final String STATUS_NOT_FOUND = "HTTP/1.1 404";
    public static final String STATUS_INTERNAL_SERVER_ERROR = "HTTP/1.1 500";
}
