package controller;

import model.exceptions.WalletNotFoundException;
import repository.WalletRepository;

public class BalanceController {

    private final WalletRepository walletRepository;

    public BalanceController(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public WalletDTO getAccountBalance(String clientId) throws WalletNotFoundException {
        return walletRepository.getWallet(clientId).orElseThrow(WalletNotFoundException::new);
    }

}
