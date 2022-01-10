package repository;

import controller.WalletDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryWalletRepository implements WalletRepository {

    private final Map<String, WalletDTO> walletRepository  = new HashMap<>();

    @Override
    public Optional<WalletDTO> getWallet(String id) {
        if(walletRepository.containsKey(id)) {
            return Optional.of(walletRepository.get(id));
        }
        return Optional.empty();
    }

    @Override
    public void updateWallet(String id, WalletDTO walletDTO) {
        if(walletRepository.containsKey(id)) {
            walletRepository.put(id, walletDTO);
        }
    }

    @Override
    public WalletDTO createNewWallet(String id) {
        WalletDTO walletDTO = new WalletDTO("", 0, 0d);
        walletRepository.put(id, walletDTO);
        return walletDTO;
    }


    @Override
    public Optional<String> getLastTransactionByWalletId(String id) {
        if(walletRepository.containsKey(id)) {
            String transactionId = walletRepository.get(id).getTransactionId();
            return Optional.of(transactionId);
        }
        return Optional.empty();
    }
}
