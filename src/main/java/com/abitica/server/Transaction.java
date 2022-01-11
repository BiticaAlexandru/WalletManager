package com.abitica.server;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Transaction {
    @JsonProperty
    private String transactionId;
    @JsonProperty
    private Double coins;

    public Transaction() {
    }

    public Transaction(String transactionId, Double coins) {
        this.transactionId = transactionId;
        this.coins = coins;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public double getCoins() {
        return coins;
    }

    public void setCoins(double coins) {
        this.coins = coins;
    }
}
