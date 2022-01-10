package model.executors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.BalanceController;
import controller.CreditController;
import controller.WalletDTO;
import model.exceptions.DuplicateRequestException;
import model.EmptyWalletException;
import model.exceptions.WalletNotPersistedException;
import server.HttpRequest;
import server.HttpResponse;
import server.Transaction;

import java.util.HashMap;
import java.util.Map;

public class CreditRequestExecutor implements RequestExecutor {
    private final CreditController creditController;
    private final BalanceController balanceController;
    private final ObjectMapper objectMapper;

    public CreditRequestExecutor(CreditController creditController, BalanceController balanceController, ObjectMapper objectMapper) {
        this.creditController = creditController;
        this.balanceController = balanceController;
        this.objectMapper = objectMapper;
    }

    @Override
    public HttpResponse execute(HttpRequest httpRequest) {
        String clientId = httpRequest.getHeaders().get("URI").split("/")[2];
        Map<String, String> headers = new HashMap<>();
        try {
            Transaction transaction = objectMapper.readValue(httpRequest.getBody(), Transaction.class);
            WalletDTO walletDTO = creditController.creditAccount(clientId, transaction);
            headers.put("STATUS", "HTTP/1.1 201 CREATED");
            String responseBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(walletDTO);
            return new HttpResponse(headers, responseBody);
        } catch (JsonProcessingException | WalletNotPersistedException e) {
            e.printStackTrace();
            headers.put("STATUS", "HTTP/1.1 500");
            return new HttpResponse(headers, "{\"message\":\"The request was processed with error\" }");//TODO add error response
        } catch (DuplicateRequestException e) {
            return handleDuplicateRequest(headers, clientId);
        }
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
        } catch (EmptyWalletException e) {
            headers.put("STATUS", "HTTP/1.1 404");
            return new HttpResponse(headers, "{\"message\":\"The request was processed with error\" }");//TODO add error response
        }
    }
}
