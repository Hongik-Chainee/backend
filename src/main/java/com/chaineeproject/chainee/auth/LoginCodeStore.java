// src/main/java/com/chaineeproject/chainee/auth/LoginCodeStore.java
package com.chaineeproject.chainee.auth;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginCodeStore {

    private record Entry(Long userId, long expMs) {}
    private final ConcurrentHashMap<String, Entry> map = new ConcurrentHashMap<>();

    /** 코드 저장(유효기간 포함) */
    public void put(String code, Long userId, Duration ttl) {
        long exp = System.currentTimeMillis() + ttl.toMillis();
        map.put(code, new Entry(userId, exp));
    }

    /** 1회용 소비(사용과 동시에 제거). 만료/미존재면 null */
    public Long consume(String code) {
        Entry e = map.remove(code);
        if (e == null) return null;
        if (System.currentTimeMillis() > e.expMs) return null;
        return e.userId;
    }
}
