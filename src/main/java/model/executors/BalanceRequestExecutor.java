package model.executors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.BalanceController;
import controller.WalletDTO;
import lombok.extern.slf4j.Slf4j;
import model.exceptions.WalletNotFoundException;
import server.HttpConstants;
import server.HttpRequest;
import server.HttpResponse;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class BalanceRequestExecutor implements RequestExecutor {

    private final BalanceController balanceController;
    private final ObjectMapper objectMapper;

    public BalanceRequestExecutor(BalanceController balanceController, ObjectMapper objectMapper) {
        this.balanceController = balanceController;
        this.objectMapper = objectMapper;
    }

    @Override
    public HttpResponse execute(HttpRequest httpRequest) {
        String clientId = httpRequest.getHeaders().get(HttpConstants.URI_HEADER_KEY).split("/")[2];
        Map<String, String> headers = new HashMap<>();
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
            return new HttpResponse(headers, "{\"message\":\"The requested wallet was not found\" }");
        }
    }
}
