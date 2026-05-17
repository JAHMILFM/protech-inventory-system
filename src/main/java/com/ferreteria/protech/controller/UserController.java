package com.ferreteria.protech.controller;

import com.ferreteria.protech.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll().stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("username", u.getUsername());
            map.put("nombre", u.getNombreCompleto());
            map.put("email", u.getEmail());
            map.put("activo", u.getActivo());
            if (!u.getRoles().isEmpty()) {
                map.put("rol", u.getRoles().iterator().next().getNombre());
            } else {
                map.put("rol", "SIN_ROL");
            }
            return map;
        }).collect(Collectors.toList());
    }
}
