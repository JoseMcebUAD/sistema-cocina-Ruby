package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.ComplementoPredeterminadoComida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para complementos predeterminados asignados a comidas.
 * Capa: dao — acceso a datos de la tabla complemento_predeterminado_comida.
 */
@Repository
public interface ComplementoPredeterminadoComidaRepository extends JpaRepository<ComplementoPredeterminadoComida, Integer> {

    List<ComplementoPredeterminadoComida> findByComida_IdComida(int idComida);

    boolean existsByComida_IdComidaAndComplemento_IdComplemento(int idComida, int idComplemento);
}
