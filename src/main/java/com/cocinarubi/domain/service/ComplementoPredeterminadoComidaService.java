package com.cocinarubi.domain.service;

import com.cocinarubi.dao.ComplementoPredeterminadoComidaRepository;
import com.cocinarubi.domain.entity.Comida;
import com.cocinarubi.domain.entity.Complemento;
import com.cocinarubi.domain.entity.ComplementoPredeterminadoComida;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.ComplementoPredeterminadoComidaRequestDTO;
import com.cocinarubi.presentation.dto.response.ComplementoPredeterminadoComidaResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Gestiona los complementos predeterminados asignados a cada comida del menú.
 * Capa: Service — lógica de negocio de asignación y validación de predeterminados.
 */
@Service
public class ComplementoPredeterminadoComidaService {

    private final ComplementoPredeterminadoComidaRepository repo;
    private final ComidaService comidaService;
    private final ComplementoService complementoService;

    public ComplementoPredeterminadoComidaService(ComplementoPredeterminadoComidaRepository repo,
                                                   ComidaService comidaService,
                                                   ComplementoService complementoService) {
        this.repo = repo;
        this.comidaService = comidaService;
        this.complementoService = complementoService;
    }

    public List<ComplementoPredeterminadoComidaResponseDTO> findByIdComida(int idComida) {
        return repo.findByComida_IdComida(idComida)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Asigna un complemento predeterminado a una comida.
     * Valida existencia de comida y complemento, y unicidad de la combinación.
     */
    public ComplementoPredeterminadoComidaResponseDTO save(int idComida,
                                                           ComplementoPredeterminadoComidaRequestDTO dto) {
        // ComidaService: valida que la comida exista antes de asignar el predeterminado
        Comida comida = comidaService.findById(idComida);

        // ComplementoService: valida que el complemento exista
        Complemento complemento = complementoService.findById(dto.getIdComplemento());

        if (repo.existsByComida_IdComidaAndComplemento_IdComplemento(idComida, dto.getIdComplemento())) {
            throw new BusinessException(
                    "El complemento ya está asignado como predeterminado a esta comida",
                    HttpStatus.CONFLICT);
        }

        ComplementoPredeterminadoComida nuevo = ComplementoPredeterminadoComida.builder()
                .comida(comida)
                .complemento(complemento)
                .cantidad(dto.getCantidad())
                .build();

        return toDTO(repo.save(nuevo));
    }

    public void delete(int id) {
        if (!repo.existsById(id)) {
            throw new BusinessException(
                    "Complemento predeterminado no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        repo.deleteById(id);
    }

    private ComplementoPredeterminadoComidaResponseDTO toDTO(ComplementoPredeterminadoComida c) {
        return new ComplementoPredeterminadoComidaResponseDTO(
                c.getIdComplementoPredeterminadoComida(),
                c.getComplemento().getIdComplemento(),
                c.getComplemento().getNombreComplemento(),
                c.getCantidad()
        );
    }
}
