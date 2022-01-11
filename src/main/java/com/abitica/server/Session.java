package com.abitica.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import com.abitica.model.Method;
import com.abitica.model.exceptions.UnknownEndpointException;
import com.abitica.executors.RequestExecutorProxy;

import java.io.IOException;
import java.net.Socket;

@Slf4j
public class Session implements Runnable {

    private final Socket socket;
    private final ObjectMapper objectMapper;
    private final RequestParser requestParser;
    private final ResponseSender responseSender;
    private final RequestExecutorProxy requestExecutorProxy;

    public Session(Socket socket, ObjectMapper objectMapper, RequestParser requestParser, ResponseSender responseSender, RequestExecutorProxy requestExecutorProxy) {
        this.socket = socket;
        this.objectMapper = objectMapper;
        this.requestParser = requestParser;
        this.responseSender = responseSender;
        this.requestExecutorProxy = requestExecutorProxy;
    }

    @Override
    public void run() {
        try {
            HttpRequest httpRequest = requestParser.parseHttpRequest(socket, objectMapper);
            HttpResponse httpResponse = executeRequest(httpRequest);
            responseSender.sendHttpResponse(httpResponse, socket);
        } catch(EmptyRequestException e) {
            log.warn("An empty request occurred");
        } catch (IOException e) {
            log.error("Error during response sending", e);
        } catch (UnknownEndpointException e) {
            log.error("Undefined endpoint", e);
        }
    }

    private HttpResponse executeRequest(HttpRequest httpRequest) throws UnknownEndpointException {
        Method method = Method.valueOf(httpRequest.getHeaders().get(HttpConstants.METHOD_HEADER_KEY));
        String url = httpRequest.getHeaders().get(HttpConstants.URI_HEADER_KEY);
        return requestExecutorProxy.executeRequest(method, url, httpRequest);
    }

}

