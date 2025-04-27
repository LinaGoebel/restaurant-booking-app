package de.restaurant_booking_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Включение CSRF защиты для веб-форм
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**") // Отключаем только для API если используете токены
                )
                .authorizeHttpRequests(authorize -> authorize
                        // Публичные ресурсы
                        .requestMatchers("/", "/home", "/css/**", "/js/**", "/images/**").permitAll()
                        // Swagger/API документация
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // H2 консоль (только для разработки)
                        .requestMatchers("/h2-console/**").permitAll()
                        // Защищенные ресурсы требуют аутентификации
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard")
                        .permitAll()
                )
                .logout(logout -> logout
                        .permitAll()
                )
                // Для H2 консоли (только для разработки)
                .headers(headers -> headers.frameOptions().sameOrigin());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
