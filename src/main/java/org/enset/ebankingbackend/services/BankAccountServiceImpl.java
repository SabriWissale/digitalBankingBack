package org.enset.ebankingbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enset.ebankingbackend.dtos.*;
import org.enset.ebankingbackend.entities.*;
import org.enset.ebankingbackend.enums.OperationType;
import org.enset.ebankingbackend.exceptions.BalanceNotSufficientException;
import org.enset.ebankingbackend.exceptions.BankAccountNotFoundException;
import org.enset.ebankingbackend.exceptions.CustomerNotFoundException;
import org.enset.ebankingbackend.mappers.BankAccountMapperImpl;
import org.enset.ebankingbackend.repositories.AccountOperationRepository;
import org.enset.ebankingbackend.repositories.BankAccountRepository;
import org.enset.ebankingbackend.repositories.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class BankAccountServiceImpl implements BankAccountService {
    private CustomerRepository customerRepository;
    private BankAccountRepository bankAccountRepository;
    private AccountOperationRepository accountOperationRepository;
    private BankAccountMapperImpl dtoMapper;

    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        log.info("Saving new Customer");
        Customer customer=dtoMapper.fromCustomerDTO(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public BankAccountDTO saveAccount(BankAccountDTO bankAccountDTO) throws CustomerNotFoundException {
        log.info("Saving new account");
        if(bankAccountDTO instanceof SavingBankAccountDTO)
        {
            Customer customer=customerRepository.findById(((SavingBankAccountDTO) bankAccountDTO).getCustomerDTO().getId()).orElse(null);
            if(customer==null)
                throw new CustomerNotFoundException("Customer not found");

            SavingBankAccountDTO savingBankAccountDTO=(SavingBankAccountDTO) bankAccountDTO;
            savingBankAccountDTO.setId(UUID.randomUUID().toString());
            SavingAccount savingAccount=dtoMapper.fromSavingBankAccountDTO(savingBankAccountDTO);
            SavingAccount savedAccount = bankAccountRepository.save(savingAccount);
            return dtoMapper.fromSavingBankAccount(savedAccount);
        }
        if(bankAccountDTO instanceof CurrentBankAccountDTO)
        {
            Customer customer=customerRepository.findById(((CurrentBankAccountDTO) bankAccountDTO).getCustomerDTO().getId()).orElse(null);
            if(customer==null)
                throw new CustomerNotFoundException("Customer not found");

            CurrentBankAccountDTO currentBankAccountDTO=(CurrentBankAccountDTO) bankAccountDTO;
            currentBankAccountDTO.setId(UUID.randomUUID().toString());
            CurrentAccount currentAccount=dtoMapper.fromCurrentBankAccountDTO(currentBankAccountDTO);
            CurrentAccount savedAccount = bankAccountRepository.save(currentAccount);
            return dtoMapper.fromCurrentBankAccount(savedAccount);
        }
        return null;
    }

    @Override
    public CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException {
        Customer customer=customerRepository.findById(customerId).orElse(null);
        if(customer==null)
            throw new CustomerNotFoundException("Customer not found");
        CurrentAccount currentAccount=new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setCreatedAt(new Date());
        currentAccount.setBalance(initialBalance);
        currentAccount.setOverDraft(overDraft);
        currentAccount.setCustomer(customer);
        CurrentAccount savedBankAccount = bankAccountRepository.save(currentAccount);
        return dtoMapper.fromCurrentBankAccount(savedBankAccount);
    }

    @Override
    public SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException {
        Customer customer=customerRepository.findById(customerId).orElse(null);
        if(customer==null)
            throw new CustomerNotFoundException("Customer not found");
        SavingAccount savingAccount=new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setCreatedAt(new Date());
        savingAccount.setBalance(initialBalance);
        savingAccount.setInterestRate(interestRate);
        savingAccount.setCustomer(customer);
        SavingAccount savedBankAccount = bankAccountRepository.save(savingAccount);
        return dtoMapper.fromSavingBankAccount(savedBankAccount);
    }



    @Override
    public BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId)
                .orElseThrow(()->new BankAccountNotFoundException("BankAccount not found"));
        if(bankAccount instanceof SavingAccount){
            SavingAccount savingAccount= (SavingAccount) bankAccount;
            return dtoMapper.fromSavingBankAccount(savingAccount);
        } else {
            CurrentAccount currentAccount= (CurrentAccount) bankAccount;
            return dtoMapper.fromCurrentBankAccount(currentAccount);
        }
    }

    @Override
    public void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId)
                .orElseThrow(()->new BankAccountNotFoundException("BankAccount not found"));
        if(bankAccount.getBalance()<amount)
            throw new BalanceNotSufficientException("Balance not sufficient");
        AccountOperation accountOperation=new AccountOperation();
        accountOperation.setType(OperationType.DEBIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance()-amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void credit(String accountId, double amount, String description) throws BankAccountNotFoundException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId)
                .orElseThrow(()->new BankAccountNotFoundException("BankAccount not found"));
        AccountOperation accountOperation=new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance()+amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void transfer(String accountIdSource, String accountIdDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException {
        debit(accountIdSource,amount,"Transfer to "+accountIdDestination);
        credit(accountIdDestination,amount,"Transfer from "+accountIdSource);
    }
    @Override
    public List<BankAccountDTO> bankAccountList(){
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        List<BankAccountDTO> bankAccountDTOS = bankAccounts.stream().map(bankAccount -> {
            if (bankAccount instanceof SavingAccount) {
                SavingAccount savingAccount = (SavingAccount) bankAccount;
                return dtoMapper.fromSavingBankAccount(savingAccount);
            } else {
                CurrentAccount currentAccount = (CurrentAccount) bankAccount;
                return dtoMapper.fromCurrentBankAccount(currentAccount);
            }
        }).collect(Collectors.toList());
        return bankAccountDTOS;
    }



    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer Not found"));
        return dtoMapper.fromCustomer(customer);
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        log.info("Saving new Customer");
        Customer customer=dtoMapper.fromCustomerDTO(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }
    @Override
    public void deleteCustomer(Long customerId){
        customerRepository.deleteById(customerId);
    }
    @Override
    public List<AccountOperationDTO> accountHistory(String accountId){
        List<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountId);
        return accountOperations.stream().map(op->dtoMapper.fromAccountOperation(op)).collect(Collectors.toList());
    }

    @Override
    public AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId).orElse(null);
        if(bankAccount==null) throw new BankAccountNotFoundException("Account not Found");
        Page<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountIdOrderByOperationDateDesc(accountId, PageRequest.of(page, size));
        AccountHistoryDTO accountHistoryDTO=new AccountHistoryDTO();
        List<AccountOperationDTO> accountOperationDTOS = accountOperations.getContent().stream().map(op -> dtoMapper.fromAccountOperation(op)).collect(Collectors.toList());
        accountHistoryDTO.setAccountOperationDTOS(accountOperationDTOS);
        accountHistoryDTO.setAccountId(bankAccount.getId());
        accountHistoryDTO.setBalance(bankAccount.getBalance());
        accountHistoryDTO.setCurrentPage(page);
        accountHistoryDTO.setPageSize(size);
        accountHistoryDTO.setTotalPages(accountOperations.getTotalPages());
        return accountHistoryDTO;
    }

    @Override
    public CustomerPageableDTO searchCustomers(String keyword, int page, int size) {
        CustomerPageableDTO customerPageableDTO = new CustomerPageableDTO();
        Page<Customer> customers = customerRepository.searchCustomer(keyword, PageRequest.of(page, size));
        List<CustomerDTO> customerDTOS = customers.getContent().stream().map(c -> dtoMapper.fromCustomer(c)).collect(Collectors.toList());
        customerPageableDTO.setCurrentPage(page);
        customerPageableDTO.setPageSize(size);
        customerPageableDTO.setTotalPages(customers.getTotalPages());
        customerPageableDTO.setCustomerDTOS(customerDTOS);
        return customerPageableDTO;
    }

    @Override
    public CustomerPageableDTO listCustomers(int page, int size) {
        CustomerPageableDTO customerPageableDTO = new CustomerPageableDTO();
        Page<Customer> customers = customerRepository.findAll(PageRequest.of(page, size));
        List<CustomerDTO> customerDTOS = customers.getContent().stream().map(c -> dtoMapper.fromCustomer(c)).collect(Collectors.toList());
        customerPageableDTO.setCurrentPage(page);
        customerPageableDTO.setPageSize(size);
        customerPageableDTO.setTotalPages(customers.getTotalPages());
        customerPageableDTO.setCustomerDTOS(customerDTOS);
        return customerPageableDTO;
    }

    @Override
    public List<CustomerDTO> listCustomersList() {

        List<Customer> customers = customerRepository.findAll();
        List<CustomerDTO> customerDTOS = customers.stream().map(c -> dtoMapper.fromCustomer(c)).collect(Collectors.toList());
        return customerDTOS;
    }
// return list of a customer's accounts
    @Override
    public BankAccountsPageable getCustomerAccounts(Long customerId, int page, int size) {
        BankAccountsPageable customerAccountsPageable = new BankAccountsPageable();

        Page<BankAccount> bankAccounts = bankAccountRepository.findByCustomerId(customerId, PageRequest.of(page, size));

        List<BankAccountDTO> bankAccountDTOS = bankAccounts.stream().map(bankAccount -> {
            if (bankAccount instanceof SavingAccount) {
                SavingAccount savingAccount = (SavingAccount) bankAccount;
                return dtoMapper.fromSavingBankAccount(savingAccount);
            } else {
                CurrentAccount currentAccount = (CurrentAccount) bankAccount;
                return dtoMapper.fromCurrentBankAccount(currentAccount);
            }
        }).collect(Collectors.toList());

        customerAccountsPageable.setCurrentPage(page);
        customerAccountsPageable.setPageSize(size);
        customerAccountsPageable.setTotalPages(bankAccounts.getTotalPages());
        customerAccountsPageable.setBankAccountDTOS(bankAccountDTOS);
        return customerAccountsPageable;
    }


    @Override
    public BankAccountsPageable getBankAccounts(int page, int size) {
        BankAccountsPageable accountsPageable = new BankAccountsPageable();

        Page<BankAccount> bankAccounts = bankAccountRepository.findAll(PageRequest.of(page, size));

        List<BankAccountDTO> bankAccountDTOS = bankAccounts.stream().map(bankAccount -> {
            if (bankAccount instanceof SavingAccount) {
                SavingAccount savingAccount = (SavingAccount) bankAccount;
                return dtoMapper.fromSavingBankAccount(savingAccount);
            } else {
                CurrentAccount currentAccount = (CurrentAccount) bankAccount;
                return dtoMapper.fromCurrentBankAccount(currentAccount);
            }
        }).collect(Collectors.toList());

        accountsPageable.setCurrentPage(page);
        accountsPageable.setPageSize(size);
        accountsPageable.setTotalPages(bankAccounts.getTotalPages());
        accountsPageable.setBankAccountDTOS(bankAccountDTOS);
        return accountsPageable;
    }

    @Override
    public BankAccountsPageable searchAccounts(String keyword, int page, int size) {
        BankAccountsPageable accountsPageable = new BankAccountsPageable();

        Page<BankAccount> bankAccounts = bankAccountRepository.searchBankAccount(keyword, PageRequest.of(page, size));

        List<BankAccountDTO> bankAccountDTOS = bankAccounts.stream().map(bankAccount -> {
            if (bankAccount instanceof SavingAccount) {
                SavingAccount savingAccount = (SavingAccount) bankAccount;
                return dtoMapper.fromSavingBankAccount(savingAccount);
            } else {
                CurrentAccount currentAccount = (CurrentAccount) bankAccount;
                return dtoMapper.fromCurrentBankAccount(currentAccount);
            }
        }).collect(Collectors.toList());

        accountsPageable.setCurrentPage(page);
        accountsPageable.setPageSize(size);
        accountsPageable.setTotalPages(bankAccounts.getTotalPages());
        accountsPageable.setBankAccountDTOS(bankAccountDTOS);
        return accountsPageable;
    }

    @Override
    public void deleteAccount(String accountId){
        bankAccountRepository.deleteById(accountId);
    }

}
