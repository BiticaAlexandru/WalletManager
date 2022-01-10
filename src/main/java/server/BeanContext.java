package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import controller.BalanceController;
import controller.CreditController;
import controller.DebitController;
import model.executors.*;
import repository.DatabaseWalletRepository;
import repository.InMemoryWalletRepository;
import repository.WalletRepository;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class BeanContext {
    private final static Map<Class, Object> beans = new HashMap<>();

    public void initializeInMemoryContext() {
        WalletRepository walletRepository = getInMemoryWalletRepository();
        getBalanceController(walletRepository);
    }
    public void initializeOfficialContext() {
        WalletRepository walletRepository = getDatabaseWalletRepository();
        getBalanceController(walletRepository);
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
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres","postgres", "postgres");
            beans.put(java.sql.Connection.class, connection);
            return connection;
        }
    }

//Controllers

    private BalanceController getBalanceController(WalletRepository walletRepository) {
        if(beans.containsKey(BalanceController.class)) {
            return (BalanceController) beans.get(BalanceController.class);
        }
        BalanceController balanceController = new BalanceController(walletRepository);
        beans.put(BalanceController.class, balanceController);
        return balanceController;
    }

    private CreditController getCreditController(WalletRepository walletRepository) {
        if(beans.containsKey(CreditController.class)) {
            return (CreditController) beans.get(CreditController.class);
        }
        CreditController creditController = new CreditController(walletRepository);
        beans.put(CreditController.class, creditController);
        return creditController;
    }

    private DebitController getDebitController(WalletRepository walletRepository) {
        if(beans.containsKey(DebitController.class)) {
            return (DebitController) beans.get(DebitController.class);
        }
        DebitController debitController = new DebitController(walletRepository);
        beans.put(DebitController.class, debitController);
        return debitController;
    }

//Executors

    public BalanceRequestExecutor getBalanceRequestExecutor(WalletRepository walletRepository) {
        if(beans.containsKey(BalanceRequestExecutor.class)) {
            return (BalanceRequestExecutor) beans.get(BalanceRequestExecutor.class);
        }
        BalanceController balanceController = getBalanceController(walletRepository);
        ObjectMapper objectMapper = getObjectMapper();
        BalanceRequestExecutor balanceRequestExecutor = new BalanceRequestExecutor(balanceController, objectMapper);
        beans.put(BalanceRequestExecutor.class, balanceRequestExecutor);
        return balanceRequestExecutor;
    }

    public CreditRequestExecutor getCreditRequestExecutor(WalletRepository walletRepository) {
        if(beans.containsKey(CreditRequestExecutor.class)) {
            return (CreditRequestExecutor) beans.get(CreditRequestExecutor.class);
        }
        BalanceController balanceController = getBalanceController(walletRepository);
        CreditController creditController = getCreditController(walletRepository);
        ObjectMapper objectMapper = getObjectMapper();
        CreditRequestExecutor creditRequestExecutor = new CreditRequestExecutor(creditController, balanceController, objectMapper);
        beans.put(CreditRequestExecutor.class, creditRequestExecutor);
        return creditRequestExecutor;
    }

    public DebitRequestExecutor getDebitRequestExecutor(WalletRepository walletRepository) {
        if(beans.containsKey(DebitRequestExecutor.class)) {
            return (DebitRequestExecutor) beans.get(DebitRequestExecutor.class);
        }
        BalanceController balanceController = getBalanceController(walletRepository);
        DebitController debitController = getDebitController(walletRepository);
        ObjectMapper objectMapper = getObjectMapper();
        DebitRequestExecutor debitRequestExecutor = new DebitRequestExecutor(debitController, balanceController, objectMapper);
        beans.put(DebitRequestExecutor.class, debitController);
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