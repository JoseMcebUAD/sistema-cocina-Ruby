package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    @Override
    @Query("SELECT u FROM Usuario u ORDER BY u.nombreUsuario ASC")
    List<Usuario> findAll();

    boolean existsByNombreUsuario(String nombreUsuario);

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
}
