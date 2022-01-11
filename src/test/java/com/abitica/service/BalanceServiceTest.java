package com.abitica.service;

import com.abitica.model.WalletDTO;
import com.abitica.model.exceptions.WalletNotFoundException;
import com.abitica.repository.WalletRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

public class BalanceServiceTest {

    @Test
    void testRight() throws WalletNotFoundException {
        WalletRepository walletRepositoryMock = Mockito.mock(WalletRepository.class);
        WalletDTO walletDTO = new WalletDTO("xyz", 0,0);
        Mockito.when(walletRepositoryMock.getWallet("1")).thenReturn(Optional.of(walletDTO));

        BalanceService balanceService = new BalanceService(walletRepositoryMock);
        Assertions.assertEquals(walletDTO, balanceService.getAccountBalance("1"));
    }

    @Test
    void testWalletNotFoundException() {
        WalletRepository walletRepositoryMock = Mockito.mock(WalletRepository.class);
        Mockito.when(walletRepositoryMock.getWallet("1")).thenReturn(Optional.ofNullable(null));

        BalanceService balanceService = new BalanceService(walletRepositoryMock);
        Assertions.assertThrows(WalletNotFoundException.class, () -> balanceService.getAccountBalance("1"));
    }
}
