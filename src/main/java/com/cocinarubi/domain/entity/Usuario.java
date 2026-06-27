package com.cocinarubi.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;

/**
 * Operador del dashboard interno (cajero, cocinero, administrador).
 *
 * <p>La autenticación es por {@code nombre_usuario} + PIN de 5 dígitos hasheado
 * con BCrypt (RNF). Implementa {@link UserDetails} para integrarse con
 * Spring Security. La sesión JWT expira a los 3 días sin actividad y el token
 * se renueva cada 3 horas.</p>
 *
 * <p>El rol determina qué rutas del dashboard están disponibles para el usuario.
 * El prefijo {@code ROLE_} se agrega automáticamente en {@link #getAuthorities()}.</p>
 *
 * <p>Relaciones: {@code @ManyToOne} LAZY a {@link RolUsuario}.</p>
 */
@Entity
@Table(name = "usuario")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", nullable = false)
    private RolUsuario rolUsuario;

    @Column(name = "nombre_usuario", nullable = false, length = 20)
    private String nombreUsuario;

    @JsonIgnore
    @Column(name = "contrasena", nullable = false, length = 70)
    private String contrasena;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    // -------------------------------------------------------------------------
    // UserDetails
    // -------------------------------------------------------------------------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rolUsuario.getNombreRol()));
    }

    @Override
    public String getPassword() {
        return this.contrasena;
    }

    @Override
    public String getUsername() {
        return this.nombreUsuario;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return bloqueadoHasta == null
                || bloqueadoHasta.isBefore(LocalDateTime.now(ZoneId.of("America/Merida")));
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
