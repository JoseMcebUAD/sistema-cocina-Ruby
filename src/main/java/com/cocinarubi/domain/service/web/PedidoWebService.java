package com.cocinarubi.domain.service.web;

import com.cocinarubi.DBConstants.PedidoCreadoDesde;
import com.cocinarubi.dao.ClienteRepository;
import com.cocinarubi.dao.PedidoRepository;
import com.cocinarubi.dao.TarifaEspecialRepository;
import com.cocinarubi.domain.entity.Cliente;
import com.cocinarubi.domain.mapper.PedidoMapper;
import com.cocinarubi.domain.service.CatalogoPedidoService;
import com.cocinarubi.domain.service.PedidoService;
import com.cocinarubi.exception.BusinessException;
import com.cocinarubi.presentation.dto.request.PedidoRequestDTO;
import com.cocinarubi.presentation.dto.response.PedidoResponseDTO;
import com.cocinarubi.presentation.strategy.strategyImplementation.PedidoConfirmationImp;
import com.cocinarubi.presentation.strategy.strategyImplementation.PedidoValidationImp;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PedidoWebService extends PedidoService {

    private final ClienteRepository clienteRepository;
    private final HttpServletRequest httpRequest;

    public PedidoWebService(PedidoRepository pedidoRepository,
                            PedidoValidationImp pedidoValidation,
                            PedidoConfirmationImp pedidoConfirmation,
                            PedidoMapper pedidoMapper,
                            CatalogoPedidoService catalogoPedido,
                            ApplicationEventPublisher eventPublisher,
                            TarifaEspecialRepository tarifaEspecialRepository,
                            ClienteRepository clienteRepository,
                            HttpServletRequest httpRequest) {
        super(pedidoRepository, pedidoValidation, pedidoConfirmation, pedidoMapper,
                catalogoPedido, eventPublisher, tarifaEspecialRepository);
        this.clienteRepository = clienteRepository;
        this.httpRequest = httpRequest;
    }

    @Override
    @Transactional
    public PedidoResponseDTO save(PedidoRequestDTO dto) {
        verificarTokenWeb(dto);
        return super.save(dto);
    }

    @Override
    @Transactional
    public PedidoResponseDTO update(int id, PedidoRequestDTO dto) {
        verificarTokenWeb(dto);
        return super.update(id, dto);
    }

    private void verificarTokenWeb(PedidoRequestDTO dto) {
        if (!PedidoCreadoDesde.WEB.equals(dto.getPedidoCreadoDesde())) {
            throw new BusinessException(
                    "Este endpoint solo acepta pedidos de origen WEB", HttpStatus.BAD_REQUEST);
        }

        String header = httpRequest.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BusinessException("Token de sesión requerido", HttpStatus.UNAUTHORIZED);
        }

        String token = header.substring(7);
        Optional<Cliente> clienteOpt = clienteRepository.findBySessionToken(token);

        if (clienteOpt.isEmpty()
                || clienteOpt.get().getTokenExpiracion() == null
                || clienteOpt.get().getTokenExpiracion().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Token de sesión inválido o expirado", HttpStatus.UNAUTHORIZED);
        }
    }
}
