package org.enset.ebankingbackend.security.service;

import org.enset.ebankingbackend.security.entities.AppRole;
import org.enset.ebankingbackend.security.entities.AppUser;

import java.util.List;

public interface AccountService {

    AppUser addNewUser(AppUser appUser);
    AppRole addNewRole(AppRole appRole);
    void addRoleToUser(String username, String rolename);
    AppUser loadUserByUserName(String userName);
    List<AppUser> listUsers();


}
