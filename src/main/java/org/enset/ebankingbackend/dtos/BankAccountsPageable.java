package org.enset.ebankingbackend.dtos;

import lombok.Data;

import java.util.List;

@Data
public class BankAccountsPageable {

    private int currentPage;
    private int totalPages;
    private int pageSize;
    private List<BankAccountDTO> bankAccountDTOS;
}
