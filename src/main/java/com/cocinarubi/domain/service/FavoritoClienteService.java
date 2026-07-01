package com.cocinarubi.domain.service;

import com.cocinarubi.DBConstants.TipoCatalogoProducto;
import com.cocinarubi.dao.BasicoRepository;
import com.cocinarubi.dao.ClienteRepository;
import com.cocinarubi.dao.ComidaRepository;
import com.cocinarubi.dao.DesayunoRepository;
import com.cocinarubi.dao.FavoritoClienteRepository;
import com.cocinarubi.dao.ProductoCocinaRepository;
import com.cocinarubi.domain.entity.Cliente;
import com.cocinarubi.domain.entity.FavoritoCliente;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.FavoritoClienteRequestDTO;
import com.cocinarubi.presentation.dto.response.FavoritoClienteResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.FavoritoClienteConfirmationImp;
import com.cocinarubi.presentation.strategy.strategyImplementation.FavoritoClienteValidationImp;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestiona los favoritos del cliente con resolución polimórfica del producto
 * referenciado según {@link TipoCatalogoProducto}.
 */
@Service
public class FavoritoClienteService {

    private final FavoritoClienteRepository favoritoClienteRepository;
    private final ClienteRepository clienteRepository;
    private final ComidaRepository comidaRepository;
    private final DesayunoRepository desayunoRepository;
    private final BasicoRepository basicoRepository;
    private final ProductoCocinaRepository productoCocinaRepository;
    private final FavoritoClienteValidationImp favoritoClienteValidation;
    private final FavoritoClienteConfirmationImp favoritoClienteConfirmation;

    public FavoritoClienteService(FavoritoClienteRepository favoritoClienteRepository,
                                  ClienteRepository clienteRepository,
                                  ComidaRepository comidaRepository,
                                  DesayunoRepository desayunoRepository,
                                  BasicoRepository basicoRepository,
                                  ProductoCocinaRepository productoCocinaRepository,
                                  FavoritoClienteValidationImp favoritoClienteValidation,
                                  FavoritoClienteConfirmationImp favoritoClienteConfirmation) {
        this.favoritoClienteRepository = favoritoClienteRepository;
        this.clienteRepository = clienteRepository;
        this.comidaRepository = comidaRepository;
        this.desayunoRepository = desayunoRepository;
        this.basicoRepository = basicoRepository;
        this.productoCocinaRepository = productoCocinaRepository;
        this.favoritoClienteValidation = favoritoClienteValidation;
        this.favoritoClienteConfirmation = favoritoClienteConfirmation;
    }

    @Transactional(readOnly = true)
    public List<FavoritoClienteResponseDTO> findAll() {
        return favoritoClienteRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FavoritoClienteResponseDTO findById(int id) {
        return toResponseDTO(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public List<FavoritoClienteResponseDTO> findBySessionToken(String sessionToken) {
        return favoritoClienteRepository.findByCliente_SessionToken(sessionToken).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public FavoritoClienteResponseDTO save(FavoritoClienteRequestDTO dto) {
        favoritoClienteValidation.validarPost(dto);
        if (!dto.isSaltarConfirmacion()) {
            favoritoClienteConfirmation.validarPost(dto);
        }
        Cliente cliente = clienteRepository.findBySessionToken(dto.getSessionToken())
                .orElseThrow(() -> new BusinessException(
                        "Cliente no encontrado", HttpStatus.BAD_REQUEST));
        FavoritoCliente entidad = FavoritoCliente.builder()
                .cliente(cliente)
                .idProducto(dto.getIdProducto())
                .tipoCatalogoProducto(dto.getTipoCatalogoProducto())
                .build();
        return toResponseDTO(favoritoClienteRepository.save(entidad));
    }

    @Transactional
    public FavoritoClienteResponseDTO update(int id, FavoritoClienteRequestDTO dto) {
        favoritoClienteValidation.validarPost(dto);
        FavoritoCliente existente = findEntityById(id);
        existente.setIdProducto(dto.getIdProducto());
        existente.setTipoCatalogoProducto(dto.getTipoCatalogoProducto());
        return toResponseDTO(favoritoClienteRepository.save(existente));
    }

    public void delete(int id) {
        if (!favoritoClienteRepository.existsById(id)) {
            throw new BusinessException(
                    "Favorito no encontrado con id: " + id, HttpStatus.NOT_FOUND);
        }
        favoritoClienteRepository.deleteById(id);
    }

    private FavoritoCliente findEntityById(int id) {
        return favoritoClienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Favorito no encontrado con id: " + id, HttpStatus.NOT_FOUND));
    }

    /** Resuelve el nombre del producto consultando la tabla correspondiente según el tipo. */
    private String resolverNombreProducto(TipoCatalogoProducto tipo, int idProducto) {
        return switch (tipo) {
            case COMIDA -> comidaRepository.findById(idProducto)
                    .map(c -> c.getNombreComida()).orElse("(producto eliminado)");
            case DESAYUNO -> desayunoRepository.findById(idProducto)
                    .map(d -> d.getNombreDesayuno()).orElse("(producto eliminado)");
            case BASICO -> basicoRepository.findById(idProducto)
                    .map(b -> b.getComida() != null ? b.getComida().getNombreComida() : "(básico)")
                    .orElse("(producto eliminado)");
            case SNACK, CHAROLA, BEBIDA -> productoCocinaRepository.findById(idProducto)
                    .map(p -> p.getNombreProducto()).orElse("(producto eliminado)");
        };
    }

    private FavoritoClienteResponseDTO toResponseDTO(FavoritoCliente entidad) {
        String nombre = resolverNombreProducto(
                entidad.getTipoCatalogoProducto(), entidad.getIdProducto());
        return new FavoritoClienteResponseDTO(
                entidad.getIdFavoritoCliente(),
                entidad.getCliente() != null ? entidad.getCliente().getSessionToken() : null,
                entidad.getIdProducto(),
                entidad.getTipoCatalogoProducto(),
                nombre
        );
    }
}
