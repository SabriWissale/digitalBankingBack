package org.enset.ebankingbackend.web;

import org.enset.ebankingbackend.dtos.*;
import org.enset.ebankingbackend.services.BankAccountService;
import org.enset.ebankingbackend.exceptions.BalanceNotSufficientException;
import org.enset.ebankingbackend.exceptions.BankAccountNotFoundException;
import org.enset.ebankingbackend.exceptions.CustomerNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
public class BankAccountRestAPI {
    private BankAccountService bankAccountService;

    public BankAccountRestAPI(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping("/accounts/{accountId}")
    public BankAccountDTO getBankAccount(@PathVariable String accountId) throws BankAccountNotFoundException {
        return bankAccountService.getBankAccount(accountId);
    }

    @GetMapping("/accounts/{accountId}/operations")
    public List<AccountOperationDTO> getHistory(@PathVariable String accountId){
        return bankAccountService.accountHistory(accountId);
    }

    @GetMapping("/accounts/{accountId}/pageOperations")
    public AccountHistoryDTO getAccountHistory(
            @PathVariable String accountId,
            @RequestParam(name="page",defaultValue = "0") int page,
            @RequestParam(name="size",defaultValue = "5")int size) throws BankAccountNotFoundException {
        return bankAccountService.getAccountHistory(accountId,page,size);
    }
    @PostMapping("/accounts/debit")
    public DebitDTO debit(@RequestBody DebitDTO debitDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        this.bankAccountService.debit(debitDTO.getAccountId(),debitDTO.getAmount(),debitDTO.getDescription());
        return debitDTO;
    }
    @PostMapping("/accounts/credit")
    public CreditDTO credit(@RequestBody CreditDTO creditDTO) throws BankAccountNotFoundException {
        this.bankAccountService.credit(creditDTO.getAccountId(),creditDTO.getAmount(),creditDTO.getDescription());
        return creditDTO;
    }
    @PostMapping("/accounts/transfer")
    public void transfer(@RequestBody TransferRequestDTO transferRequestDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        this.bankAccountService.transfer(
                transferRequestDTO.getAccountSource(),
                transferRequestDTO.getAccountDestination(),
                transferRequestDTO.getAmount());
    }

    @GetMapping("/accounts")
    public BankAccountsPageable listAccounts(@RequestParam(name="page",defaultValue = "0") int page,
                                             @RequestParam(name="size",defaultValue = "5")int size){
        return bankAccountService.getBankAccounts(page, size);
    }

    @GetMapping("/accounts/search")
    public BankAccountsPageable searchBankAccounts(@RequestParam(name = "keyword",defaultValue = "") String keyword, @RequestParam(defaultValue = "0")int page,
                                               @RequestParam(defaultValue = "5")int size){
        return bankAccountService.searchAccounts("%"+keyword+"%", page, size);
    }


    @GetMapping("/accounts/customer/{customerId}")
    public BankAccountsPageable getCustomerAccounts(@PathVariable Long customerId,
                                                    @RequestParam(name="page",defaultValue = "0") int page,
                                                    @RequestParam(name="size",defaultValue = "5")int size) throws BankAccountNotFoundException {
        return bankAccountService.getCustomerAccounts(customerId, page, size);
    }

    @PostMapping("/accounts/savingAccount")
    public BankAccountDTO saveSavingAccount(@RequestBody SavingBankAccountDTO bankAccountDTO) throws CustomerNotFoundException {
        return bankAccountService.saveAccount(bankAccountDTO);

    }

    @PostMapping("/accounts/currentAccount")
    public BankAccountDTO saveCurrentAccount(@RequestBody CurrentBankAccountDTO bankAccountDTO) throws CustomerNotFoundException {
        return bankAccountService.saveAccount(bankAccountDTO);

    }

    @DeleteMapping("/accounts/delete/{id}")
    public void deleteAccount(@PathVariable String id){
        bankAccountService.deleteAccount(id);
    }

}
