package com.chaineeproject.chainee.security.ws;

import java.security.Principal;

public record WsPrincipal(Long userId) implements Principal {
    @Override public String getName() { return String.valueOf(userId); }
}
