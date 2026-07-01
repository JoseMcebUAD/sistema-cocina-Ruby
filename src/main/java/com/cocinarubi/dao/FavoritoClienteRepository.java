package com.cocinarubi.dao;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.domain.entity.FavoritoCliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoritoClienteRepository extends JpaRepository<FavoritoCliente, Integer> {

    List<FavoritoCliente> findByCliente_SessionToken(String sessionToken);

    boolean existsByCliente_SessionTokenAndIdProductoAndTipoCatalogoProducto(
            String sessionToken, Integer idProducto, TipoCatalogoProducto tipoCatalogoProducto);
}
