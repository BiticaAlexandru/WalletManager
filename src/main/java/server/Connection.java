package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.Method;
import model.exceptions.UnknownEndpointException;
import model.executors.RequestExecutorProxy;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;

public class Connection implements Runnable{

    private Socket socket;
    private ObjectMapper objectMapper;
    private RequestParser requestParser;
    private RequestExecutorProxy requestExecutorProxy;

    public Connection(Socket socket, ObjectMapper objectMapper, RequestParser requestParser, RequestExecutorProxy requestExecutorProxy) {
        this.socket = socket;
        this.objectMapper = objectMapper;
        this.requestParser = requestParser;
        this.requestExecutorProxy = requestExecutorProxy;
    }

    @Override
    public void run() {
        try {
            HttpRequest httpRequest = requestParser.parseHttpRequest(socket, objectMapper);
            HttpResponse httpResponse = executeRequest(httpRequest);
            sendHttpResponse(httpResponse);
        } catch(EmptyRequestException e) {
            System.out.println("An empty request occurred");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HttpResponse executeRequest(HttpRequest httpRequest) throws UnknownEndpointException {
        Method method = Method.valueOf(httpRequest.getHeaders().get("METHOD"));
        String url = httpRequest.getHeaders().get("URI");
        return requestExecutorProxy.executeRequest(method, url, httpRequest);
    }

    private void sendHttpResponse(HttpResponse httpResponse) throws IOException {
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))){
            Map<String, String> headers = httpResponse.getHeaders();
            writer.write(headers.get("STATUS")+ "\r\n");
            writer.write("Content-type: text/html" + "\r\n");
            writer.write("Content-length: " + httpResponse.getBody().getBytes().length + "");
            writer.write("\r\n\r\n");
            writer.write(httpResponse.getBody());
            writer.flush();
        } catch (SocketException s) {
            socket.close();
        }
    }
}

