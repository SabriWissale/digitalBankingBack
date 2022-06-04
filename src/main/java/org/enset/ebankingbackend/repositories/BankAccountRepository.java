package org.enset.ebankingbackend.repositories;

import org.enset.ebankingbackend.entities.BankAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BankAccountRepository extends JpaRepository<BankAccount,String> {

    @Query("select b from BankAccount b  where b.id like :kw")
    Page<BankAccount> searchBankAccount(@Param("kw") String keyword, Pageable pageable);


    Page<BankAccount> findByCustomerId(Long customerId, Pageable pageable);
}
