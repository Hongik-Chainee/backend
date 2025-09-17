package com.chaineeproject.chainee.jwt;

import com.chaineeproject.chainee.auth.KeyProvider;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/auth")
public class JwkController {
    private final KeyProvider keys;
    public JwkController(KeyProvider keys){ this.keys = keys; }

    @GetMapping("/jwks.json")
    public Map<String, Object> jwks() { return keys.jwkSet().toJSONObject(); }
}