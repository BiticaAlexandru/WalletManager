package model;

public class Wallet {
    private final String id;
    private long version;
    private double balance;

    public Wallet(String id, long version, double balance) {
        this.id = id;
        this.version = version;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public long increaseVersion() {
        return ++version;
    }

    public long getVersion() {
        return version;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
}
