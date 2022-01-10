package controller;

import model.exceptions.EmptyWalletException;
import repository.WalletRepository;

public class BalanceController {

    private final WalletRepository walletRepository;

    public BalanceController(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public WalletDTO getAccountBalance(String clientId) throws EmptyWalletException {
        return walletRepository.getWallet(clientId).orElseThrow(EmptyWalletException::new);
    }

}
