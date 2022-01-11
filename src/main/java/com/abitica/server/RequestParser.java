package com.abitica.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static com.abitica.server.HttpConstants.*;

public class RequestParser {

    private static final String HEADER_FIRST_LINE_SEPARATOR = " ";




    public HttpRequest parseHttpRequest(Socket socket, ObjectMapper objectMapper) throws IOException, EmptyRequestException {
        BufferedReader inBufferReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Map<String, String> headers = parseHeaders(inBufferReader);
        String requestBody = parseContent(inBufferReader, headers, objectMapper);
        return new HttpRequest(headers, requestBody);
    }

    private Map<String, String> parseHeaders(BufferedReader inBufferReader) throws IOException, EmptyRequestException {
        Map<String, String> headers = new HashMap<>();
        String inputLine = inBufferReader.readLine();
        if (inputLine != null && !inputLine.isEmpty()) {
            String[] tokens = inputLine.split(HEADER_FIRST_LINE_SEPARATOR);
            headers.put(METHOD_HEADER_KEY, tokens[0]);
            headers.put(URI_HEADER_KEY, tokens[1]);
        }
        while ((inputLine = inBufferReader.readLine()) != null && !inputLine.equals("")) {
            String[] header = inputLine.split(HttpConstants.KEY_VALUE_HEADER_SEPARATOR);
            headers.put(header[0].trim(), header[1].trim());
        }
        if (headers.isEmpty()) {
            throw new EmptyRequestException();
        }
        return headers;
    }

    private String parseContent(BufferedReader inBufferReader, Map<String, String> headers, ObjectMapper objectMapper) throws IOException {
        String contentLength = headers.get(CONTENT_LENGTH_HEADER);
        if (contentLength != null && !contentLength.isEmpty()) {
            int bodyLength = Integer.parseInt(contentLength);
            char[] body = new char[bodyLength];
            inBufferReader.read(body);
            return new String(body);
        }
        return "";
    }
}
