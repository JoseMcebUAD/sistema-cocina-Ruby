package com.cocinarubi.dao;

import com.cocinarubi.domain.entity.ProductoReceta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRecetaRepository extends JpaRepository<ProductoReceta, Integer> {

    // JOIN FETCH para evitar N+1 al pintar la lista de ingredientes.
    @Query("SELECT pr FROM ProductoReceta pr JOIN FETCH pr.producto "
            + "WHERE pr.comida.idComida = :idComida ORDER BY pr.idProductoReceta ASC")
    List<ProductoReceta> findByIdComidaWithProducto(@Param("idComida") int idComida);

    Optional<ProductoReceta> findByComida_IdComidaAndProducto_IdProducto(int idComida, int idProducto);

    boolean existsByComida_IdComidaAndProducto_IdProducto(int idComida, int idProducto);
}
