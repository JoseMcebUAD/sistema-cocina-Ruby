package com.cocinarubi.dao;

import com.cocinarubi.entity.Anuncio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface AnuncioRepository extends JpaRepository<Anuncio, Integer> {

    @Override
    @Query("SELECT a FROM Anuncio a ORDER BY a.fechaExpiracionAnuncio DESC")
    List<Anuncio> findAll();
}
