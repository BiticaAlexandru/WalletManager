package com.abitica.service;

import com.abitica.model.WalletDTO;
import com.abitica.model.exceptions.DuplicateRequestException;
import com.abitica.model.exceptions.WalletNotPersistedException;
import com.abitica.repository.WalletRepository;
import com.abitica.server.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Optional;

public class CreditServiceTest {

    @Test
    void testCreditNonexistentWallet() throws DuplicateRequestException, WalletNotPersistedException {
        WalletRepository walletRepositoryMock = Mockito.mock(WalletRepository.class);

        WalletDTO initialWallet = new WalletDTO("", 0,0);
        Transaction transaction = new Transaction("abc", 2d);
        WalletDTO updatedWallet = new WalletDTO("abc", 1, 2);

        Mockito.when(walletRepositoryMock.createNewWallet("1")).thenReturn(initialWallet);
        Mockito.when(walletRepositoryMock.getLastTransactionByWalletId("1")).thenReturn(Optional.ofNullable(""));
        Mockito.when(walletRepositoryMock.getWallet("1")).thenReturn(Optional.ofNullable(updatedWallet));

        CreditService creditService = new CreditService(walletRepositoryMock);

        Assertions.assertEquals(updatedWallet, creditService.creditAccount("1", transaction));
    }

    @Test
    void testCreditExistentWallet() throws DuplicateRequestException, WalletNotPersistedException {
        WalletRepository walletRepositoryMock = Mockito.mock(WalletRepository.class);

        WalletDTO initialWallet = new WalletDTO("xyz", 1,3);
        Transaction transaction = new Transaction("abc", 2d);
        WalletDTO updatedWallet = new WalletDTO("abc", 2, 5);

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
        CreditService creditService = new CreditService(walletRepositoryMock);

        Assertions.assertEquals(updatedWallet, creditService.creditAccount("1", transaction));
    }

    @Test
    void testWalletNotPersistedException() {
        WalletRepository walletRepositoryMock = Mockito.mock(WalletRepository.class);

        WalletDTO initialWallet = new WalletDTO("", 0,0);
        Transaction transaction = new Transaction("abc", 2d);

        Mockito.when(walletRepositoryMock.createNewWallet("1")).thenReturn(initialWallet);
        Mockito.when(walletRepositoryMock.getLastTransactionByWalletId("1")).thenReturn(Optional.ofNullable(""));
        Mockito.when(walletRepositoryMock.getWallet("1")).thenReturn(Optional.ofNullable(null));

        CreditService creditService = new CreditService(walletRepositoryMock);

        Assertions.assertThrows(WalletNotPersistedException.class, ()->creditService.creditAccount("1", transaction));
    }

    @Test
    void testDuplicatedRequestException() {
        WalletRepository walletRepositoryMock = Mockito.mock(WalletRepository.class);

        Transaction transaction = new Transaction("xyz", 2d);

        Mockito.when(walletRepositoryMock.getLastTransactionByWalletId("1")).thenReturn(Optional.ofNullable("xyz"));
        CreditService creditService = new CreditService(walletRepositoryMock);

        Assertions.assertThrows(DuplicateRequestException.class, ()->creditService.creditAccount("1", transaction));
    }
}
