package com.abitica.executors;

import lombok.extern.slf4j.Slf4j;
import com.abitica.model.Method;
import com.abitica.model.exceptions.UnknownEndpointException;
import com.abitica.server.AvailableEndpoints;
import com.abitica.server.HttpRequest;
import com.abitica.server.HttpResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
@Slf4j
public class RequestExecutorProxy {

    private final BalanceRequestExecutor balanceRequestExecutor;
    private final DebitRequestExecutor debitRequestExecutor;
    private final CreditRequestExecutor creditRequestExecutor;
    private Map<Method, Map<String, RequestExecutor>> requestExecutorMap;

    public RequestExecutorProxy(BalanceRequestExecutor balanceRequestExecutor, DebitRequestExecutor debitRequestExecutor,
                                CreditRequestExecutor creditRequestExecutor) {
        this.balanceRequestExecutor = balanceRequestExecutor;
        this.debitRequestExecutor = debitRequestExecutor;
        this.creditRequestExecutor = creditRequestExecutor;
        this.requestExecutorMap = createRequestExecutorsMap();
    }

    Map<Method,Map<String, RequestExecutor>> createRequestExecutorsMap() {
        requestExecutorMap = new HashMap<>();
        Map<String, RequestExecutor> getRequestMap = new HashMap<>();
        setRequestExecutorsForGet(getRequestMap);
        setRequestExecutorsForPost();
        return requestExecutorMap;
    }

    private void setRequestExecutorsForPost() {
        Map<String, RequestExecutor> postRequestMap = new HashMap<>();
        postRequestMap.put(AvailableEndpoints.CREDIT_WALLET, creditRequestExecutor);
        postRequestMap.put(AvailableEndpoints.DEBIT_WALLET, debitRequestExecutor);
        requestExecutorMap.put(Method.POST, postRequestMap);
    }

    private void setRequestExecutorsForGet(Map<String, RequestExecutor> getRequestMap) {
        getRequestMap.put(AvailableEndpoints.GET_WALLET_BALANCE, balanceRequestExecutor);
        requestExecutorMap.put(Method.GET, getRequestMap);
    }


    public HttpResponse executeRequest(Method method, String url, HttpRequest httpRequest) throws UnknownEndpointException {
        log.info("Getting executor for method {} and url {}", method, url);
        Map<String, RequestExecutor> executorsByMethod = requestExecutorMap.get(method);
        Optional<RequestExecutor> requestExecutor =  executorsByMethod.entrySet().stream()
                .filter(entry -> url.matches(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst();
        return requestExecutor.orElseThrow(()->
                new UnknownEndpointException("Endpoint with method ["+method+"] and url ["+ url + "] was not defined"))
                .execute(httpRequest);
    }
}
