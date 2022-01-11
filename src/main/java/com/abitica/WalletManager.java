package com.abitica;

import com.abitica.server.ResponseSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import com.abitica.executors.RequestExecutorProxy;
import com.abitica.configuration.BeanContext;
import com.abitica.server.UnsupportedProfileConfigurationException;
import com.abitica.server.Session;
import com.abitica.server.RequestParser;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class WalletManager {

    public static final int PORT = 8080;

    public static void main(String[] args) throws IOException, SQLException {
        BeanContext context = new BeanContext();
        Properties properties = context.loadProperties();
        String profile = (String) properties.getOrDefault(BeanContext.PROFILE_PROPERTY, BeanContext.IN_MEMORY_PROFILE);
        try{
            initializeProfile(context, profile);
            startServer(context);
        } catch (UnsupportedProfileConfigurationException unsupportedProfileConfigurationException) {
            log.error("Profile was wrongly defined into the properties file. It can be only: official/memory.", unsupportedProfileConfigurationException);
        } finally {
            context.closeContext();
        }
    }

    private static void initializeProfile(BeanContext context, String profile) throws UnsupportedProfileConfigurationException {
        boolean isOfficial = profile.equalsIgnoreCase(BeanContext.OFFICIAL_PROFILE);
        boolean isInMemory = profile.equalsIgnoreCase(BeanContext.IN_MEMORY_PROFILE);
        if(!isOfficial && !isInMemory) {
            throw new UnsupportedProfileConfigurationException();
        }
        if(isOfficial) {
            context.initializeOfficialContext();
        } else {
            context.initializeInMemoryContext();
        }
    }

    private static void startServer(BeanContext context) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        ExecutorService executorService = Executors.newCachedThreadPool();
        ObjectMapper objectMapper = context.getObjectMapper();
        RequestParser requestParser = context.getRequestParser();
        ResponseSender responseSender = context.gerResponseSender();
        RequestExecutorProxy requestExecutorProxy = context.getRequestExecutorProxy();
        while(true) {
            Socket socket = serverSocket.accept();
            Session session = new Session(socket, objectMapper, requestParser, responseSender, requestExecutorProxy);
            executorService.execute(session);
        }
    }


}
