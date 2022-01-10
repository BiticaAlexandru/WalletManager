import com.fasterxml.jackson.databind.ObjectMapper;
import model.executors.RequestExecutorProxy;
import server.BeanContext;
import server.BrokenProfileConfiguration;
import server.Connection;
import server.RequestParser;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoinServer {

    public static final int PORT = 8080;


    public static void main(String[] args) throws IOException, SQLException {
        BeanContext context = new BeanContext();
        Properties properties = context.loadProperties();
        String profile = (String) properties.getOrDefault(BeanContext.PROFILE_PROPERTY, BeanContext.IN_MEMORY_PROFILE);
        try{
            if(profile.equalsIgnoreCase(BeanContext.OFFICIAL_PROFILE)) {
                context.initializeOfficialContext();
            }
            if(profile.equalsIgnoreCase(BeanContext.IN_MEMORY_PROFILE)) {
                context.initializeInMemoryContext();
            } else {
                throw new BrokenProfileConfiguration();
            }
            startServer(context);
        } catch (BrokenProfileConfiguration brokenProfileConfiguration) {
            brokenProfileConfiguration.printStackTrace(); //TODO add message
        } finally {
            context.closeContext();
        }
    }

    private static void startServer(BeanContext context) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        ExecutorService executorService = Executors.newCachedThreadPool();
        ObjectMapper objectMapper = context.getObjectMapper();
        RequestParser requestParser = context.getRequestParser();
        RequestExecutorProxy requestExecutorProxy = context.getRequestExecutorProxy();
        while(true) {
            Socket socket = serverSocket.accept();
            Connection connection = new Connection(socket, objectMapper, requestParser, requestExecutorProxy);
            executorService.execute(connection);
        }
    }


}
