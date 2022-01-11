package com.abitica.service;

import com.abitica.model.WalletDTO;
import com.abitica.model.exceptions.*;
import com.abitica.repository.WalletRepository;
import com.abitica.server.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Optional;

public class DebitServiceTest {
    @Test
    void testMissingWalletException() {
        WalletRepository walletRepositoryMock = Mockito.mock(WalletRepository.class);

        Mockito.when(walletRepositoryMock.getLastTransactionByWalletId("1")).thenReturn(Optional.ofNullable(""));
        Mockito.when(walletRepositoryMock.getWallet("1")).thenReturn(Optional.ofNullable(null));

        DebitService debitService = new DebitService(walletRepositoryMock);

        Transaction transaction = new Transaction("asd", 0d);
        Assertions.assertThrows(MissingWalletException.class,() -> debitService.debitAccount("1", transaction));
    }

    @Test
    void testInsufficientFundsException() {
        WalletRepository walletRepositoryMock = Mockito.mock(WalletRepository.class);

        WalletDTO initialWallet = new WalletDTO("xyz", 1,1);
        Transaction transaction = new Transaction("abc", 2d);

        Mockito.when(walletRepositoryMock.getLastTransactionByWalletId("1")).thenReturn(Optional.ofNullable("xyz"));
        Mockito.when(walletRepositoryMock.getWallet("1")).thenReturn(Optional.ofNullable(initialWallet));

        DebitService debitService = new DebitService(walletRepositoryMock);

        Assertions.assertThrows(InsufficientFundsException.class,() -> debitService.debitAccount("1", transaction));
    }

    @Test
    void testDuplicatedRequestException() {
        WalletRepository walletRepositoryMock = Mockito.mock(WalletRepository.class);

        Transaction transaction = new Transaction("xyz", 2d);

        Mockito.when(walletRepositoryMock.getLastTransactionByWalletId("1")).thenReturn(Optional.ofNullable("xyz"));
        DebitService debitService = new DebitService(walletRepositoryMock);

        Assertions.assertThrows(DuplicateRequestException.class, () -> debitService.debitAccount("1", transaction));
    }



    @Test
    void testDebitWallet() throws DuplicateRequestException, InsufficientFundsException, MissingWalletException {
        WalletRepository walletRepositoryMock = Mockito.mock(WalletRepository.class);

        WalletDTO initialWallet = new WalletDTO("xyz", 1,3);
        Transaction transaction = new Transaction("abc", 2d);
        WalletDTO updatedWallet = new WalletDTO("abc", 2, 1);

        Mockito.when(walletRepositoryMock.getWallet("1")).thenAnswer(new Answer<Optional<WalletDTO>>() {
            boolean firstCall = true;
            @Override
            public Optional<WalletDTO> answer(InvocationOnMock invocationOnMock) {
                if(firstCall) {
                    firstCall = false;
                    return Optional.ofNullable(initialWallet);
                }
                return Optional.ofNullable(updatedWallet);
            }
        });

        Mockito.when(walletRepositoryMock.getLastTransactionByWalletId("1")).thenReturn(Optional.ofNullable("xyz"));
        DebitService debitService = new DebitService(walletRepositoryMock);
        Assertions.assertEquals(updatedWallet, debitService.debitAccount("1", transaction));
    }

}
