package com.abitica.service;

import com.abitica.model.WalletDTO;
import com.abitica.model.exceptions.DuplicateRequestException;
import com.abitica.model.exceptions.WalletNotPersistedException;
import com.abitica.repository.RepositoryConstants;
import com.abitica.repository.WalletRepository;
import com.abitica.server.Transaction;

public class CreditService {

    private final WalletRepository walletRepository;

    public CreditService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public WalletDTO creditAccount(String clientId, Transaction transaction) throws WalletNotPersistedException, DuplicateRequestException {
        String transactionId = walletRepository.getLastTransactionByWalletId(clientId).orElseGet(() -> RepositoryConstants.EMPTY_TRANSACTION_ID);
        if(transactionId.isEmpty() || !transactionId.equalsIgnoreCase(transaction.getTransactionId())) {
            WalletDTO walletDTO = transactionId.isEmpty() ? walletRepository.createNewWallet(clientId) : walletRepository.getWallet(clientId).get();
            walletDTO.setTransactionId(transaction.getTransactionId());
            walletDTO.setVersion(walletDTO.getVersion() + 1);
            walletDTO.setCoins(walletDTO.getCoins() + transaction.getCoins());
            walletRepository.updateWallet(clientId, walletDTO);
            return walletRepository.getWallet(clientId).orElseThrow(WalletNotPersistedException::new);
        }
        throw new DuplicateRequestException();
    }
}
