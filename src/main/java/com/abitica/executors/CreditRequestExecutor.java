package com.abitica.executors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.abitica.service.BalanceService;
import com.abitica.service.CreditService;
import com.abitica.model.WalletDTO;
import lombok.extern.slf4j.Slf4j;
import com.abitica.model.exceptions.DuplicateRequestException;
import com.abitica.model.exceptions.WalletNotFoundException;
import com.abitica.model.exceptions.WalletNotPersistedException;
import com.abitica.server.HttpConstants;
import com.abitica.server.HttpRequest;
import com.abitica.server.HttpResponse;
import com.abitica.server.Transaction;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CreditRequestExecutor implements RequestExecutor {
    private final CreditService creditService;
    private final BalanceService balanceController;
    private final ObjectMapper objectMapper;

    public CreditRequestExecutor(CreditService creditService, BalanceService balanceController, ObjectMapper objectMapper) {
        this.creditService = creditService;
        this.balanceController = balanceController;
        this.objectMapper = objectMapper;
    }

    @Override
    public HttpResponse execute(HttpRequest httpRequest) {
        String clientId = httpRequest.getHeaders().get(HttpConstants.URI_HEADER_KEY).split("/")[2];
        Map<String, String> headers = new HashMap<>();
        try {
            Transaction transaction = objectMapper.readValue(httpRequest.getBody(), Transaction.class);
            WalletDTO walletDTO = creditService.creditAccount(clientId, transaction);
            headers.put(HttpConstants.STATUS_HEADER_KEY, HttpConstants.STATUS_CREATED);
            String responseBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(walletDTO);
            return new HttpResponse(headers, responseBody);
        } catch (JsonProcessingException e) {
            log.error("Error while json processing", e);
            headers.put(HttpConstants.STATUS_HEADER_KEY, HttpConstants.STATUS_INTERNAL_SERVER_ERROR);
            return new HttpResponse(headers, "{\"message\":\"The request was processed with error\" }");
        } catch (DuplicateRequestException e) {
            return handleDuplicateRequest(headers, clientId);
        } catch (WalletNotPersistedException e) {
            headers.put(HttpConstants.STATUS_HEADER_KEY, HttpConstants.STATUS_INTERNAL_SERVER_ERROR);
            return new HttpResponse(headers, "{\"message\":\"The request was processed with error\" }");
        }
    }

    public HttpResponse handleDuplicateRequest(Map<String, String> headers, String clientId) {
        try {
            WalletDTO walletDTO = balanceController.getAccountBalance(clientId);
            headers.put(HttpConstants.STATUS_HEADER_KEY, HttpConstants.STATUS_DUPLICATED);
            String responseBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(walletDTO);
            return new HttpResponse(headers, responseBody);
        } catch (JsonProcessingException e) {
            log.error("Error while json processing", e);
            headers.put(HttpConstants.STATUS_HEADER_KEY, HttpConstants.STATUS_INTERNAL_SERVER_ERROR);
            return new HttpResponse(headers, "{\"message\":\"The request was processed with error\" }");
        } catch (WalletNotFoundException e) {
            headers.put(HttpConstants.STATUS_HEADER_KEY, HttpConstants.STATUS_NOT_FOUND);
            return new HttpResponse(headers, "{\"message\":\"The request was processed with error\" }");
        }
    }
}
