package com.industrial.saude.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class teste {
    public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder().encode("admin123"));
    }
}
