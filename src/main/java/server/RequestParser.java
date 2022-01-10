package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.exceptions.UnknownEndpointException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class RequestParser {

    public HttpRequest parseHttpRequest(Socket socket, ObjectMapper objectMapper) throws IOException, UnknownEndpointException, EmptyRequestException {
        BufferedReader inBufferReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Map<String, String> headers = parseHeaders(inBufferReader);
        String requestBody = parseContent(inBufferReader, headers, objectMapper);
        return new HttpRequest(headers, requestBody);
    }

    private String parseContent(BufferedReader inBufferReader, Map<String, String> headers, ObjectMapper objectMapper) throws IOException {
        String content = "";
        String contentLength = headers.get("Content-Length");
        if(contentLength != null && !contentLength.isEmpty()) {
            int bodyLength = Integer.parseInt(contentLength);
            char[] body = new char[bodyLength];
            inBufferReader.read(body);
            return new String(body);
        }
        return "";
    }

    private Map<String, String> parseHeaders(BufferedReader inBufferReader) throws IOException, EmptyRequestException {
        Map<String, String> headers = new HashMap<>();
        String inputLine = inBufferReader.readLine();
        if(inputLine != null && !inputLine.isEmpty()) {
            String[] tokens = inputLine.split(" ");
            headers.put("METHOD", tokens[0]);
            headers.put("URI", tokens[1]);
        }
        while ((inputLine = inBufferReader.readLine()) != null && !inputLine.equals("")) {
            String[] header = inputLine.split(":");
            headers.put(header[0].trim(), header[1].trim());
        }
        if(headers.isEmpty()) {
            throw new EmptyRequestException();
        }
        return headers;
    }
}
