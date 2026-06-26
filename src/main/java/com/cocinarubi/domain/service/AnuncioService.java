package com.cocinarubi.domain.service;

import com.cocinarubi.dao.AnuncioRepository;
import com.cocinarubi.domain.entity.Anuncio;
import com.cocinarubi.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AnuncioService {

    private final AnuncioRepository anuncioRepository;

    public AnuncioService(AnuncioRepository anuncioRepository) {
        this.anuncioRepository = anuncioRepository;
    }

    public List<Anuncio> findAll() {
        return anuncioRepository.findAll();
    }

    public Anuncio findById(int id) {
        return anuncioRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Anuncio no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    public Anuncio save(Anuncio anuncio) {
        return anuncioRepository.save(anuncio);
    }

    public void delete(int id) {
        if (!anuncioRepository.existsById(id)) {
            throw new BusinessException("Anuncio no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        anuncioRepository.deleteById(id);
    }
}
