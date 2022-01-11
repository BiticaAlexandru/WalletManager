package com.abitica.executors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.abitica.service.BalanceService;
import com.abitica.service.DebitService;
import com.abitica.model.WalletDTO;
import com.abitica.model.exceptions.DuplicateRequestException;
import com.abitica.model.exceptions.WalletNotFoundException;
import com.abitica.model.exceptions.InsufficientFundsException;
import com.abitica.model.exceptions.MissingWalletException;
import com.abitica.server.HttpRequest;
import com.abitica.server.HttpResponse;
import com.abitica.server.Transaction;

import java.util.HashMap;
import java.util.Map;

public class DebitRequestExecutor implements RequestExecutor {

    private final DebitService debitService;
    private final BalanceService balanceController;
    private final ObjectMapper objectMapper;

    public DebitRequestExecutor(DebitService debitService, BalanceService balanceController, ObjectMapper objectMapper) {
        this.debitService = debitService;
        this.balanceController = balanceController;
        this.objectMapper = objectMapper;
    }

    @Override
    public HttpResponse execute(HttpRequest httpRequest) {
        String clientId = httpRequest.getHeaders().get("URI").split("/")[2];
        Map<String, String> headers = new HashMap<>();
        try {
            Transaction transaction = objectMapper.readValue(httpRequest.getBody(), Transaction.class);
            WalletDTO walletDTO = debitService.debitAccount(clientId, transaction);
            headers.put("STATUS", "HTTP/1.1 201 ");
            String responseBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(walletDTO);
            return new HttpResponse(headers, responseBody);
        } catch (JsonProcessingException | MissingWalletException e) {
            e.printStackTrace();
            headers.put("STATUS", "HTTP/1.1 500");
            return new HttpResponse(headers, "{\"message\":\"The request was processed with error\" }");//TODO add error response
        } catch (DuplicateRequestException e) {
            return handleDuplicateRequest(headers, clientId);
        } catch (InsufficientFundsException e) {
            return handleInsufficientFundsException(headers);
        }
    }

    private HttpResponse handleInsufficientFundsException(Map<String, String> headers) {
        headers.put("STATUS", "HTTP/1.1 400 BAD REQUEST");
        return new HttpResponse(headers, "");
    }

    public HttpResponse handleDuplicateRequest(Map<String, String> headers, String clientId) {
        try {
            WalletDTO walletDTO = balanceController.getAccountBalance(clientId);
            headers.put("STATUS", "HTTP/1.1 202 OK");
            String responseBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(walletDTO);
            return new HttpResponse(headers, responseBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            headers.put("STATUS", "HTTP/1.1 500");
            return new HttpResponse(headers, "{\"message\":\"The request was processed with error\" }");//TODO add error response
        } catch (WalletNotFoundException e) {
            headers.put("STATUS", "HTTP/1.1 404");
            return new HttpResponse(headers, "{\"message\":\"The request was processed with error\" }");//TODO add error response
        }
    }
}
