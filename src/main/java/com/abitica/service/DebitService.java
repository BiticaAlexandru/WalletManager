package com.abitica.service;

import com.abitica.model.WalletDTO;
import com.abitica.model.exceptions.DuplicateRequestException;
import com.abitica.model.exceptions.InsufficientFundsException;
import com.abitica.model.exceptions.MissingWalletException;
import com.abitica.repository.RepositoryConstants;
import com.abitica.repository.WalletRepository;
import com.abitica.server.Transaction;

public class DebitService {

    private final WalletRepository walletRepository;

    public DebitService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public WalletDTO debitAccount(String clientId, Transaction transaction) throws MissingWalletException, DuplicateRequestException, InsufficientFundsException {
        String transactionId = walletRepository.getLastTransactionByWalletId(clientId).orElseGet(() -> RepositoryConstants.EMPTY_TRANSACTION_ID);
        if(transactionId.isEmpty() || !transactionId.equalsIgnoreCase(transaction.getTransactionId())) {
            WalletDTO walletDTO = walletRepository.getWallet(clientId).orElseThrow(MissingWalletException::new);
            if(walletDTO.getCoins() < transaction.getCoins()) {
                throw new InsufficientFundsException();
            }
            walletDTO.setCoins(walletDTO.getCoins() - transaction.getCoins());
            walletDTO.setTransactionId(transaction.getTransactionId());
            walletDTO.setVersion(walletDTO.getVersion()+1);
            walletRepository.updateWallet(clientId, walletDTO);
            return walletRepository.getWallet(clientId).orElseThrow(MissingWalletException::new);
        }
        throw new DuplicateRequestException();
    }
}
