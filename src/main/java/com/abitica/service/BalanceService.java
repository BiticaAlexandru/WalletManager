package com.abitica.service;

import com.abitica.model.WalletDTO;
import com.abitica.model.exceptions.WalletNotFoundException;
import com.abitica.repository.WalletRepository;

public class BalanceService {

    private final WalletRepository walletRepository;

    public BalanceService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public WalletDTO getAccountBalance(String clientId) throws WalletNotFoundException {
        return walletRepository.getWallet(clientId).orElseThrow(WalletNotFoundException::new);
    }

}
