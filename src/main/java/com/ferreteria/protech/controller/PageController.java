package com.ferreteria.protech.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para servir páginas Thymeleaf.
 * Redirige al usuario según su rol tras el login.
 */
@Controller
public class PageController {

    /**
     * Raíz: redirige según el rol del usuario autenticado.
     * ADMIN  → /admin
     * OPERARIO → /operario
     */
    @GetMapping("/")
    public String root(Authentication auth) {
        if (auth != null) {
            if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_OPERARIO"))) {
                return "redirect:/operario";
            }
            if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PROVEEDOR"))) {
                return "redirect:/proveedor";
            }
            if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENTE"))) {
                return "redirect:/tienda";
            }
            if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                return "redirect:/admin";
            }
        }
        return "redirect:/tienda";
    }

    @GetMapping("/admin")
    public String admin(Authentication auth) {
        if (auth != null && auth.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_OPERARIO"))) {
            return "redirect:/operario";
        }
        return "index";
    }

    @GetMapping("/operario")
    public String operario(Authentication auth) {
        if (auth == null) return "redirect:/login";
        return "operario";
    }

    @GetMapping("/proveedor")
    public String proveedor(Authentication auth) {
        if (auth == null) return "redirect:/login";
        return "proveedor";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registro() {
        return "registro";
    }

    @GetMapping("/tienda")
    public String tienda() {
        return "tienda";
    }

    @GetMapping("/tienda/checkout")
    public String checkout() {
        return "checkout";
    }
}
