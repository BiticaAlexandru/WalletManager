package controller;

import model.exceptions.DuplicateRequestException;
import model.exceptions.InsufficientFundsException;
import model.exceptions.MissingWalletException;
import repository.RepositoryConstants;
import repository.WalletRepository;
import server.Transaction;

public class DebitController {

    private final WalletRepository walletRepository;

    public DebitController(WalletRepository walletRepository) {
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
