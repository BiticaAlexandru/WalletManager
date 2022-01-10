package repository;

import controller.WalletDTO;

import java.util.Optional;

public interface WalletRepository {
    Optional<WalletDTO> getWallet(String id);
    void updateWallet(String id, WalletDTO walletDTO);
    WalletDTO createNewWallet(String id);
    Optional<String> getLastTransactionByWalletId(String id);
}
