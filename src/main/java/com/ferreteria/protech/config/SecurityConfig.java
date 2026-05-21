package com.ferreteria.protech.config;

import com.ferreteria.protech.model.User;
import com.ferreteria.protech.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.stream.Collectors;

/**
 * Configuración de Spring Security - Session Based Authentication.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    user.getActivo(),
                    true, true, true,
                    user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getNombre()))
                            .collect(Collectors.toList())
            );
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/registro", "/css/**", "/js/**", "/images/**", "/tienda", "/api/tienda/productos", "/api/tienda/categorias").permitAll()
                .requestMatchers("/api/productos/**", "/api/categorias/**", "/api/auth/**").permitAll()
                .requestMatchers("/tienda/checkout", "/api/tienda/checkout", "/mis-compras").hasRole("CLIENTE")
                .requestMatchers("/operario/**", "/api/ventas/**").hasRole("OPERARIO")
                .requestMatchers("/proveedor/**", "/api/proveedor/**").hasRole("PROVEEDOR")
                .requestMatchers("/api/kardex/**").hasAnyRole("ADMIN", "OPERARIO")
                .anyRequest().hasRole("ADMIN")
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    boolean isCliente = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
                    boolean isProveedor = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PROVEEDOR"));
                    boolean isOperario = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OPERARIO"));
                    boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    
                    if (isCliente) {
                        response.sendRedirect("/tienda");
                    } else if (isProveedor) {
                        response.sendRedirect("/proveedor");
                    } else if (isOperario) {
                        response.sendRedirect("/operario");
                    } else if (isAdmin) {
                        response.sendRedirect("/admin");
                    } else {
                        response.sendRedirect("/");
                    }
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}
