package com.industrial.saude.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    
    @GetMapping("/")
    public String index(HttpServletRequest request) {
        return "redirect:/login";
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
    
    @GetMapping("/colaboradores")
    public String colaboradores() {
        return "colaboradores";
    }
    
    @GetMapping("/atendimento")
    public String atendimento() {
        return "atendimento";
    }
    
    @GetMapping("/acidentes")
    public String acidentes() {
        return "acidentes";
    }
    
    @GetMapping("/agendamentos")
    public String agendamentos() {
        return "agendamentos";
    }
    
    @GetMapping("/estoque")
    public String estoque() {
        return "estoque";
    }

    @GetMapping("/relatorios")
    public String relatorios() {
        return "relatorios";
    }

    @GetMapping("/usuarios")
    public String usuarios() {
        return "usuarios";
    }
}