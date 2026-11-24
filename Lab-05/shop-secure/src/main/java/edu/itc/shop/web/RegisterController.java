package edu.itc.shop.web;

import edu.itc.shop.domain.Role;
import edu.itc.shop.service.AccountService;
import edu.itc.shop.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RegisterController {

    private final AccountService accounts;

    public RegisterController(AccountService accounts) {
        this.accounts = accounts;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        accounts.register(request.username(), request.password(), Set.of(Role.CUSTOMER));
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterResponse(request.username()));
    }

    public record RegisterResponse(String username) {
    }
}
