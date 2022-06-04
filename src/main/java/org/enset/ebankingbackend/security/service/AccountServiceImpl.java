package org.enset.ebankingbackend.security.service;

import lombok.AllArgsConstructor;
import org.enset.ebankingbackend.security.entities.AppRole;
import org.enset.ebankingbackend.security.entities.AppUser;
import org.enset.ebankingbackend.security.repositories.AppRoleRepository;
import org.enset.ebankingbackend.security.repositories.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class AccountServiceImpl implements AccountService{

    private AppRoleRepository appRoleRepository;
    private AppUserRepository appUserRepository;
    private PasswordEncoder passwordEncoder;


    @Override
    public AppUser addNewUser(AppUser appUser) {
        String pw = appUser.getPassword();
        appUser.setPassword(passwordEncoder.encode(pw));
        return appUserRepository.save(appUser);
    }

    @Override
    public AppRole addNewRole(AppRole appRole) {
        return appRoleRepository.save(appRole);
    }

    @Override
    public void addRoleToUser(String username, String rolename) {

        AppUser user = appUserRepository.findByUserName(username);
        AppRole role = appRoleRepository.findByRoleName(rolename);
        user.getAppRoles().add(role);


    }

    @Override
    public AppUser loadUserByUserName(String userName) {
        return appUserRepository.findByUserName(userName);
    }

    @Override
    public List<AppUser> listUsers() {

        return appUserRepository.findAll();
    }
}
