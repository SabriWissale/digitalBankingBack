package org.enset.ebankingbackend.services;

import org.enset.ebankingbackend.dtos.*;
import org.enset.ebankingbackend.exceptions.BalanceNotSufficientException;
import org.enset.ebankingbackend.exceptions.BankAccountNotFoundException;
import org.enset.ebankingbackend.exceptions.CustomerNotFoundException;

import java.util.List;
public interface BankAccountService {
    CustomerDTO saveCustomer(CustomerDTO customerDTO);
    CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException;
    SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException;
    CustomerPageableDTO listCustomers(int page, int size);
    List<CustomerDTO> listCustomersList();
    BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException;
    void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException;
    void credit(String accountId, double amount, String description) throws BankAccountNotFoundException;
    void transfer(String accountIdSource, String accountIdDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException;

    List<BankAccountDTO> bankAccountList();
    BankAccountsPageable getBankAccounts(int page, int size);
    BankAccountsPageable searchAccounts(String keyword, int page, int size);
    void deleteAccount(String accountId);
    BankAccountDTO saveAccount(BankAccountDTO bankAccountDTO) throws CustomerNotFoundException;

    BankAccountsPageable getCustomerAccounts(Long customerId, int page, int size);

    CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException;

    CustomerDTO updateCustomer(CustomerDTO customerDTO);

    void deleteCustomer(Long customerId);

    List<AccountOperationDTO> accountHistory(String accountId);

    AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException;

    CustomerPageableDTO searchCustomers(String keyword, int page, int size);
}
