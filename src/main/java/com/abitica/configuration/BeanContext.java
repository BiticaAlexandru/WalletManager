package com.abitica.configuration;

import com.abitica.server.ResponseSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.abitica.server.RequestParser;
import com.abitica.service.BalanceService;
import com.abitica.service.CreditService;
import com.abitica.service.DebitService;
import lombok.extern.slf4j.Slf4j;
import com.abitica.executors.BalanceRequestExecutor;
import com.abitica.executors.CreditRequestExecutor;
import com.abitica.executors.DebitRequestExecutor;
import com.abitica.executors.RequestExecutorProxy;
import com.abitica.repository.DatabaseWalletRepository;
import com.abitica.repository.InMemoryWalletRepository;
import com.abitica.repository.WalletRepository;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class BeanContext {
    private final static Map<Class, Object> beans = new HashMap<>();
    private final static String DATABASE_PASSWORD = "postgres";
    private final static String DATABASE_CONNECTION_URL = "jdbc:postgresql://localhost:5432/postgres";
    private final static String PROPERTIES_PATH = "src\\main\\resources\\application.properties";
    private final static String DATABASE_USER = "postgres";

    public final static String PROFILE_PROPERTY = "profile";
    public final static String OFFICIAL_PROFILE = "official";
    public final static String IN_MEMORY_PROFILE = "memory";

    public void initializeInMemoryContext() {
        log.info("Initializing in memory context");
        WalletRepository walletRepository = getInMemoryWalletRepository();
        getBalanceController(walletRepository);
        log.info("Finished initializing in memory context");
    }

    public Properties loadProperties() {
        try (InputStream input = new FileInputStream(PROPERTIES_PATH)) {
            Properties properties = new Properties();
            properties.load(input);
            beans.put(Properties.class, properties);
            return properties;
        } catch (IOException e) {
            log.error("Error while reading application.properties", e);
        }
        return loadDefaultProperties();
    }

    private Properties loadDefaultProperties() {
        Properties properties = new Properties();
        properties.setProperty(PROFILE_PROPERTY, IN_MEMORY_PROFILE);
        return properties;
    }

    public void initializeOfficialContext() {
        log.info("Initializing official context");
        WalletRepository walletRepository = getDatabaseWalletRepository();
        getBalanceController(walletRepository);
        log.info("Finished initializing official context");
    }

    public void closeContext() throws SQLException {
        Connection connection = getDatabaseConnection();
        connection.close();
    }

    public RequestParser getRequestParser() {
        if(beans.containsKey(RequestParser.class)) {
            return (RequestParser) beans.get(RequestParser.class);
        }
        RequestParser requestParser = new RequestParser();
        beans.put(RequestParser.class, requestParser);
        return requestParser;
    }

    public ResponseSender gerResponseSender() {
        if(beans.containsKey(ResponseSender.class)) {
            return (ResponseSender) beans.get(ResponseSender.class);
        }
        ResponseSender responseSender = new ResponseSender();
        beans.put(ResponseSender.class, responseSender);
        return responseSender;
    }

    public ObjectMapper getObjectMapper() {
        if(beans.containsKey(ObjectMapper.class)) {
            return (ObjectMapper) beans.get(ObjectMapper.class);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        beans.put(ObjectMapper.class, objectMapper);
        return objectMapper;
    }

//Repositories

    public WalletRepository getInMemoryWalletRepository() {
        if (beans.containsKey(WalletRepository.class)) {
            return (WalletRepository) beans.get(WalletRepository.class);
        }
        WalletRepository walletRepository = new InMemoryWalletRepository();
        beans.put(WalletRepository.class, walletRepository);
        return walletRepository;
    }

    public WalletRepository getDatabaseWalletRepository() {
        if (beans.containsKey(WalletRepository.class)) {
            return (WalletRepository) beans.get(WalletRepository.class);
        }
        try {
            Connection connection = getDatabaseConnection();
            WalletRepository walletRepository = new DatabaseWalletRepository(connection);
            beans.put(WalletRepository.class, walletRepository);
            return walletRepository;
        } catch (SQLException throwables) {
            return getInMemoryWalletRepository();
        }
    }

    private java.sql.Connection getDatabaseConnection() throws SQLException {
        if(beans.containsKey(java.sql.Connection.class)) {
            return (java.sql.Connection) beans.get(java.sql.Connection.class);
        } else {
            Driver postgreDriver = new org.postgresql.Driver();
            DriverManager.registerDriver(postgreDriver);

            Connection connection = DriverManager.getConnection(DATABASE_CONNECTION_URL, DATABASE_USER, DATABASE_PASSWORD);
            beans.put(java.sql.Connection.class, connection);
            return connection;
        }
    }

//Controllers

    private BalanceService getBalanceController(WalletRepository walletRepository) {
        if(beans.containsKey(BalanceService.class)) {
            return (BalanceService) beans.get(BalanceService.class);
        }
        BalanceService balanceController = new BalanceService(walletRepository);
        beans.put(BalanceService.class, balanceController);
        return balanceController;
    }

    private CreditService getCreditController(WalletRepository walletRepository) {
        if(beans.containsKey(CreditService.class)) {
            return (CreditService) beans.get(CreditService.class);
        }
        CreditService creditService = new CreditService(walletRepository);
        beans.put(CreditService.class, creditService);
        return creditService;
    }

    private DebitService getDebitController(WalletRepository walletRepository) {
        if(beans.containsKey(DebitService.class)) {
            return (DebitService) beans.get(DebitService.class);
        }
        DebitService debitService = new DebitService(walletRepository);
        beans.put(DebitService.class, debitService);
        return debitService;
    }

//Executors

    public BalanceRequestExecutor getBalanceRequestExecutor(WalletRepository walletRepository) {
        if(beans.containsKey(BalanceRequestExecutor.class)) {
            return (BalanceRequestExecutor) beans.get(BalanceRequestExecutor.class);
        }
        BalanceService balanceController = getBalanceController(walletRepository);
        ObjectMapper objectMapper = getObjectMapper();
        BalanceRequestExecutor balanceRequestExecutor = new BalanceRequestExecutor(balanceController, objectMapper);
        beans.put(BalanceRequestExecutor.class, balanceRequestExecutor);
        return balanceRequestExecutor;
    }

    public CreditRequestExecutor getCreditRequestExecutor(WalletRepository walletRepository) {
        if(beans.containsKey(CreditRequestExecutor.class)) {
            return (CreditRequestExecutor) beans.get(CreditRequestExecutor.class);
        }
        BalanceService balanceController = getBalanceController(walletRepository);
        CreditService creditService = getCreditController(walletRepository);
        ObjectMapper objectMapper = getObjectMapper();
        CreditRequestExecutor creditRequestExecutor = new CreditRequestExecutor(creditService, balanceController, objectMapper);
        beans.put(CreditRequestExecutor.class, creditRequestExecutor);
        return creditRequestExecutor;
    }

    public DebitRequestExecutor getDebitRequestExecutor(WalletRepository walletRepository) {
        if(beans.containsKey(DebitRequestExecutor.class)) {
            return (DebitRequestExecutor) beans.get(DebitRequestExecutor.class);
        }
        BalanceService balanceController = getBalanceController(walletRepository);
        DebitService debitService = getDebitController(walletRepository);
        ObjectMapper objectMapper = getObjectMapper();
        DebitRequestExecutor debitRequestExecutor = new DebitRequestExecutor(debitService, balanceController, objectMapper);
        beans.put(DebitRequestExecutor.class, debitService);
        return debitRequestExecutor;
    }

    public RequestExecutorProxy getRequestExecutorProxy() {
        if(beans.containsKey(RequestExecutorProxy.class)) {
            return (RequestExecutorProxy) beans.get(RequestExecutorProxy.class);
        }
        WalletRepository walletRepository = getInMemoryWalletRepository();
        BalanceRequestExecutor balanceRequestExecutor = getBalanceRequestExecutor(walletRepository);
        CreditRequestExecutor creditRequestExecutor = getCreditRequestExecutor(walletRepository);
        DebitRequestExecutor debitRequestExecutor = getDebitRequestExecutor(walletRepository);
        RequestExecutorProxy requestExecutorProxy = new RequestExecutorProxy(balanceRequestExecutor, debitRequestExecutor, creditRequestExecutor);
        beans.put(RequestExecutorProxy.class, requestExecutorProxy);
        return requestExecutorProxy;
    }


}
