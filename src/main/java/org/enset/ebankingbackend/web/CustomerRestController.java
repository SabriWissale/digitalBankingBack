package org.enset.ebankingbackend.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.enset.ebankingbackend.dtos.CustomerDTO;
import org.enset.ebankingbackend.dtos.CustomerPageableDTO;
import org.enset.ebankingbackend.exceptions.CustomerNotFoundException;
import org.enset.ebankingbackend.services.BankAccountService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@CrossOrigin("*")
public class CustomerRestController {
    private BankAccountService bankAccountService;
    @GetMapping("/customers")
    public CustomerPageableDTO customers(@RequestParam(defaultValue = "0")int page,
                                       @RequestParam(defaultValue = "5")int size)
    {
        return bankAccountService.listCustomers(page, size);
    }

    @GetMapping("/customers/list")
    public List<CustomerDTO> customers()
    {
        return bankAccountService.listCustomersList();
    }

    @GetMapping("/customers/search")
    public CustomerPageableDTO searchCustomers(@RequestParam(name = "keyword",defaultValue = "") String keyword, @RequestParam(defaultValue = "0")int page,
                                               @RequestParam(defaultValue = "5")int size){
        return bankAccountService.searchCustomers("%"+keyword+"%", page, size);
    }


    @GetMapping("/customers/{id}")
    public CustomerDTO getCustomer(@PathVariable(name = "id") Long customerId) throws CustomerNotFoundException {
        return bankAccountService.getCustomer(customerId);
    }

    @PostMapping("/customers")
    public CustomerDTO saveCustomer(@RequestBody CustomerDTO customerDTO){
        return bankAccountService.saveCustomer(customerDTO);
    }
    @PutMapping("/customers/{customerId}")
    public CustomerDTO updateCustomer(@PathVariable Long customerId, @RequestBody CustomerDTO customerDTO){
        customerDTO.setId(customerId);
        return bankAccountService.updateCustomer(customerDTO);
    }
    @DeleteMapping("/customers/{id}")
    public void deleteCustomer(@PathVariable Long id){
        bankAccountService.deleteCustomer(id);
    }
}
