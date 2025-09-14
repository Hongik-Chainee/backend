package com.chaineeproject.chainee.dev;

import com.chaineeproject.chainee.entity.User;
import com.chaineeproject.chainee.repository.UserRepository;
import com.chaineeproject.chainee.security.CustomOauth2UserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
@Profile("dev")
public class DevMockLoginController {

    private final UserRepository userRepository;

    @PostMapping("/mock-login")
    public Map<String, Object> mockLogin(@RequestBody Map<String,String> body,
                                         HttpServletRequest req, HttpServletResponse res) {
        String email = body.get("email");
        User user = userRepository.findByEmail(email).orElseGet(() ->
                userRepository.save(User.builder()
                        .email(email).provider("google").providerId("dev-mock").build())
        );

        CustomOauth2UserDetails principal = new CustomOauth2UserDetails(user, Map.of(), false);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
        new HttpSessionSecurityContextRepository().saveContext(ctx, req, res);

        return Map.of("mockLogin", true, "email", email);
    }
}
