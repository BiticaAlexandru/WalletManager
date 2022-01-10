package server;

public class AvailableEndpoints {
    public static final String GET_WALLET_BALANCE = "/wallets/(.*)";
    public static final String CREDIT_WALLET = "/wallets/(.*)/credit";
    public static final String DEBIT_WALLET = "/wallets/(.*)/debit";
}
