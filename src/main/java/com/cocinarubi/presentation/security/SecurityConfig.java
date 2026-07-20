package com.cocinarubi.presentation.security;

import com.cocinarubi.presentation.filter.CorrelationFilter;
import com.cocinarubi.presentation.filter.GlobalRateLimitFilter;
import com.cocinarubi.presentation.filter.LoginRateLimitFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;
    private final EntryPointNoAutorizado entryPointNoAutorizado;
    private final ManejadorAccesoDenegado manejadorAccesoDenegado;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtService jwtService,
                          UsuarioDetailsService usuarioDetailsService,
                          EntryPointNoAutorizado entryPointNoAutorizado,
                          ManejadorAccesoDenegado manejadorAccesoDenegado,
                          ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.usuarioDetailsService = usuarioDetailsService;
        this.entryPointNoAutorizado = entryPointNoAutorizado;
        this.manejadorAccesoDenegado = manejadorAccesoDenegado;
        this.objectMapper = objectMapper;
    }

    // ── Filtros registrados como @Bean (NO como @Component) ─────────────────
    // Si fueran @Component, Spring Boot los detectaría y los registraría dos veces:
    // una en el servlet container y otra aquí, ejecutando cada filtro dos veces por request.

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, usuarioDetailsService);
    }

    @Bean
    public CorrelationFilter correlationFilter() {
        return new CorrelationFilter();
    }

    @Bean
    public LoginRateLimitFilter loginRateLimitFilter() {
        return new LoginRateLimitFilter(objectMapper);
    }

    @Bean
    public GlobalRateLimitFilter globalRateLimitFilter() {
        return new GlobalRateLimitFilter(objectMapper);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ── Cadena de seguridad ──────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers
                    .frameOptions(frame -> frame.deny())
                    .contentTypeOptions(Customizer.withDefaults())
                    .referrerPolicy(referrer ->
                            referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
            )
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth

                    // ── Rutas públicas ────────────────────────────────────
                    .requestMatchers(
                            "/auth/**",
                            "/v3/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/cocina-rubi-doc-api.html",
                            "/api-doc.html",
                            "/actuator/health"
                    ).permitAll()

                    .requestMatchers("/actuator/**").authenticated()

                    // ── Solo JEFA_COCINA ──────────────────────────────────
                    .requestMatchers(
                            "/usuario/**",
                            "/auditoria/**"
                        ).hasRole("JEFA_COCINA")
                        
                        // ── Ambos roles ───────────────────────────────────────
                        .requestMatchers(
                            "/comida/**",
                            "/desayuno/**",
                            "/ruta/**",
                            "/tarifa-especial/**",
                            "/horario-atencion/**",
                            "/anuncio/**",
                            "/codigoCliente/**",
                            "/complemento/**",
                            "/basico/**",
                            "/registro-cliente/**",
                            "/cliente/**",
                            "/pago-repartidor/**"
                    ).hasAnyRole("JEFA_COCINA", "COCINA")

                    .anyRequest().authenticated()
            )

            // ── Orden: GlobalRateLimit → LoginRateLimit → Correlation → JWT → Spring ──
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(correlationFilter(), JwtAuthenticationFilter.class)
            .addFilterBefore(loginRateLimitFilter(), CorrelationFilter.class)
            .addFilterBefore(globalRateLimitFilter(), LoginRateLimitFilter.class)

            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(entryPointNoAutorizado)
                    .accessDeniedHandler(manejadorAccesoDenegado)
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Ajustar orígenes para producción
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        // Necesario para que el frontend pueda leer el header Authorization renovado
        config.setExposedHeaders(List.of("Authorization", "X-Correlation-ID"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
