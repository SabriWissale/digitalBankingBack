package org.enset.ebankingbackend.dtos;

import lombok.Data;

import java.util.List;
@Data
public class CustomerPageableDTO {
    private int currentPage;
    private int totalPages;
    private int pageSize;
    private List<CustomerDTO> customerDTOS;
}
