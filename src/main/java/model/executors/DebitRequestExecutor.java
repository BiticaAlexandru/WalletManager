package model.executors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.BalanceController;
import controller.DebitController;
import controller.WalletDTO;
import model.*;
import model.exceptions.DuplicateRequestException;
import model.exceptions.InsufficientFundsException;
import model.exceptions.MissingWalletException;
import server.HttpRequest;
import server.HttpResponse;
import server.Transaction;

import java.util.HashMap;
import java.util.Map;

public class DebitRequestExecutor implements RequestExecutor {

    private final DebitController debitController;
    private final BalanceController balanceController;
    private final ObjectMapper objectMapper;

    public DebitRequestExecutor(DebitController debitController, BalanceController balanceController, ObjectMapper objectMapper) {
        this.debitController = debitController;
        this.balanceController = balanceController;
        this.objectMapper = objectMapper;
    }

    @Override
    public HttpResponse execute(HttpRequest httpRequest) {
        String clientId = httpRequest.getHeaders().get("URI").split("/")[2];
        Map<String, String> headers = new HashMap<>();
        try {
            Transaction transaction = objectMapper.readValue(httpRequest.getBody(), Transaction.class);
            WalletDTO walletDTO = debitController.debitAccount(clientId, transaction);
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
        } catch (EmptyWalletException e) {
            headers.put("STATUS", "HTTP/1.1 404");
            return new HttpResponse(headers, "{\"message\":\"The request was processed with error\" }");//TODO add error response
        }
    }
}
