package model.executors;

import model.Method;
import model.exceptions.UnknownEndpointException;
import server.AvailableEndpoints;
import server.HttpRequest;
import server.HttpResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RequestExecutorProxy {

    private final BalanceRequestExecutor balanceRequestExecutor;
    private final DebitRequestExecutor debitRequestExecutor;
    private final CreditRequestExecutor creditRequestExecutor;
    private Map<Method, Map<String, RequestExecutor>> requestExecutorMap;

    public RequestExecutorProxy(BalanceRequestExecutor balanceRequestExecutor, DebitRequestExecutor debitRequestExecutor, CreditRequestExecutor creditRequestExecutor) {
        this.balanceRequestExecutor = balanceRequestExecutor;
        this.debitRequestExecutor = debitRequestExecutor;
        this.creditRequestExecutor = creditRequestExecutor;
        this.requestExecutorMap = createRequestExecutorsMap();
    }

    Map<Method,Map<String, RequestExecutor>> createRequestExecutorsMap() {
        requestExecutorMap = new HashMap<>();
        Map<String, RequestExecutor> getRequestMap = new HashMap<>();
        getRequestMap.put(AvailableEndpoints.GET_WALLET_BALANCE, balanceRequestExecutor);
        requestExecutorMap.put(Method.GET, getRequestMap);
        Map<String, RequestExecutor> postRequestMap = new HashMap<>();
        postRequestMap.put(AvailableEndpoints.CREDIT_WALLET, creditRequestExecutor);
        postRequestMap.put(AvailableEndpoints.DEBIT_WALLET, debitRequestExecutor);
        requestExecutorMap.put(Method.POST, postRequestMap);
        return requestExecutorMap;
    }


    public HttpResponse executeRequest(Method method, String url, HttpRequest httpRequest) throws UnknownEndpointException {
        Map<String, RequestExecutor> executorsByMethod = requestExecutorMap.get(method);
        Optional<RequestExecutor> requestExecutor =  executorsByMethod.entrySet().stream().filter(entry -> url.matches(entry.getKey())).map(Map.Entry::getValue).findFirst();
        return requestExecutor.orElseThrow(UnknownEndpointException::new).execute(httpRequest);
    }
}
