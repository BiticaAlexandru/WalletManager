package model.executors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.BalanceController;
import controller.CreditController;
import controller.WalletDTO;
import lombok.extern.slf4j.Slf4j;
import model.exceptions.DuplicateRequestException;
import model.exceptions.WalletNotFoundException;
import model.exceptions.WalletNotPersistedException;
import server.HttpConstants;
import server.HttpRequest;
import server.HttpResponse;
import server.Transaction;

import java.util.HashMap;
import java.util.Map;

@Slf4j
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
        String clientId = httpRequest.getHeaders().get(HttpConstants.URI_HEADER_KEY).split("/")[2];
        Map<String, String> headers = new HashMap<>();
        try {
            Transaction transaction = objectMapper.readValue(httpRequest.getBody(), Transaction.class);
            WalletDTO walletDTO = creditController.creditAccount(clientId, transaction);
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
            headers.put(HttpConstants.STATUS_HEADER_KEY, HttpConstants.STATUS_OK);
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
