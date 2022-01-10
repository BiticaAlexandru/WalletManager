package model.executors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.BalanceController;
import controller.WalletDTO;
import model.exceptions.EmptyWalletException;
import server.HttpRequest;
import server.HttpResponse;

import java.util.HashMap;
import java.util.Map;

public class BalanceRequestExecutor implements RequestExecutor {

    private final BalanceController balanceController;
    private final ObjectMapper objectMapper;

    public BalanceRequestExecutor(BalanceController balanceController, ObjectMapper objectMapper) {
        this.balanceController = balanceController;
        this.objectMapper = objectMapper;
    }

    @Override
    public HttpResponse execute(HttpRequest httpRequest) {
        String clientId = httpRequest.getHeaders().get("URI").split("/")[2];
        Map<String, String> headers = new HashMap<>();
        try {
            WalletDTO walletDTO = balanceController.getAccountBalance(clientId);
            headers.put("STATUS", "HTTP/1.1 200 OK");
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
