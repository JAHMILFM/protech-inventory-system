package com.ferreteria.protech.controller;

import com.ferreteria.protech.dto.RegistroDTO;
import com.ferreteria.protech.model.Role;
import com.ferreteria.protech.model.User;
import com.ferreteria.protech.repository.RoleRepository;
import com.ferreteria.protech.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthRestController(UserRepository userRepo, RoleRepository roleRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrarCliente(@Valid @RequestBody RegistroDTO dto) {
        if (userRepo.existsByUsername(dto.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "El nombre de usuario ya está en uso"));
        }

        Role clienteRole = roleRepo.findByNombre("CLIENTE")
                .orElseThrow(() -> new RuntimeException("Error interno: Rol CLIENTE no encontrado"));

        User newUser = new User();
        newUser.setUsername(dto.getUsername());
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        newUser.setNombreCompleto(dto.getNombreCompleto());
        newUser.setEmail(dto.getEmail());
        newUser.setTelefono(dto.getTelefono());
        newUser.setRoles(Set.of(clienteRole));

        userRepo.save(newUser);

        return ResponseEntity.ok(Map.of("success", true, "message", "Registro exitoso"));
    }
}
