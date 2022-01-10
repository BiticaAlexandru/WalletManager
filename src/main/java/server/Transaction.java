package server;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Transaction {
    @JsonProperty
    private String transactionId;
    @JsonProperty
    private Long coins;

    public Transaction() {
    }

    public Transaction(String transactionId, Long coins) {
        this.transactionId = transactionId;
        this.coins = coins;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Long getCoins() {
        return coins;
    }

    public void setCoins(Long coins) {
        this.coins = coins;
    }
}
