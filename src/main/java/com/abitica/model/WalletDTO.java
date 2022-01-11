package com.abitica.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class WalletDTO {
    @JsonProperty
    private String transactionId;
    @JsonProperty
    private long version;
    @JsonProperty
    private double coins;

    public WalletDTO(String transactionId, long version, double coins) {
        this.transactionId = transactionId;
        this.version = version;
        this.coins = coins;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public double getCoins() {
        return coins;
    }

    public void setCoins(double coins) {
        this.coins = coins;
    }
}
