package org.enset.ebankingbackend.security.web;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.enset.ebankingbackend.security.entities.AppRole;
import org.enset.ebankingbackend.security.entities.AppUser;
import org.enset.ebankingbackend.security.service.AccountService;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class AccountRestController {
    private AccountService accountService;


    public AccountRestController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/users")
    @PostAuthorize("hasAuthority('USER')")
    public List<AppUser> appUsers()
    {
        return accountService.listUsers();
    }

    @PostMapping("/users")
    @PostAuthorize("hasAuthority('ADMIN')")
    public AppUser saveUser(@RequestBody AppUser appUser)
    {
        return accountService.addNewUser(appUser);
    }

    @PostMapping("/roles")
    @PostAuthorize("hasAuthority('ADMIN')")
    public AppRole saveRole(@RequestBody AppRole appRole)
    {
        return accountService.addNewRole(appRole);
    }

    @PostMapping("/addRoleToUser")
    @PostAuthorize("hasAuthority('ADMIN')")
    public void addRoleToUser(@RequestBody RoleUserForm roleUserForm)
    {
        accountService.addRoleToUser(roleUserForm.getUsername(), roleUserForm.getRolename());
    }

    @GetMapping("/refreshToken")
    public void refreshToken(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception
    {
        String authToken = httpServletRequest.getHeader("Authorization");
        if(authToken!= null && authToken.startsWith("Bearer "))
        {
            try {
                String jwt = authToken.substring(7);
                Algorithm algorithm = Algorithm.HMAC256("mySecret1234");
                JWTVerifier jwtVerifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = jwtVerifier.verify(jwt);
                String username = decodedJWT.getSubject();

                AppUser appUser = accountService.loadUserByUserName(username);
                String jwtAccessToken = JWT.create().
                        withSubject(appUser.getUserName()).
                        withExpiresAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000)).
                        withIssuer(httpServletRequest.getRequestURL().toString())
                        .withClaim("roles", appUser.getAppRoles().stream().map(r->r.getRoleName()).collect(Collectors.toList()))
                        .sign(algorithm);

                Map<String, String> idToken = new HashMap<>();
                idToken.put("access-token", jwtAccessToken);
                idToken.put("refresh-token", jwt);
                httpServletResponse.setContentType("application/json");
                new ObjectMapper().writeValue(httpServletResponse.getOutputStream(), idToken);

            } catch (Exception e) {

                throw e;
            }
        }
        else
        {
            throw new RuntimeException("Refresh token required");
        }
    }

    @GetMapping("/profile")
    public AppUser profile(Principal principal)
    {
        return accountService.loadUserByUserName(principal.getName());
    }
}

@Data
class RoleUserForm {
    private String username;
    private String rolename;

}
