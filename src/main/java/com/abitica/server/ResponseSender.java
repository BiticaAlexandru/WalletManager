package com.abitica.server;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;

@Slf4j
public class ResponseSender {

    private final static String CONTENT_TYPE_TEXT_HTML = "Content-type: text/html";
    private final static String HEADER_BODY_SEPARATOR = "\r\n\r\n";

    public void sendHttpResponse(HttpResponse httpResponse, Socket socket) throws IOException {
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))){
            Map<String, String> headers = httpResponse.getHeaders();
            writer.write(headers.get(HttpConstants.STATUS_HEADER_KEY)+ "\r\n");

            writer.write(CONTENT_TYPE_TEXT_HTML + "\r\n");
            writer.write(HttpConstants.CONTENT_LENGTH_HEADER + HttpConstants.KEY_VALUE_HEADER_SEPARATOR + httpResponse.getBody().getBytes().length);
            writer.write(HEADER_BODY_SEPARATOR);
            writer.write(httpResponse.getBody());
            writer.flush();
        } catch (SocketException s) {
            socket.close();
            log.error("Exception while sending response", s);
        }
    }
}
